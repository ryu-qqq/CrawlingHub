package com.ryuqq.crawlinghub.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.integration.base.WebApiIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Health check integration test for Web API. Verifies that the application starts correctly with
 * all dependencies.
 */
class HealthCheckIntegrationTest extends WebApiIntegrationTest {

    @Test
    @DisplayName("Actuator health endpoint should return UP status")
    void healthEndpoint_shouldReturnUp() {
        // when
        ResponseEntity<String> response =
                restTemplate.getForEntity(url("/actuator/health"), String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Application should start with TestContainers")
    void applicationContext_shouldLoadSuccessfully() {
        // This test verifies that:
        // 1. MySQL container is running and connected
        // 2. Redis container is running and connected
        // 3. LocalStack (SQS) container is running
        // 4. Flyway migrations are applied
        // 5. Spring context loads successfully

        assertThat(MYSQL_CONTAINER.isRunning()).isTrue();
        assertThat(REDIS_CONTAINER.isRunning()).isTrue();
        assertThat(LOCALSTACK_CONTAINER.isRunning()).isTrue();
    }

    @Test
    @DisplayName("WireMock servers should be available")
    void wireMockServers_shouldBeAvailable() {
        assertThat(marketplaceWireMock.isRunning()).isTrue();
    }
}
