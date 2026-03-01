package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.List;
import java.util.Map;

public record ProductSyncFailureSummaryApiResponse(
        Map<String, Long> failureCountsBySyncType,
        List<FailureDetailApiResponse> recentFailures,
        long totalFailures,
        long totalPending) {

    public record FailureDetailApiResponse(String syncType, String errorMessage, long count) {}
}
