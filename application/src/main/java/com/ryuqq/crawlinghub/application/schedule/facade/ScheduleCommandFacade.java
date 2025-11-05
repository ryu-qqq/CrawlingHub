package com.ryuqq.crawlinghub.application.schedule.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleResponse;
import com.ryuqq.crawlinghub.application.schedule.port.out.LoadSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SaveSchedulePort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 스케줄 Command Facade (S1 Phase - Accept)
 *
 * <p>Orchestration Pattern 3-Phase Lifecycle의 S1 Phase를 담당합니다:
 * <ul>
 *   <li>S1 (Accept): DB 저장 + Outbox 저장 → 즉시 202 Accepted 반환</li>
 *   <li>S2 (Execute): OutboxProcessor가 별도 처리 (EventBridge 호출)</li>
 *   <li>S3 (Finalize): Finalizer가 재시도 및 정리</li>
 * </ul>
 *
 * <p>핵심 원칙:
 * <ul>
 *   <li>❌ @Transactional 내 외부 API 호출 절대 금지</li>
 *   <li>✅ DB 저장 + Outbox 저장만 수행 (같은 트랜잭션)</li>
 *   <li>✅ 즉시 202 Accepted 반환 (빠른 응답)</li>
 *   <li>✅ EventBridge 실패해도 DB는 안전 (Outbox가 재시도)</li>
 * </ul>
 *
 * @author 개발자
 * @since 2024-01-01
 */
@Service
public class ScheduleCommandFacade {

    private final CronExpressionValidator cronValidator;
    private final SaveSchedulePort saveSchedulePort;
    private final LoadSchedulePort loadSchedulePort;
    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final ObjectMapper objectMapper;

    public ScheduleCommandFacade(
        CronExpressionValidator cronValidator,
        SaveSchedulePort saveSchedulePort,
        LoadSchedulePort loadSchedulePort,
        SellerCrawlScheduleOutboxPort outboxPort,
        ObjectMapper objectMapper
    ) {
        this.cronValidator = cronValidator;
        this.saveSchedulePort = saveSchedulePort;
        this.loadSchedulePort = loadSchedulePort;
        this.outboxPort = outboxPort;
        this.objectMapper = objectMapper;
    }

    /**
     * 스케줄 생성 (S1 Phase - Accept)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Idempotency 체크 (중복 요청 방지)</li>
     *   <li>Cron 표현식 검증</li>
     *   <li>CrawlSchedule Domain 생성</li>
     *   <li>다음 실행 시간 계산</li>
     *   <li>DB 저장 (Schedule)</li>
     *   <li>Outbox 저장 (EventBridge 작업 기록 - 같은 트랜잭션)</li>
     *   <li>즉시 202 Accepted 반환</li>
     * </ol>
     *
     * @param command 생성 Command
     * @return ScheduleResponse (DB 저장 완료 상태)
     */
    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleCommand command) {
        // 1. Idempotency Check
        String idemKey = generateIdemKey(command.sellerId(), "CREATE");
        if (outboxPort.existsByIdemKey(idemKey)) {
            return outboxPort.findByIdemKey(idemKey)
                .map(this::toScheduleResponseFromOutbox)
                .orElseThrow(() -> new IllegalStateException("Idempotency Key는 존재하지만 Outbox를 찾을 수 없습니다"));
        }

        // 2. Cron 검증 (빠른 실패)
        validateCronExpression(command.cronExpression());

        // 3. Domain 생성
        CrawlSchedule schedule = CrawlSchedule.forNew(
            MustitSellerId.of(command.sellerId()),
            CronExpression.of(command.cronExpression())
        );

        // 4. 다음 실행 시간 계산
        LocalDateTime nextExecution = cronValidator.calculateNextExecution(
            command.cronExpression(),
            LocalDateTime.now()
        );
        schedule.calculateNextExecution(nextExecution);

        // 5. DB 저장
        CrawlSchedule savedSchedule = saveSchedulePort.save(schedule);

        // 6. Outbox 저장 (같은 트랜잭션 - EventBridge 작업 기록)
        SellerCrawlScheduleOutbox outbox = SellerCrawlScheduleOutbox.forEventBridgeRegistration(
            savedSchedule.getSellerIdValue(),
            toPayloadJson(savedSchedule.toEventBridgePayload()),
            idemKey
        );
        outboxPort.save(outbox);

        // 7. Domain Event 등록 (트랜잭션 커밋 후 자동 발행)
        // ✅ 이벤트는 트랜잭션 커밋 후 발행되며, EventListener에서 비동기로 Outbox Processor 호출
        savedSchedule.registerEvent(
            com.ryuqq.crawlinghub.domain.schedule.event.ScheduleCreatedEvent.of(
                savedSchedule.getIdValue(),
                savedSchedule.getSellerIdValue(),
                savedSchedule.getCronExpressionValue(),
                idemKey
            )
        );
        // 이벤트를 등록했으므로 다시 저장해야 함 (Spring Data가 이벤트 발행)
        saveSchedulePort.save(savedSchedule);

        // 8. 즉시 202 Accepted 반환 (EventBridge 호출 없음!)
        // ✅ 트랜잭션 커밋 후 이벤트가 발행되고, EventListener에서 비동기로 Outbox Processor 호출
        return toScheduleResponse(savedSchedule.toResponse());
    }

    /**
     * 스케줄 수정 (S1 Phase - Accept)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Idempotency 체크</li>
     *   <li>Cron 표현식 검증</li>
     *   <li>기존 Schedule 조회</li>
     *   <li>Schedule 수정 (updateSchedule)</li>
     *   <li>다음 실행 시간 재계산</li>
     *   <li>DB 저장 (Schedule)</li>
     *   <li>Outbox 저장 (EventBridge UPDATE 작업 기록)</li>
     *   <li>즉시 202 Accepted 반환</li>
     * </ol>
     *
     * @param command 수정 Command
     * @return ScheduleResponse (DB 저장 완료 상태)
     */
    @Transactional
    public ScheduleResponse updateSchedule(UpdateScheduleCommand command) {
        // 1. 기존 Schedule 조회
        CrawlSchedule schedule = loadSchedulePort.findById(CrawlScheduleId.of(command.scheduleId()))
            .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다: " + command.scheduleId()));

        // 2. Idempotency Check
        String idemKey = generateIdemKey(schedule.getSellerIdValue(), "UPDATE_" + command.scheduleId());
        if (outboxPort.existsByIdemKey(idemKey)) {
            return outboxPort.findByIdemKey(idemKey)
                .map(this::toScheduleResponseFromOutbox)
                .orElseThrow(() -> new IllegalStateException("Idempotency Key는 존재하지만 Outbox를 찾을 수 없습니다"));
        }

        // 3. Cron 검증
        validateCronExpression(command.cronExpression());

        // 4. Schedule 수정
        schedule.updateSchedule(CronExpression.of(command.cronExpression()));

        // 5. 다음 실행 시간 재계산
        LocalDateTime nextExecution = cronValidator.calculateNextExecution(
            command.cronExpression(),
            LocalDateTime.now()
        );
        schedule.calculateNextExecution(nextExecution);

        // 6. DB 저장
        CrawlSchedule updatedSchedule = saveSchedulePort.save(schedule);

        // 7. Outbox 저장 (EventBridge UPDATE 작업 기록)
        SellerCrawlScheduleOutbox outbox = SellerCrawlScheduleOutbox.forEventBridgeUpdate(
            updatedSchedule.getSellerIdValue(),
            toPayloadJson(updatedSchedule.toEventBridgePayload()),
            idemKey
        );
        outboxPort.save(outbox);

        // 8. Domain Event 등록 (트랜잭션 커밋 후 자동 발행)
        // ✅ 이벤트는 트랜잭션 커밋 후 발행되며, EventListener에서 비동기로 Outbox Processor 호출
        updatedSchedule.registerEvent(
            com.ryuqq.crawlinghub.domain.schedule.event.ScheduleUpdatedEvent.of(
                updatedSchedule.getIdValue(),
                updatedSchedule.getSellerIdValue(),
                updatedSchedule.getCronExpressionValue(),
                idemKey
            )
        );
        // 이벤트를 등록했으므로 다시 저장해야 함 (Spring Data가 이벤트 발행)
        saveSchedulePort.save(updatedSchedule);

        // 9. 즉시 202 Accepted 반환
        // ✅ 트랜잭션 커밋 후 이벤트가 발행되고, EventListener에서 비동기로 Outbox Processor 호출
        return toScheduleResponse(updatedSchedule.toResponse());
    }

    /**
     * Cron 표현식 검증 (공통 로직)
     *
     * @param expression Cron 표현식
     */
    private void validateCronExpression(String expression) {
        if (!cronValidator.isValid(expression)) {
            throw new IllegalArgumentException("유효하지 않은 Cron 표현식입니다: " + expression);
        }
    }

    /**
     * Idempotency Key 생성
     *
     * <p>형식: seller:{sellerId}:event:{eventType}:uuid
     *
     * @param sellerId 셀러 ID
     * @param eventType 이벤트 타입 (CREATE, UPDATE)
     * @return Idempotency Key
     */
    private String generateIdemKey(Long sellerId, String eventType) {
        return String.format("seller:%d:event:%s:%s",
            sellerId,
            eventType,
            UUID.randomUUID().toString().substring(0, 8)
        );
    }

    /**
     * EventBridge Payload를 JSON으로 변환
     */
    private String toPayloadJson(CrawlSchedule.EventBridgePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Payload JSON 변환 실패", e);
        }
    }

    /**
     * Domain의 ScheduleResponseData를 Application의 ScheduleResponse로 변환
     */
    private ScheduleResponse toScheduleResponse(CrawlSchedule.ScheduleResponseData data) {
        return new ScheduleResponse(
            data.scheduleId(),
            data.sellerId(),
            data.cronExpression(),
            data.status(),
            data.nextExecutionTime(),
            data.lastExecutedAt(),
            data.createdAt(),
            data.updatedAt()
        );
    }

    /**
     * Outbox에서 ScheduleResponse 복원 (Idempotency 처리용)
     *
     * <p>중복 요청 시 Outbox에서 기존 응답을 복원합니다.
     */
    private ScheduleResponse toScheduleResponseFromOutbox(SellerCrawlScheduleOutbox outbox) {
        try {
            // Payload에서 Schedule 정보 파싱
            CrawlSchedule.EventBridgePayload payload = objectMapper.readValue(
                outbox.getPayload(),
                CrawlSchedule.EventBridgePayload.class
            );

            // 간단한 ScheduleResponse 생성 (전체 정보는 없지만 중복 요청임을 알림)
            return new ScheduleResponse(
                payload.scheduleId(),
                payload.sellerId(),
                payload.cronExpression(),
                null, // 상태는 없음
                null, // 다음 실행 시간 없음
                null,
                outbox.getCreatedAt(),
                outbox.getUpdatedAt()
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox Payload 파싱 실패", e);
        }
    }
}
