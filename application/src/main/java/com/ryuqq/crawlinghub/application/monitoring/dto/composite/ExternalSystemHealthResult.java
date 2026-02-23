package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import java.util.List;

public record ExternalSystemHealthResult(List<SystemHealth> systems) {

    public record SystemHealth(String system, long recentFailures, String status) {}
}
