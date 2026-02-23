package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.Map;

public record OutboxSummaryApiResponse(
        OutboxDetailApiResponse crawlTaskOutbox,
        OutboxDetailApiResponse schedulerOutbox,
        OutboxDetailApiResponse productSyncOutbox) {

    public record OutboxDetailApiResponse(Map<String, Long> countsByStatus, long total) {}
}
