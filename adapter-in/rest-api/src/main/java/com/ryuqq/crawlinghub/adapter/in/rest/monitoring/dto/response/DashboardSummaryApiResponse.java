package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

public record DashboardSummaryApiResponse(
        long activeSchedulers,
        long runningTasks,
        long pendingOutbox,
        long recentErrors,
        String overallStatus) {}
