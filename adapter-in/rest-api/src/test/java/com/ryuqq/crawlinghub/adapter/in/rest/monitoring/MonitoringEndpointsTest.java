package com.ryuqq.crawlinghub.adapter.in.rest.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * MonitoringEndpoints 단위 테스트
 *
 * <p>모니터링 도메인 엔드포인트 상수값을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("MonitoringEndpoints 단위 테스트")
class MonitoringEndpointsTest {

    @Test
    @DisplayName("DASHBOARD 상수가 올바른 경로이다")
    void shouldHaveCorrectDashboardConstant() {
        assertThat(MonitoringEndpoints.DASHBOARD).isEqualTo("/api/v1/monitoring/dashboard");
    }

    @Test
    @DisplayName("CRAWL_TASKS_SUMMARY 상수가 올바른 경로이다")
    void shouldHaveCorrectCrawlTasksSummaryConstant() {
        assertThat(MonitoringEndpoints.CRAWL_TASKS_SUMMARY)
                .isEqualTo("/api/v1/monitoring/crawl-tasks/summary");
    }

    @Test
    @DisplayName("OUTBOX_SUMMARY 상수가 올바른 경로이다")
    void shouldHaveCorrectOutboxSummaryConstant() {
        assertThat(MonitoringEndpoints.OUTBOX_SUMMARY)
                .isEqualTo("/api/v1/monitoring/outbox/summary");
    }

    @Test
    @DisplayName("CRAWLED_RAW_SUMMARY 상수가 올바른 경로이다")
    void shouldHaveCorrectCrawledRawSummaryConstant() {
        assertThat(MonitoringEndpoints.CRAWLED_RAW_SUMMARY)
                .isEqualTo("/api/v1/monitoring/crawled-raw/summary");
    }

    @Test
    @DisplayName("EXTERNAL_SYSTEMS_HEALTH 상수가 올바른 경로이다")
    void shouldHaveCorrectExternalSystemsHealthConstant() {
        assertThat(MonitoringEndpoints.EXTERNAL_SYSTEMS_HEALTH)
                .isEqualTo("/api/v1/monitoring/external-systems/health");
    }
}
