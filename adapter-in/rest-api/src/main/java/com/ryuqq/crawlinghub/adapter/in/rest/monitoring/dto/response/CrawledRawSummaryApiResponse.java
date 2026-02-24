package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.Map;

public record CrawledRawSummaryApiResponse(Map<String, Long> countsByStatus, long totalRaw) {}
