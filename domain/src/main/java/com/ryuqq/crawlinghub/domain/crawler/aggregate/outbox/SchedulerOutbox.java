package com.ryuqq.crawlinghub.domain.crawler.aggregate.outbox;

import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxStatus;

import java.time.LocalDateTime;

/**
 * SchedulerOutbox - EventBridge 스케줄러 Outbox Aggregate Root
 *
 * <p>EventBridge Scheduler로 전송할 이벤트를 관리하는 Outbox 패턴 구현입니다.</p>
 *
 * <p><strong>핵심 책임:</strong></p>
 * <ul>
 *   <li>✅ 스케줄러 이벤트 전송 관리 (WAITING → SENT/FAILED)</li>
 *   <li>✅ Payload JSON 형식 검증</li>
 *   <li>✅ 재시도 카운트 추적 (retryCount)</li>
 * </ul>
 *
 * <p><strong>상태 전환:</strong></p>
 * <pre>
 * WAITING (생성 직후)
 *    ↓ markAsSent()
 * SENT (전송 성공)
 *
 * WAITING (생성 직후)
 *    ↓ markAsFailed()
 * FAILED (전송 실패, 재시도 가능)
 * </pre>
 *
 * <p><strong>Zero-Tolerance 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Lombok 금지</li>
 *   <li>✅ Law of Demeter 준수</li>
 *   <li>✅ Private Constructor + Factory Method</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SchedulerOutbox {

    private final SchedulerOutboxId outboxId;
    private final ScheduleId scheduleId;
    private final SchedulerOutboxEventType eventType;
    private final String payload;
    private SchedulerOutboxStatus status;
    private Integer retryCount;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Private Constructor - Factory Method 패턴
     *
     * @param scheduleId 스케줄 ID
     * @param eventType 이벤트 타입
     * @param payload JSON payload
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private SchedulerOutbox(ScheduleId scheduleId, SchedulerOutboxEventType eventType, String payload) {
        validatePayload(payload);
        this.outboxId = SchedulerOutboxId.generate();
        this.scheduleId = scheduleId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = SchedulerOutboxStatus.WAITING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * SchedulerOutbox 생성 Factory Method
     *
     * <p>초기 상태: WAITING</p>
     * <p>retryCount: 0</p>
     *
     * @param scheduleId 스케줄 ID
     * @param eventType 이벤트 타입
     * @param payload JSON payload
     * @return 생성된 SchedulerOutbox
     * @throws IllegalArgumentException payload가 유효한 JSON 형식이 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox create(ScheduleId scheduleId, SchedulerOutboxEventType eventType, String payload) {
        return new SchedulerOutbox(scheduleId, eventType, payload);
    }

    /**
     * Payload JSON 형식 검증
     *
     * <p>간단한 JSON 검증:</p>
     * <ul>
     *   <li>✅ null이 아님</li>
     *   <li>✅ '{'로 시작</li>
     *   <li>✅ '}'로 종료</li>
     * </ul>
     *
     * @param payload JSON payload
     * @throws IllegalArgumentException payload가 유효한 JSON 형식이 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    private void validatePayload(String payload) {
        if (payload == null || !payload.trim().startsWith("{") || !payload.trim().endsWith("}")) {
            throw new IllegalArgumentException("Payload는 유효한 JSON 형식이어야 합니다");
        }
    }

    // ===== Getters =====

    public SchedulerOutboxId getOutboxId() {
        return outboxId;
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public SchedulerOutboxEventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public SchedulerOutboxStatus getStatus() {
        return status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
