package com.ryuqq.crawlinghub.application.schedule.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ScheduleOutcome;
import com.ryuqq.crawlinghub.application.schedule.port.out.EventBridgeSchedulerPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.ScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Schedule Outbox State Manager
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Outbox 상태 변경 (생성, 처리 중, 완료, 실패)</li>
 *   <li>✅ Outbox CRUD (Port 호출)</li>
 *   <li>✅ EventBridge API 호출 (외부 시스템 통신)</li>
 *   <li>✅ 트랜잭션 경계 관리 (각 Outbox 처리는 별도 트랜잭션)</li>
 * </ul>
 *
 * <p><strong>AOP 이슈 해결:</strong></p>
 * <ul>
 *   <li>✅ ScheduleOutboxProcessor에서 분리 → @Transactional 정상 작동</li>
 *   <li>✅ Processor는 이 Manager를 호출 → Spring Proxy 통과</li>
 *   <li>✅ 각 Outbox 처리는 독립적인 트랜잭션 (실패 격리)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java Constructor (Lombok 금지)</li>
 *   <li>✅ @Component (Spring Bean 등록)</li>
 *   <li>✅ @Transactional (트랜잭션 경계)</li>
 *   <li>✅ Single Responsibility (Outbox 상태 관리만)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class ScheduleOutboxStateManager {

    private static final Logger log = LoggerFactory.getLogger(ScheduleOutboxStateManager.class);

    private final ScheduleOutboxPort outboxPort;
    private final EventBridgeSchedulerPort eventBridgePort;
    private final ObjectMapper objectMapper;

    public ScheduleOutboxStateManager(
        ScheduleOutboxPort outboxPort,
        EventBridgeSchedulerPort eventBridgePort,
        ObjectMapper objectMapper
    ) {
        this.outboxPort = outboxPort;
        this.eventBridgePort = eventBridgePort;
        this.objectMapper = objectMapper;
    }

    /**
     * Outbox 생성 및 저장
     *
     * @param sellerId 셀러 ID
     * @param payload EventBridge Payload (JSON)
     * @param idemKey Idempotency Key
     * @param eventType 이벤트 타입 (EVENTBRIDGE_REGISTER, EVENTBRIDGE_UPDATE, EVENTBRIDGE_DELETE)
     * @return 저장된 Outbox
     */
    @Transactional
    public ScheduleOutbox createOutbox(
        Long sellerId,
        String payload,
        String idemKey,
        String eventType
    ) {
        ScheduleOutbox outbox;

        switch (eventType) {
            case "EVENTBRIDGE_REGISTER" -> outbox = ScheduleOutbox.forEventBridgeRegistration(
                sellerId, payload, idemKey
            );
            case "EVENTBRIDGE_UPDATE" -> outbox = ScheduleOutbox.forEventBridgeUpdate(
                sellerId, payload, idemKey
            );
            case "EVENTBRIDGE_DELETE" -> outbox = ScheduleOutbox.forEventBridgeDelete(
                sellerId, payload, idemKey
            );
            default -> throw new IllegalArgumentException("알 수 없는 이벤트 타입: " + eventType);
        }

        return outboxPort.save(outbox);
    }

    /**
     * Outbox 조회 (Idempotency Key)
     *
     * @param idemKey Idempotency Key
     * @return Outbox (존재하지 않으면 null)
     */
    @Transactional(readOnly = true)
    public ScheduleOutbox findByIdemKey(String idemKey) {
        return outboxPort.findByIdemKey(idemKey).orElse(null);
    }

    /**
     * Outbox 존재 여부 확인 (Idempotency Check)
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsByIdemKey(String idemKey) {
        return outboxPort.existsByIdemKey(idemKey);
    }

    /**
     * PENDING 상태 Outbox 조회
     *
     * @return PENDING 상태 Outbox 목록
     */
    @Transactional(readOnly = true)
    public List<ScheduleOutbox> findPendingOutboxes() {
        return outboxPort.findByWalStatePending();
    }

    /**
     * 단일 Outbox 처리 (별도 트랜잭션) ⭐ AOP 이슈 해결!
     *
     * <p><strong>AOP 작동 원리:</strong></p>
     * <ul>
     *   <li>✅ ScheduleOutboxProcessor가 이 Manager를 Spring Bean으로 주입받음</li>
     *   <li>✅ Processor에서 manager.processOne() 호출 → Spring Proxy 통과</li>
     *   <li>✅ @Transactional이 정상 작동 (각 Outbox는 독립 트랜잭션)</li>
     * </ul>
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Timeout 체크</li>
     *   <li>상태 전이: PENDING → IN_PROGRESS</li>
     *   <li>EventBridge API 호출 (외부 시스템 - 트랜잭션 밖!)</li>
     *   <li>결과에 따라 상태 업데이트: COMPLETED or FAILED</li>
     *   <li>DB 저장</li>
     * </ol>
     *
     * @param outbox 처리할 Outbox
     * @return ScheduleOutcome (Ok/Fail)
     */
    @Transactional
    public ScheduleOutcome processOne(ScheduleOutbox outbox) {
        log.info("🔄 Outbox 처리 시작: ID={}, EventType={}, IdemKey={}",
            outbox.getId(), outbox.getEventType(), outbox.getIdemKey());

        // 1. Timeout 체크
        if (outbox.isTimeout()) {
            log.warn("⏱️ Outbox Timeout: ID={}, Timeout={}ms",
                outbox.getId(), outbox.getTimeoutMillis());
            outbox.markTimeout();
            outboxPort.save(outbox);
            return ScheduleOutcome.fail("TIMEOUT", "Outbox 처리 시간 초과", "Timeout");
        }

        // 2. 상태 전이: PENDING → IN_PROGRESS
        outbox.startProcessing();
        outboxPort.save(outbox);

        // 3. EventBridge 호출 (외부 API - 트랜잭션 밖!)
        ScheduleOutcome outcome = executeEventBridgeOperation(outbox);

        // 4. 결과 처리
        if (outcome.isSuccess()) {
            log.info("✅ EventBridge 성공: ID={}, Message={}", outbox.getId(), outcome.message());
            outbox.markCompleted();
        } else {
            log.error("❌ EventBridge 실패: ID={}, Error={}, Cause={}",
                outbox.getId(), outcome.errorCode(), outcome.cause());
            outbox.recordFailure(outcome.message());
        }

        outboxPort.save(outbox);

        log.info("🏁 Outbox 처리 완료: ID={}, FinalState={}/{}",
            outbox.getId(), outbox.getWalState(), outbox.getOperationState());

        return outcome;
    }

    /**
     * EventBridge 작업 실행 (외부 API 호출)
     *
     * <p>Payload에서 EventType을 추출하여 적절한 EventBridge API 호출:
     * <ul>
     *   <li>EVENTBRIDGE_REGISTER: registerSchedule()</li>
     *   <li>EVENTBRIDGE_UPDATE: updateSchedule()</li>
     *   <li>EVENTBRIDGE_DELETE: deleteSchedule()</li>
     * </ul>
     *
     * @param outbox Outbox
     * @return ScheduleOutcome (Ok/Fail)
     */
    private ScheduleOutcome executeEventBridgeOperation(ScheduleOutbox outbox) {
        try {
            // Payload 파싱
            CrawlSchedule.EventBridgePayload payload = objectMapper.readValue(
                outbox.getPayload(),
                CrawlSchedule.EventBridgePayload.class
            );

            // EventType에 따라 분기
            String eventType = outbox.getEventType();
            switch (eventType) {
                case "EVENTBRIDGE_REGISTER" -> {
                    String scheduleName = eventBridgePort.registerSchedule(
                        payload.scheduleId(),
                        payload.sellerId(),
                        payload.cronExpression()
                    );
                    return ScheduleOutcome.success("EventBridge 등록 성공: " + scheduleName);
                }
                case "EVENTBRIDGE_UPDATE" -> {
                    eventBridgePort.updateSchedule(
                        payload.scheduleId(),
                        payload.sellerId(),
                        payload.cronExpression()
                    );
                    return ScheduleOutcome.success("EventBridge 업데이트 성공");
                }
                case "EVENTBRIDGE_DELETE" -> {
                    eventBridgePort.deleteSchedule(
                        payload.scheduleId(),
                        payload.sellerId()
                    );
                    return ScheduleOutcome.success("EventBridge 삭제 성공");
                }
                default -> {
                    return ScheduleOutcome.failure(
                        "UNKNOWN_EVENT_TYPE",
                        "알 수 없는 EventType: " + eventType,
                        eventType
                    );
                }
            }
        } catch (JsonProcessingException e) {
            return ScheduleOutcome.failure(
                "PAYLOAD_PARSE_ERROR",
                "Payload 파싱 실패: " + e.getMessage(),
                e.getClass().getName()
            );
        } catch (Exception e) {
            return ScheduleOutcome.failure(
                "EVENTBRIDGE_API_ERROR",
                "EventBridge API 호출 실패: " + e.getMessage(),
                e.getClass().getName()
            );
        }
    }
}
