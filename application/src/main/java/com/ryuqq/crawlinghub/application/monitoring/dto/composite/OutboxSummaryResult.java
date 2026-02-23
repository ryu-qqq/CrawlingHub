package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.Map;

public record OutboxSummaryResult(
        OutboxDetail crawlTaskOutbox,
        OutboxDetail schedulerOutbox,
        OutboxDetail productSyncOutbox) {

    public record OutboxDetail(Map<String, Long> countsByStatus, long total) {
        public OutboxDetail {
            countsByStatus = Map.copyOf(countsByStatus);
        }
    }
}
