package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.List;
import java.util.Map;

public record ProductSyncFailureSummaryResult(
        Map<String, Long> failureCountsBySyncType,
        List<FailureDetail> recentFailures,
        long totalFailures,
        long totalPending) {

    public ProductSyncFailureSummaryResult {
        failureCountsBySyncType = Map.copyOf(failureCountsBySyncType);
        recentFailures = List.copyOf(recentFailures);
    }

    public record FailureDetail(String syncType, String errorMessage, long count) {}
}
