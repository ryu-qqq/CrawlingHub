package com.ryuqq.crawlinghub.domain.eventbridge.aggregate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ryuqq.crawlinghub.domain.common.DomainEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.event.SchedulerUpdatedEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

/**
 * CrawlingScheduler Aggregate Root
 */
public final class CrawlingScheduler {

    private final Long schedulerId;
    private final Long sellerId;
    private String schedulerName;
    private CronExpression cronExpression;
    private final LocalDateTime createdAt;
    private final Clock clock;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private SchedulerStatus status;
    private LocalDateTime updatedAt;

    private CrawlingScheduler(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Clock clock
    ) {
        this.schedulerId = schedulerId;
        this.sellerId = sellerId;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.clock = clock;
    }

    public static CrawlingScheduler forNew(
        Long sellerId,
        String schedulerName,
        CronExpression cronExpression,
        Clock clock
    ) {
        Objects.requireNonNull(sellerId, "sellerId must not be null");
        Objects.requireNonNull(schedulerName, "schedulerName must not be null");
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        Objects.requireNonNull(clock, "clock must not be null");

        LocalDateTime now = LocalDateTime.now(clock);
        return new CrawlingScheduler(
            null,
            sellerId,
            schedulerName,
            cronExpression,
            SchedulerStatus.PENDING,
            now,
            now,
            clock
        );
    }

    public static CrawlingScheduler of(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status,
        Clock clock
    ) {
        Objects.requireNonNull(schedulerId, "schedulerId must not be null");
        Objects.requireNonNull(status, "status must not be null");

        LocalDateTime now = LocalDateTime.now(clock);
        return new CrawlingScheduler(
            schedulerId,
            sellerId,
            schedulerName,
            cronExpression,
            status,
            now,
            now,
            clock
        );
    }

    public static CrawlingScheduler reconstitute(
        Long schedulerId,
        Long sellerId,
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Clock clock
    ) {
        return new CrawlingScheduler(
            schedulerId,
            sellerId,
            schedulerName,
            cronExpression,
            status,
            createdAt,
            updatedAt,
            clock
        );
    }

    public Long getSchedulerId() {
        return schedulerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public SchedulerStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Clock getClock() {
        return clock;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public SchedulerUpdatedEvent update(
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus status
    ) {
        Objects.requireNonNull(schedulerName, "schedulerName must not be null");
        Objects.requireNonNull(cronExpression, "cronExpression must not be null");
        Objects.requireNonNull(status, "status must not be null");

        boolean nameChanged = !Objects.equals(this.schedulerName, schedulerName);
        boolean cronChanged = !Objects.equals(this.cronExpression, cronExpression);
        boolean statusChanged = this.status != status;

        if (!nameChanged && !cronChanged && !statusChanged) {
            return null;
        }

        SchedulerStatus previousStatus = this.status;
        this.schedulerName = schedulerName;
        this.cronExpression = cronExpression;
        this.status = status;
        this.updatedAt = LocalDateTime.now(clock);

        return publishSchedulerUpdatedEvent(
            schedulerName,
            cronExpression,
            previousStatus,
            status,
            updatedAt
        );
    }

    private SchedulerUpdatedEvent publishSchedulerUpdatedEvent(
        String schedulerName,
        CronExpression cronExpression,
        SchedulerStatus previousStatus,
        SchedulerStatus currentStatus,
        LocalDateTime occurredAt
    ) {
        SchedulerUpdatedEvent event = new SchedulerUpdatedEvent(
            schedulerId,
            schedulerName,
            cronExpression,
            previousStatus,
            currentStatus,
            occurredAt
        );
        this.domainEvents.add(event);
        return event;
    }
}

