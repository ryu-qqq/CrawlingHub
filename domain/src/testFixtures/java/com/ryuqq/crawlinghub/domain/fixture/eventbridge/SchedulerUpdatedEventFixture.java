package com.ryuqq.crawlinghub.domain.fixture.eventbridge;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.eventbridge.event.SchedulerUpdatedEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public final class SchedulerUpdatedEventFixture {

    private SchedulerUpdatedEventFixture() {
    }

    public static SchedulerUpdatedEvent forNew() {
        return anUpdatedEvent();
    }

    public static SchedulerUpdatedEvent of() {
        return anUpdatedEvent();
    }

    public static SchedulerUpdatedEvent reconstitute() {
        return anUpdatedEvent();
    }

    public static SchedulerUpdatedEvent anUpdatedEvent() {
        return new SchedulerUpdatedEvent(
            aSchedulerId(),
            anUpdatedSchedulerName(),
            anUpdatedCronExpression(),
            aPreviousStatus(),
            aCurrentStatus(),
            occurredAt()
        );
    }

    public static Long aSchedulerId() {
        return 1001L;
    }

    public static String anUpdatedSchedulerName() {
        return "updated-scheduler-name";
    }

    public static CronExpression anUpdatedCronExpression() {
        return CronExpression.of("cron(0 9 * * ? *)");
    }

    public static SchedulerStatus aPreviousStatus() {
        return SchedulerStatus.PENDING;
    }

    public static SchedulerStatus aCurrentStatus() {
        return SchedulerStatus.ACTIVE;
    }

    public static LocalDateTime occurredAt() {
        return LocalDateTime.of(2025, 11, 30, 12, 0);
    }
}

