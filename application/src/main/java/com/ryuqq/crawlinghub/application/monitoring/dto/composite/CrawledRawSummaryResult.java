package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.Map;

public record CrawledRawSummaryResult(Map<String, Long> countsByStatus, long totalRaw) {
    public CrawledRawSummaryResult {
        countsByStatus = Map.copyOf(countsByStatus);
    }
}
