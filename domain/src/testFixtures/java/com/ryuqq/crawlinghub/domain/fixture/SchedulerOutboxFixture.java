package com.ryuqq.crawlinghub.domain.fixture;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.outbox.SchedulerOutbox;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.crawler.vo.SchedulerOutboxEventType;

/**
 * SchedulerOutbox TestFixture
 *
 * <p><strong>제공 메서드:</strong></p>
 * <ul>
 *   <li>{@link #waitingOutbox()} - WAITING 상태의 SchedulerOutbox</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
public class SchedulerOutboxFixture {

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
}
