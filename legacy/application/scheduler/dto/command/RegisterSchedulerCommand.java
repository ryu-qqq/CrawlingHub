package com.ryuqq.crawlinghub.application.schedule.dto.command;

public record RegisterSchedulerCommand(
        Long sellerId, String schedulerName, String cronExpression) {}
