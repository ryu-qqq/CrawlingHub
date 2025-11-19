package com.ryuqq.crawlinghub.application.scheduler.dto.command;

public record RegisterSchedulerCommand(
        Long sellerId,
        String schedulerName,
        String cronExpression
) {
}

