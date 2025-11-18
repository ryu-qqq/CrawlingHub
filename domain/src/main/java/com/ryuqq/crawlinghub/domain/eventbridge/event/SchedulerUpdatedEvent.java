package com.ryuqq.crawlinghub.domain.eventbridge.event;

import java.time.LocalDateTime;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public record SchedulerUpdatedEvent(
    Long schedulerId,
    String schedulerName,
    CronExpression cronExpression,
    SchedulerStatus previousStatus,
    SchedulerStatus currentStatus,
    LocalDateTime occurredAt
) implements DomainEvent {
}

