package com.ryuqq.crawlinghub.application.schedule.orchestrator;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.application.schedule.orchestrator.outcome.ScheduleOutcome;
import com.ryuqq.crawlinghub.application.schedule.port.out.EventBridgeSchedulerPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.SellerCrawlScheduleOutboxPort;
import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;
import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;

/**
 * Schedule Outbox Processor (S2 Phase - Execute)
 *
 * <p>Orchestration Pattern 3-Phase Lifecycle의 S2 Phase를 담당합니다:
 * <ul>
 *   <li>S1 (Accept): Facade가 DB + Outbox 저장 완료</li>
 *   <li>S2 (Execute): **이 Processor가 Outbox를 읽고 EventBridge 호출** ✅</li>
 *   <li>S3 (Finalize): Finalizer가 재시도 및 정리</li>
 * </ul>
 *
 * <p>핵심 원칙:
 * <ul>
 *   <li>✅ @Scheduled로 주기적 실행 (1초마다)</li>
 *   <li>✅ Outbox에서 PENDING 상태 조회</li>
 *   <li>✅ EventBridge API 호출 (외부 시스템)</li>
 *   <li>✅ 성공 시 COMPLETED, 실패 시 retryCount++</li>
 *   <li>✅ 각 Outbox 처리는 별도 트랜잭션</li>
 * </ul>
 *
 * <p>왜 @Async가 아니라 @Scheduled인가?
 * <ul>
 *   <li>@Scheduled는 이미 별도 스레드 풀에서 실행됩니다</li>
 *   <li>Outbox 패턴은 Polling 방식입니다 (주기적 조회)</li>
 *   <li>@Async는 메서드 호출 시점에 비동기화하지만, Outbox는 이미 DB에 저장되어 있습니다</li>
 * </ul>
 *
 * @author 개발자
 * @since 2024-01-01
 */
@Component
public class ScheduleOutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(ScheduleOutboxProcessor.class);

    private final SellerCrawlScheduleOutboxPort outboxPort;
    private final EventBridgeSchedulerPort eventBridgePort;
    private final ObjectMapper objectMapper;

    public ScheduleOutboxProcessor(
        SellerCrawlScheduleOutboxPort outboxPort,
        EventBridgeSchedulerPort eventBridgePort,
        ObjectMapper objectMapper
    ) {
        this.outboxPort = outboxPort;
        this.eventBridgePort = eventBridgePort;
        this.objectMapper = objectMapper;
    }

    /**
     * Outbox 처리 (S2 Phase - Execute)
     *
     * <p>처리 흐름:
     * <ol>
     *   <li>Outbox에서 WAL_STATE=PENDING 조회</li>
     *   <li>각 Outbox에 대해 processOne() 호출</li>
     *   <li>EventBridge API 호출 (외부 시스템)</li>
     *   <li>성공: WAL_STATE=COMPLETED, OPERATION_STATE=COMPLETED</li>
     *   <li>실패: retryCount++, errorMessage 기록</li>
     * </ol>
     *
     * <p>실행 주기: 1초마다 (fixedDelay = 1000ms)
     */
    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        List<SellerCrawlScheduleOutbox> pendingOutboxes = outboxPort.findByWalStatePending();

        if (pendingOutboxes.isEmpty()) {
            return; // PENDING 없으면 조용히 종료
        }

        log.info("📋 Outbox 처리 시작: {} 건", pendingOutboxes.size());

        for (SellerCrawlScheduleOutbox outbox : pendingOutboxes) {
            try {
                processOne(outbox);
            } catch (Exception e) {
                log.error("❌ Outbox 처리 실패 (ID: {}): {}", outbox.getId(), e.getMessage(), e);
                // 개별 실패는 기록하고 계속 진행 (다른 Outbox 처리)
            }
        }

        log.info("✅ Outbox 처리 완료");
    }

    /**
     * 단일 Outbox 처리 (별도 트랜잭션)
     *
     * <p>왜 별도 트랜잭션인가?
     * <ul>
     *   <li>각 Outbox 처리는 독립적입니다</li>
     *   <li>한 Outbox 실패가 다른 Outbox에 영향 주지 않습니다</li>
     *   <li>EventBridge 호출 전후로 Outbox 상태를 업데이트해야 합니다</li>
     * </ul>
     *
     * @param outbox 처리할 Outbox
     */
    @Transactional
    public void processOne(SellerCrawlScheduleOutbox outbox) {
        log.info("🔄 Outbox 처리 시작: ID={}, EventType={}, IdemKey={}",
            outbox.getId(), outbox.getEventType(), outbox.getIdemKey());

        // 1. Timeout 체크
        if (outbox.isTimeout()) {
            log.warn("⏱️ Outbox Timeout: ID={}, Timeout={}ms",
                outbox.getId(), outbox.getTimeoutMillis());
            outbox.markTimeout();
            outboxPort.save(outbox);
            return;
        }

        // 2. 상태 전이: PENDING → IN_PROGRESS
        outbox.startProcessing();
        outboxPort.save(outbox);

        // 3. EventBridge 호출 (외부 API - 트랜잭션 밖!)
        ScheduleOutcome outcome = executeEventBridgeOperation(outbox);

        // 4. 결과 처리
        switch (outcome) {
            case ScheduleOutcome.Ok ok -> {
                log.info("✅ EventBridge 성공: ID={}, Message={}", outbox.getId(), ok.message());
                outbox.markCompleted();
            }
            case ScheduleOutcome.Fail fail -> {
                log.error("❌ EventBridge 실패: ID={}, Error={}, Cause={}",
                    outbox.getId(), fail.errorMessage(), fail.cause());
                outbox.recordFailure(fail.errorMessage());
            }
        }

        outboxPort.save(outbox);

        log.info("🏁 Outbox 처리 완료: ID={}, FinalState={}/{}",
            outbox.getId(), outbox.getWalState(), outbox.getOperationState());
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
    private ScheduleOutcome executeEventBridgeOperation(SellerCrawlScheduleOutbox outbox) {
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
                    return ScheduleOutcome.ok("EventBridge 등록 성공: " + scheduleName);
                }
                case "EVENTBRIDGE_UPDATE" -> {
                    eventBridgePort.updateSchedule(
                        payload.scheduleId(),
                        payload.sellerId(),
                        payload.cronExpression()
                    );
                    return ScheduleOutcome.ok("EventBridge 업데이트 성공");
                }
                case "EVENTBRIDGE_DELETE" -> {
                    eventBridgePort.deleteSchedule(
                        payload.scheduleId(),
                        payload.sellerId()
                    );
                    return ScheduleOutcome.ok("EventBridge 삭제 성공");
                }
                default -> {
                    return ScheduleOutcome.fail(
                        "UNKNOWN_EVENT_TYPE",
                        "알 수 없는 EventType: " + eventType,
                        eventType
                    );
                }
            }
        } catch (JsonProcessingException e) {
            return ScheduleOutcome.fail(
                "PAYLOAD_PARSE_ERROR",
                "Payload 파싱 실패: " + e.getMessage(),
                e.getClass().getName()
            );
        } catch (Exception e) {
            return ScheduleOutcome.fail(
                "EVENTBRIDGE_API_ERROR",
                "EventBridge API 호출 실패: " + e.getMessage(),
                e.getClass().getName()
            );
        }
    }
}
