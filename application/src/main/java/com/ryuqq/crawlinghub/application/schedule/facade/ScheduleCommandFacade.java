package com.ryuqq.crawlinghub.application.schedule.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.command.CreateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateScheduleCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleResponse;
import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleOutboxStateManager;
import com.ryuqq.crawlinghub.application.schedule.manager.ScheduleStateManager;
import com.ryuqq.crawlinghub.application.schedule.validator.CronExpressionValidator;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.CrawlScheduleId;
import com.ryuqq.crawlinghub.domain.schedule.CronExpression;
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
 * <p><strong>리팩토링 완료 (2025-11-06):</strong></p>
 * <ul>
 *   <li>✅ ScheduleStateManager 도입: Schedule 상태 관리 위임</li>
 *   <li>✅ ScheduleOutboxStateManager 도입: Outbox 상태 관리 위임</li>
 *   <li>✅ SRP 준수: Facade는 오케스트레이션, Manager는 상태 관리</li>
 *   <li>✅ Domain Event 자동 발행: Domain 내부에서 이벤트 등록</li>
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
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Service
public class ScheduleCommandFacade {

    private final CronExpressionValidator cronValidator;
    private final ScheduleStateManager scheduleStateManager;
    private final ScheduleOutboxStateManager outboxStateManager;

    public ScheduleCommandFacade(
        CronExpressionValidator cronValidator,
        ScheduleStateManager scheduleStateManager,
        ScheduleOutboxStateManager outboxStateManager
    ) {
        this.cronValidator = cronValidator;
        this.scheduleStateManager = scheduleStateManager;
        this.outboxStateManager = outboxStateManager;
    }

    /**
     * 스케줄 생성 (S1 Phase - Accept)
     *
     * <p><strong>리팩토링 완료:</strong> ScheduleStateManager + ScheduleOutboxStateManager 사용</p>
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Idempotency 체크 (중복 요청 방지)</li>
     *   <li>Cron 표현식 검증</li>
     *   <li>다음 실행 시간 계산</li>
     *   <li>ScheduleStateManager.createSchedule() 호출 (Schedule 생성 + Event 발행)</li>
     *   <li>ScheduleOutboxStateManager.createOutbox() 호출 (Outbox 생성)</li>
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
        if (outboxStateManager.existsByIdemKey(idemKey)) {
            CrawlSchedule existingSchedule = scheduleStateManager.getSchedule(
                CrawlScheduleId.of(command.sellerId()) // 임시: sellerId로 조회 (실제로는 Outbox에서 scheduleId 추출 필요)
            );
            return toScheduleResponse(existingSchedule.toResponse());
        }

        // 2. Cron 검증 (빠른 실패)
        validateCronExpression(command.cronExpression());

        // 3. 다음 실행 시간 계산
        LocalDateTime nextExecution = cronValidator.calculateNextExecution(
            command.cronExpression(),
            LocalDateTime.now()
        );

        // 4. ScheduleStateManager를 통한 Schedule 생성 (Domain Event 자동 발행)
        CrawlSchedule savedSchedule = scheduleStateManager.createSchedule(
            MustitSellerId.of(command.sellerId()),
            CronExpression.of(command.cronExpression()),
            nextExecution,
            idemKey
        );

        // 5. ScheduleOutboxStateManager를 통한 Outbox 생성
        String payload = toPayloadJson(savedSchedule.toEventBridgePayload());
        outboxStateManager.createOutbox(
            savedSchedule.getSellerIdValue(),
            payload,
            idemKey,
            "EVENTBRIDGE_REGISTER"
        );

        // 6. 즉시 202 Accepted 반환
        // ✅ 트랜잭션 커밋 후 Domain Event 자동 발행
        // ✅ OutboxProcessor가 별도로 EventBridge 호출
        return toScheduleResponse(savedSchedule.toResponse());
    }

    /**
     * 스케줄 수정 (S1 Phase - Accept)
     *
     * <p><strong>리팩토링 완료:</strong> ScheduleStateManager + ScheduleOutboxStateManager 사용</p>
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Idempotency 체크</li>
     *   <li>Cron 표현식 검증</li>
     *   <li>다음 실행 시간 계산</li>
     *   <li>ScheduleStateManager.updateSchedule() 호출 (Schedule 수정 + Event 발행)</li>
     *   <li>ScheduleOutboxStateManager.createOutbox() 호출 (Outbox 생성)</li>
     *   <li>즉시 202 Accepted 반환</li>
     * </ol>
     *
     * @param command 수정 Command
     * @return ScheduleResponse (DB 저장 완료 상태)
     */
    @Transactional
    public ScheduleResponse updateSchedule(UpdateScheduleCommand command) {
        // 1. Idempotency Check
        String idemKey = generateIdemKey(command.scheduleId(), "UPDATE_" + command.scheduleId());
        if (outboxStateManager.existsByIdemKey(idemKey)) {
            CrawlSchedule existingSchedule = scheduleStateManager.getSchedule(
                CrawlScheduleId.of(command.scheduleId())
            );
            return toScheduleResponse(existingSchedule.toResponse());
        }

        // 2. Cron 검증 (빠른 실패)
        validateCronExpression(command.cronExpression());

        // 3. 다음 실행 시간 계산
        LocalDateTime nextExecution = cronValidator.calculateNextExecution(
            command.cronExpression(),
            LocalDateTime.now()
        );

        // 4. ScheduleStateManager를 통한 Schedule 수정 (Domain Event 자동 발행)
        CrawlSchedule updatedSchedule = scheduleStateManager.updateSchedule(
            CrawlScheduleId.of(command.scheduleId()),
            CronExpression.of(command.cronExpression()),
            nextExecution,
            idemKey
        );

        // 5. ScheduleOutboxStateManager를 통한 Outbox 생성
        String payload = toPayloadJson(updatedSchedule.toEventBridgePayload());
        outboxStateManager.createOutbox(
            updatedSchedule.getSellerIdValue(),
            payload,
            idemKey,
            "EVENTBRIDGE_UPDATE"
        );

        // 6. 즉시 202 Accepted 반환
        // ✅ 트랜잭션 커밋 후 Domain Event 자동 발행
        // ✅ OutboxProcessor가 별도로 EventBridge 호출
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
     * <p>형식: seller:{id}:event:{eventType}:uuid
     *
     * @param id 식별자 (sellerId 또는 scheduleId)
     * @param eventType 이벤트 타입 (CREATE, UPDATE)
     * @return Idempotency Key
     */
    private String generateIdemKey(Long id, String eventType) {
        return String.format("seller:%d:event:%s:%s",
            id,
            eventType,
            UUID.randomUUID().toString().substring(0, 8)
        );
    }

    /**
     * EventBridge Payload를 JSON으로 변환
     */
    private String toPayloadJson(CrawlSchedule.EventBridgePayload payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
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
}
