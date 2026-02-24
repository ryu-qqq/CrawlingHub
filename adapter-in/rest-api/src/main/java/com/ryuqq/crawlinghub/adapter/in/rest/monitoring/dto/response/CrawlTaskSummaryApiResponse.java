package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.Map;

public record CrawlTaskSummaryApiResponse(
        Map<String, Long> countsByStatus, long stuckTasks, long totalTasks) {}
