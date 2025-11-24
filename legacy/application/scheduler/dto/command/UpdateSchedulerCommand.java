package com.ryuqq.crawlinghub.application.schedule.dto.command;

import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public record UpdateSchedulerCommand(
        Long schedulerId, String schedulerName, String cronExpression, SchedulerStatus status) {}
