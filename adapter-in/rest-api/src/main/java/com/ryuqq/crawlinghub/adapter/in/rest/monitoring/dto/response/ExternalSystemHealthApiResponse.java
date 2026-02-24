package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response;

import java.util.List;

public record ExternalSystemHealthApiResponse(List<SystemHealthApiResponse> systems) {

    public record SystemHealthApiResponse(String system, long recentFailures, String status) {}
}
