package com.ryuqq.crawlinghub.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.integration.base.WebApiIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.AuthTestHelper;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Monitoring API 통합 테스트
 *
 * <p>Monitoring API의 전체 동작을 검증하는 통합 테스트입니다.
 *
 * <p><strong>테스트 대상 엔드포인트:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/monitoring/dashboard - 대시보드 요약 조회
 *   <li>GET /api/v1/monitoring/crawl-tasks/summary - 크롤 태스크 요약 조회
 *   <li>GET /api/v1/monitoring/outbox/summary - 아웃박스 요약 조회
 *   <li>GET /api/v1/monitoring/crawled-raw/summary - 크롤 원시 데이터 요약 조회
 *   <li>GET /api/v1/monitoring/external-systems/health - 외부 시스템 헬스 조회
 * </ul>
 *
 * @since 1.0.0
 */
@DisplayName("Monitoring API 통합 테스트")
class MonitoringIntegrationTest extends WebApiIntegrationTest {

    private static final String MONITORING_BASE_URL = "/api/v1/monitoring";
    private static final String DASHBOARD_URL = MONITORING_BASE_URL + "/dashboard";
    private static final String CRAWL_TASKS_SUMMARY_URL =
            MONITORING_BASE_URL + "/crawl-tasks/summary";
    private static final String OUTBOX_SUMMARY_URL = MONITORING_BASE_URL + "/outbox/summary";
    private static final String CRAWLED_RAW_SUMMARY_URL =
            MONITORING_BASE_URL + "/crawled-raw/summary";
    private static final String EXTERNAL_SYSTEMS_HEALTH_URL =
            MONITORING_BASE_URL + "/external-systems/health";

    @Autowired private TestDataHelper testDataHelper;

    @Nested
    @DisplayName("GET /api/v1/monitoring/dashboard - 대시보드 요약 조회")
    class GetDashboardSummary {

        @Test
        @DisplayName("데이터 없는 상태에서 대시보드 요약 조회 성공 - HEALTHY 상태 반환")
        void shouldReturnDashboardSummaryWithEmptyData() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("activeSchedulers")).isEqualTo(0);
            assertThat(data.get("runningTasks")).isEqualTo(0);
            assertThat(data.get("pendingOutbox")).isEqualTo(0);
            assertThat(data.get("recentErrors")).isEqualTo(0);
            assertThat(data.get("overallStatus")).isEqualTo("HEALTHY");
        }

        @Test
        @DisplayName("스케줄러/태스크 데이터 존재 시 대시보드 요약 조회 성공")
        void shouldReturnDashboardSummaryWithData() {
            // given
            testDataHelper.insertTaskTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat((Integer) data.get("activeSchedulers")).isGreaterThanOrEqualTo(0);
            assertThat((Integer) data.get("runningTasks")).isGreaterThanOrEqualTo(0);
            assertThat(data.get("overallStatus")).isIn("HEALTHY", "WARNING", "CRITICAL");
        }

        @Test
        @DisplayName("lookbackMinutes 파라미터 지정 시 대시보드 요약 조회 성공")
        void shouldReturnDashboardSummaryWithCustomLookback() {
            // given
            testDataHelper.insertTaskTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL + "?lookbackMinutes=30"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("overallStatus")).isIn("HEALTHY", "WARNING", "CRITICAL");
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/monitoring/crawl-tasks/summary - 크롤 태스크 요약 조회")
    class GetCrawlTaskSummary {

        @Test
        @DisplayName("데이터 없는 상태에서 크롤 태스크 요약 조회 성공 - 빈 상태 반환")
        void shouldReturnEmptyCrawlTaskSummaryWithNoData() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWL_TASKS_SUMMARY_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("totalTasks")).isEqualTo(0);
            assertThat(data.get("stuckTasks")).isEqualTo(0);
        }

        @Test
        @DisplayName("태스크 데이터 존재 시 상태별 카운트 반환")
        void shouldReturnCrawlTaskSummaryWithData() {
            // given
            testDataHelper.insertTaskTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWL_TASKS_SUMMARY_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> countsByStatus = (Map<String, Object>) data.get("countsByStatus");
            assertThat(countsByStatus).isNotNull();
            // TestDataHelper.insertTasks() 기준: PUBLISHED(4), SUCCESS(1), FAILED(1), TIMEOUT(1),
            // WAITING(1)
            assertThat((Integer) data.get("totalTasks")).isEqualTo(8);
        }

        @Test
        @DisplayName("lookbackMinutes 파라미터 지정 시 크롤 태스크 요약 조회 성공")
        void shouldReturnCrawlTaskSummaryWithCustomLookback() {
            // given
            testDataHelper.insertTaskTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWL_TASKS_SUMMARY_URL + "?lookbackMinutes=120"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat((Integer) data.get("totalTasks")).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWL_TASKS_SUMMARY_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/monitoring/outbox/summary - 아웃박스 요약 조회")
    class GetOutboxSummary {

        @Test
        @DisplayName("데이터 없는 상태에서 아웃박스 요약 조회 성공 - 각 아웃박스 총합 0 반환")
        void shouldReturnEmptyOutboxSummaryWithNoData() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(OUTBOX_SUMMARY_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> crawlTaskOutbox = (Map<String, Object>) data.get("crawlTaskOutbox");
            assertThat(crawlTaskOutbox).isNotNull();
            assertThat(crawlTaskOutbox.get("total")).isEqualTo(0);

            @SuppressWarnings("unchecked")
            Map<String, Object> schedulerOutbox = (Map<String, Object>) data.get("schedulerOutbox");
            assertThat(schedulerOutbox).isNotNull();
            assertThat(schedulerOutbox.get("total")).isEqualTo(0);

            @SuppressWarnings("unchecked")
            Map<String, Object> productSyncOutbox =
                    (Map<String, Object>) data.get("productSyncOutbox");
            assertThat(productSyncOutbox).isNotNull();
            assertThat(productSyncOutbox.get("total")).isEqualTo(0);
        }

        @Test
        @DisplayName("아웃박스 데이터 존재 시 타입별 아웃박스 상태 반환")
        void shouldReturnOutboxSummaryWithData() {
            // given
            testDataHelper.insertProductOutboxTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(OUTBOX_SUMMARY_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> productSyncOutbox =
                    (Map<String, Object>) data.get("productSyncOutbox");
            assertThat(productSyncOutbox).isNotNull();
            // TestDataHelper.insertProductSyncOutbox() 기준: 5개 레코드
            assertThat((Integer) productSyncOutbox.get("total")).isEqualTo(5);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(OUTBOX_SUMMARY_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/monitoring/crawled-raw/summary - 크롤 원시 데이터 요약 조회")
    class GetCrawledRawSummary {

        @Test
        @DisplayName("데이터 없는 상태에서 크롤 원시 데이터 요약 조회 성공 - 총합 0 반환")
        void shouldReturnEmptyCrawledRawSummaryWithNoData() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWLED_RAW_SUMMARY_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("totalRaw")).isEqualTo(0);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(CRAWLED_RAW_SUMMARY_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/monitoring/external-systems/health - 외부 시스템 헬스 조회")
    class GetExternalSystemHealth {

        @Test
        @DisplayName("데이터 없는 상태에서 외부 시스템 헬스 조회 성공 - 모든 시스템 HEALTHY 반환")
        void shouldReturnHealthyStatusWithNoFailures() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXTERNAL_SYSTEMS_HEALTH_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> systems = (List<Map<String, Object>>) data.get("systems");
            assertThat(systems).isNotNull();
            // 시스템 수: CRAWL_TASK, CRAWL_TASK_OUTBOX, SCHEDULER_OUTBOX, PRODUCT_SYNC_OUTBOX
            assertThat(systems).hasSize(4);
            systems.forEach(system -> assertThat(system.get("status")).isEqualTo("HEALTHY"));
        }

        @Test
        @DisplayName("태스크 및 아웃박스 데이터 존재 시 외부 시스템 헬스 반환")
        void shouldReturnExternalSystemHealthWithData() {
            // given
            testDataHelper.insertProductOutboxTestData();
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXTERNAL_SYSTEMS_HEALTH_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> systems = (List<Map<String, Object>>) data.get("systems");
            assertThat(systems).hasSize(4);

            // 각 시스템 응답 필드 검증
            systems.forEach(
                    system -> {
                        assertThat(system.get("system")).isNotNull();
                        assertThat(system.get("recentFailures")).isNotNull();
                        assertThat(system.get("status")).isIn("HEALTHY", "WARNING", "CRITICAL");
                    });
        }

        @Test
        @DisplayName("lookbackMinutes 파라미터 지정 시 외부 시스템 헬스 조회 성공")
        void shouldReturnExternalSystemHealthWithCustomLookback() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXTERNAL_SYSTEMS_HEALTH_URL + "?lookbackMinutes=30"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> systems = (List<Map<String, Object>>) data.get("systems");
            assertThat(systems).hasSize(4);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXTERNAL_SYSTEMS_HEALTH_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("공통 - lookbackMinutes 파라미터 경계값 테스트")
    class LookbackMinutesValidation {

        @Test
        @DisplayName("lookbackMinutes=0 지정 시 기본값(60분)으로 대체되어 정상 응답")
        void shouldUseDefaultLookbackWhenZero() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when - 0 이하 값은 기본 60분으로 대체
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL + "?lookbackMinutes=0"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("lookbackMinutes 음수 지정 시 기본값(60분)으로 대체되어 정상 응답")
        void shouldUseDefaultLookbackWhenNegative() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when - 0 이하 값은 기본 60분으로 대체
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(DASHBOARD_URL + "?lookbackMinutes=-10"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
