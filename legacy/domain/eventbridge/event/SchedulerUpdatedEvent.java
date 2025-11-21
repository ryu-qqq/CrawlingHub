package com.ryuqq.crawlinghub.domain.eventbridge.event;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;

public record SchedulerUpdatedEvent(
        Long schedulerId,
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus previousStatus,
        SchedulerStatus currentStatus,
        LocalDateTime occurredAt)
        implements DomainEvent {}
