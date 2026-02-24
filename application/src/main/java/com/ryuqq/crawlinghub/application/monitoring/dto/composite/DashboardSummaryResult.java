package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

public record DashboardSummaryResult(
        long activeSchedulers,
        long runningTasks,
        long pendingOutbox,
        long recentErrors,
        SystemStatus overallStatus) {

    public enum SystemStatus {
        HEALTHY,
        WARNING,
        CRITICAL
    }
}
