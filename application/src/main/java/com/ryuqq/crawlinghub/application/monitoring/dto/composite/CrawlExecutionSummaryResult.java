package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.Map;

public record CrawlExecutionSummaryResult(
        Map<String, Long> countsByStatus, long totalExecutions, double successRate) {

    public CrawlExecutionSummaryResult {
        countsByStatus = Map.copyOf(countsByStatus);
    }
}
