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
 *   <li>✅ 스케줄러 이벤트 전송 관리 (WAITING → SENDING → COMPLETED/FAILED)</li>
 *   <li>✅ Payload JSON 형식 검증</li>
 *   <li>✅ 재시도 카운트 추적 (retryCount)</li>
 * </ul>
 *
 * <p><strong>상태 전환:</strong></p>
 * <pre>
 * WAITING (생성 직후)
 *    ↓ send()
 * SENDING (전송 중)
 *    ↓ complete() / fail()
 * COMPLETED (성공) 또는 FAILED (실패)
 *    ↓ retry() (FAILED인 경우)
 * WAITING (재시도)
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

    private static final int MAX_RETRY_COUNT = 5;

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

    // ===== Business Methods =====

    /**
     * 전송 시작 (WAITING → SENDING)
     *
     * <p><strong>전환 조건:</strong></p>
     * <ul>
     *   <li>✅ WAITING 상태에서만 전송 가능</li>
     *   <li>✅ SENDING 상태로 전환</li>
     *   <li>✅ updatedAt 타임스탬프 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException WAITING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void send() {
        if (status != SchedulerOutboxStatus.WAITING) {
            throw new IllegalStateException("WAITING 상태에서만 전송할 수 있습니다");
        }
        this.status = SchedulerOutboxStatus.SENDING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 전송 완료 (SENDING → COMPLETED)
     *
     * <p><strong>완료 조건:</strong></p>
     * <ul>
     *   <li>✅ SENDING 상태에서만 완료 가능</li>
     *   <li>✅ COMPLETED 상태로 전환</li>
     *   <li>✅ updatedAt 타임스탬프 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException SENDING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void complete() {
        if (status != SchedulerOutboxStatus.SENDING) {
            throw new IllegalStateException("SENDING 상태에서만 완료할 수 있습니다");
        }
        this.status = SchedulerOutboxStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 전송 실패 (SENDING → FAILED)
     *
     * <p><strong>실패 처리:</strong></p>
     * <ul>
     *   <li>✅ SENDING 상태에서만 실패 가능</li>
     *   <li>✅ FAILED 상태로 전환</li>
     *   <li>✅ errorMessage 저장</li>
     *   <li>✅ retryCount 증가</li>
     *   <li>✅ updatedAt 타임스탬프 갱신</li>
     * </ul>
     *
     * @param errorMessage 에러 메시지
     * @throws IllegalStateException SENDING 상태가 아닐 때
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void fail(String errorMessage) {
        if (status != SchedulerOutboxStatus.SENDING) {
            throw new IllegalStateException("SENDING 상태에서만 실패할 수 있습니다");
        }
        this.status = SchedulerOutboxStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재시도 가능 여부 판단 (Tell Don't Ask)
     *
     * <p><strong>재시도 조건:</strong></p>
     * <ul>
     *   <li>✅ retryCount < MAX_RETRY_COUNT (5회)</li>
     * </ul>
     *
     * <p><strong>Tell Don't Ask 패턴:</strong></p>
     * <ul>
     *   <li>❌ Bad: if (outbox.getRetryCount() < 5) { outbox.retry(); }</li>
     *   <li>✅ Good: if (outbox.canRetry()) { outbox.retry(); }</li>
     * </ul>
     *
     * @return 재시도 가능 여부
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public boolean canRetry() {
        return retryCount < MAX_RETRY_COUNT;
    }

    /**
     * 재시도 (FAILED → WAITING)
     *
     * <p><strong>재시도 조건:</strong></p>
     * <ul>
     *   <li>✅ FAILED 상태에서만 재시도 가능</li>
     *   <li>✅ canRetry() = true (최대 재시도 횟수 미초과)</li>
     *   <li>✅ WAITING 상태로 전환</li>
     *   <li>✅ errorMessage 초기화</li>
     *   <li>✅ updatedAt 타임스탬프 갱신</li>
     * </ul>
     *
     * @throws IllegalStateException FAILED 상태가 아닐 때 또는 최대 재시도 횟수 초과 시
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public void retry() {
        if (status != SchedulerOutboxStatus.FAILED) {
            throw new IllegalStateException("FAILED 상태에서만 재시도할 수 있습니다");
        }
        if (!canRetry()) {
            throw new IllegalStateException("최대 재시도 횟수(" + MAX_RETRY_COUNT + ")를 초과했습니다");
        }
        this.status = SchedulerOutboxStatus.WAITING;
        this.errorMessage = null;
        this.updatedAt = LocalDateTime.now();
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
