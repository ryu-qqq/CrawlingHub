package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.Map;

public record CrawlExecutionSummaryApiResponse(
        Map<String, Long> countsByStatus, long totalExecutions, double successRate) {}
