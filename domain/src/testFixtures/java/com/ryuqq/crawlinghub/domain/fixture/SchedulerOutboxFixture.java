package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.outbox.SchedulerOutbox;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;

/**
 * SchedulerOutbox TestFixture
 *
 * <p><strong>표준 메서드:</strong></p>
 * <ul>
 *   <li>{@link #forNew()} - 새 SchedulerOutbox 생성 (표준 패턴)</li>
 *   <li>{@link #of()} - 기본 SchedulerOutbox 생성 (표준 패턴)</li>
 *   <li>{@link #reconstitute} - DB에서 복원 (표준 패턴)</li>
 * </ul>
 *
 * <p><strong>헬퍼 메서드:</strong></p>
 * <ul>
 *   <li>{@link #waitingOutbox()} - WAITING 상태의 SchedulerOutbox</li>
 *   <li>{@link #sendingOutbox()} - SENDING 상태의 SchedulerOutbox</li>
 *   <li>{@link #failedOutboxWithRetryCount(int)} - FAILED 상태의 SchedulerOutbox (retryCount 지정)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SchedulerOutboxFixture {

    /**
     * 새로운 SchedulerOutbox 생성 (표준 패턴)
     *
     * <p>Aggregate의 create() Factory Method를 호출하여 새 Aggregate를 생성합니다.</p>
     *
     * @return 새로 생성된 SchedulerOutbox
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox forNew() {
        return waitingOutbox();
    }

    /**
     * 기본 SchedulerOutbox 생성 (표준 패턴)
     *
     * <p>가장 기본적인 SchedulerOutbox를 반환합니다.</p>
     *
     * @return 기본 SchedulerOutbox
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox of() {
        return waitingOutbox();
    }

    /**
     * DB에서 복원된 SchedulerOutbox 생성 (표준 패턴)
     *
     * <p>Aggregate의 reconstitute() 메서드를 호출하여 영속화된 상태를 복원합니다.</p>
     *
     * @param outboxId Outbox ID
     * @param scheduleId 스케줄 ID
     * @param eventType 이벤트 타입
     * @param payload JSON payload
     * @param status Outbox 상태
     * @param retryCount 재시도 횟수
     * @param errorMessage 에러 메시지
     * @param createdAt 생성 일시
     * @param updatedAt 수정 일시
     * @return 복원된 SchedulerOutbox
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox reconstitute(
            com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxId outboxId,
            ScheduleId scheduleId,
            com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType eventType,
            String payload,
            com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxStatus status,
            Integer retryCount,
            String errorMessage,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt
    ) {
        return SchedulerOutbox.reconstitute(
                outboxId,
                scheduleId,
                eventType,
                payload,
                status,
                retryCount,
                errorMessage,
                createdAt,
                updatedAt
        );
    }

    /**
     * WAITING 상태의 SchedulerOutbox 생성
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>EventType: SCHEDULE_REGISTERED</li>
     *   <li>Status: WAITING</li>
     *   <li>RetryCount: 0</li>
     *   <li>Payload: mustit-crawler-seller_12345 스케줄 등록 요청</li>
     * </ul>
     *
     * @return WAITING 상태의 SchedulerOutbox
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox waitingOutbox() {
        ScheduleId scheduleId = ScheduleId.generate();
        SchedulerOutboxEventType eventType = SchedulerOutboxEventType.SCHEDULE_REGISTERED;
        String payload = "{\"ruleName\":\"mustit-crawler-seller_12345\",\"scheduleExpression\":\"rate(1 day)\"}";
        return SchedulerOutbox.create(scheduleId, eventType, payload);
    }

    /**
     * SENDING 상태의 SchedulerOutbox 생성
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>EventType: SCHEDULE_REGISTERED</li>
     *   <li>Status: SENDING</li>
     *   <li>RetryCount: 0</li>
     *   <li>Payload: mustit-crawler-seller_12345 스케줄 등록 요청</li>
     * </ul>
     *
     * @return SENDING 상태의 SchedulerOutbox
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox sendingOutbox() {
        SchedulerOutbox outbox = waitingOutbox();
        outbox.send();
        return outbox;
    }

    /**
     * FAILED 상태의 SchedulerOutbox 생성 (retryCount 지정)
     *
     * <p><strong>동작:</strong></p>
     * <ul>
     *   <li>WAITING → SENDING → FAILED 전환을 retryCount만큼 반복</li>
     *   <li>각 실패 후 canRetry() 확인 후 retry() 호출</li>
     *   <li>최종적으로 FAILED 상태로 반환</li>
     * </ul>
     *
     * <p><strong>설정:</strong></p>
     * <ul>
     *   <li>EventType: SCHEDULE_REGISTERED</li>
     *   <li>Status: FAILED</li>
     *   <li>RetryCount: 지정된 값</li>
     * </ul>
     *
     * @param retryCount 재시도 횟수
     * @return FAILED 상태의 SchedulerOutbox (retryCount 지정)
     * @author ryu-qqq
     * @since 2025-11-17
     */
    public static SchedulerOutbox failedOutboxWithRetryCount(int retryCount) {
        SchedulerOutbox outbox = sendingOutbox();
        for (int i = 0; i < retryCount; i++) {
            outbox.fail("Test error " + i);
            if (outbox.canRetry()) {
                outbox.retry();
                outbox.send();
            }
        }
        // 마지막 fail로 FAILED 상태로 만들기
        if (outbox.canRetry()) {
            outbox.fail("Final test error");
        }
        return outbox;
    }
}
