package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto;

public record DashboardCountsDto(
        long activeSchedulers, long runningTasks, long pendingOutbox, long recentErrors) {}
