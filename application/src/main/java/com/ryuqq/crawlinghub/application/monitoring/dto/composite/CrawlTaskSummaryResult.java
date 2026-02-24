package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.Map;

public record CrawlTaskSummaryResult(
        Map<String, Long> countsByStatus, long stuckTasks, long totalTasks) {
    public CrawlTaskSummaryResult {
        countsByStatus = Map.copyOf(countsByStatus);
    }
}
