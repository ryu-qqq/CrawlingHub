package com.ryuqq.crawlinghub.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.integration.base.WebApiIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.AuthTestHelper;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * CrawlScheduler API 통합 테스트
 *
 * <p>테스트 시나리오:
 *
 * <ul>
 *   <li>GET /api/v1/crawling/schedules - 스케줄러 목록 조회 (페이징, 필터링)
 *   <li>POST /api/v1/crawling/schedules - 스케줄러 등록
 *   <li>PATCH /api/v1/crawling/schedules/{id} - 스케줄러 수정
 *   <li>POST /api/v1/crawling/schedules/{id}/trigger - 수동 트리거
 *   <li>인증 검증 (401)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
class CrawlSchedulerIntegrationTest extends WebApiIntegrationTest {

    private static final String SCHEDULES_BASE_URL = "/api/v1/crawling/schedules";

    @Autowired private TestDataHelper testDataHelper;

    @BeforeEach
    void setUpTestData() {
        // Seller와 Scheduler 데이터 삽입
        testDataHelper.insertSellers();
        testDataHelper.insertSchedulers();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/schedules - 목록 조회")
    class ListCrawlSchedulers {

        @Test
        @DisplayName("인증된 사용자는 스케줄러 목록을 조회할 수 있다")
        void shouldReturnSchedulerList() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
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
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).isNotEmpty();
        }

        @Test
        @DisplayName("셀러 ID 필터로 해당 셀러의 스케줄러만 조회된다")
        void shouldFilterBySellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?sellerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 셀러 1의 스케줄러만 조회됨 (scheduler 1, 2)
            content.forEach(
                    scheduler ->
                            assertThat(((Number) scheduler.get("sellerId")).longValue())
                                    .isEqualTo(1L));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 스케줄러만 조회된다")
        void shouldFilterByStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?statuses=ACTIVE"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(scheduler -> assertThat(scheduler.get("status")).isEqualTo("ACTIVE"));
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?page=0&size=2"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            assertThat(((Number) data.get("page")).intValue()).isEqualTo(0);
            assertThat(((Number) data.get("size")).intValue()).isEqualTo(2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content.size()).isLessThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/schedules - 스케줄러 등록")
    class RegisterCrawlScheduler {

        @Test
        @DisplayName("인증된 사용자는 스케줄러를 등록할 수 있다")
        void shouldRegisterScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("sellerId", 1L);
            request.put("schedulerName", "new-test-scheduler");
            request.put("cronExpression", "cron(0 3 * * ? *)");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then - registerCrawlScheduler returns ApiResponse<Long>
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            Object data = response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(((Number) data).longValue()).isPositive();
        }

        @Test
        @DisplayName("필수 필드 누락 시 400을 반환한다 - sellerId")
        void shouldReturn400WhenSellerIdMissing() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("schedulerName", "test-scheduler");
            request.put("cronExpression", "cron(0 3 * * ? *)");
            // sellerId 누락

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("필수 필드 누락 시 400을 반환한다 - schedulerName")
        void shouldReturn400WhenSchedulerNameMissing() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("sellerId", 1L);
            request.put("cronExpression", "cron(0 3 * * ? *)");
            // schedulerName 누락

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 셀러 ID로 등록 시 404를 반환한다")
        void shouldReturn404ForNonExistentSeller() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("sellerId", 99999L);
            request.put("schedulerName", "test-scheduler");
            request.put("cronExpression", "cron(0 3 * * ? *)");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/crawling/schedules/{id} - 스케줄러 수정")
    class UpdateCrawlScheduler {

        @Test
        @DisplayName("인증된 사용자는 스케줄러를 수정할 수 있다")
        void shouldUpdateScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Long schedulerId = 1L;

            // UpdateCrawlSchedulerApiRequest requires ALL fields: schedulerName, cronExpression,
            // active
            Map<String, Object> request = new HashMap<>();
            request.put("schedulerName", "updated-scheduler-name");
            request.put("cronExpression", "cron(30 2 * * ? *)");
            request.put("active", true);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + schedulerId),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("schedulerName")).isEqualTo("updated-scheduler-name");
            assertThat(data.get("cronExpression")).isEqualTo("cron(30 2 * * ? *)");
        }

        @Test
        @DisplayName("필수 필드 누락 시 400을 반환한다")
        void shouldReturn400WhenMissingRequiredFields() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Long schedulerId = 1L;

            // schedulerName만 보내고 다른 필수 필드 누락
            Map<String, Object> request = new HashMap<>();
            request.put("schedulerName", "only-name-updated");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + schedulerId),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 스케줄러 수정 시 404를 반환한다")
        void shouldReturn404ForNonExistentScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Long nonExistentSchedulerId = 99999L;

            Map<String, Object> request = new HashMap<>();
            request.put("schedulerName", "updated-name");
            request.put("cronExpression", "cron(0 3 * * ? *)");
            request.put("active", true);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + nonExistentSchedulerId),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/schedules/{id}/trigger - 수동 트리거")
    class TriggerScheduler {

        @Test
        @DisplayName("인증된 사용자는 ACTIVE 상태의 스케줄러를 트리거할 수 있다")
        void shouldTriggerActiveScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long schedulerId = 1L; // ACTIVE 상태인 스케줄러

            // when
            ResponseEntity<Void> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + schedulerId + "/trigger"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            Void.class);

            // then - triggerScheduler returns 204 NO_CONTENT
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }

        @Test
        @DisplayName("INACTIVE 상태의 스케줄러는 트리거할 수 없다 (400)")
        void shouldNotTriggerInactiveScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long inactiveSchedulerId = 3L; // INACTIVE 상태인 스케줄러

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + inactiveSchedulerId + "/trigger"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("존재하지 않는 스케줄러 트리거 시 404를 반환한다")
        void shouldReturn404ForNonExistentScheduler() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long nonExistentSchedulerId = 99999L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/" + nonExistentSchedulerId + "/trigger"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("인증 검증")
    class Authentication {

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다")
        void shouldReturn401WithoutAuthHeader() {
            // given
            HttpHeaders headers = AuthTestHelper.unauthenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인증된 사용자는 전체 기능에 접근할 수 있다")
        void shouldAllowFullAccessWithAuthentication() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when - 읽기
            ResponseEntity<Map<String, Object>> readResponse =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // when - 등록
            Map<String, Object> registerRequest = new HashMap<>();
            registerRequest.put("sellerId", 1L);
            registerRequest.put("schedulerName", "admin-test-scheduler");
            registerRequest.put("cronExpression", "cron(0 5 * * ? *)");

            ResponseEntity<Map<String, Object>> createResponse =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(registerRequest, headers),
                            new ParameterizedTypeReference<>() {});

            // when - 수정
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("schedulerName", "admin-updated-scheduler");
            updateRequest.put("cronExpression", "cron(0 3 * * ? *)");
            updateRequest.put("active", true);

            ResponseEntity<Map<String, Object>> updateResponse =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(updateRequest, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("입력값 유효성 검증")
    class InputValidation {

        @Test
        @DisplayName("잘못된 상태값으로 조회 시 400을 반환한다")
        void shouldReturn400ForInvalidStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?statuses=INVALID_STATUS"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 페이지 번호로 조회 시 400을 반환한다")
        void shouldReturn400ForNegativePage() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?page=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("페이지 크기 초과 시 400을 반환한다")
        void shouldReturn400ForExcessivePageSize() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "?size=101"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 스케줄러 ID로 수정 시 400을 반환한다")
        void shouldReturn400ForNegativeSchedulerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("schedulerName", "test");
            request.put("cronExpression", "cron(0 3 * * ? *)");
            request.put("active", true);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL + "/-1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("스케줄러 이름이 100자를 초과하면 400을 반환한다")
        void shouldReturn400ForTooLongSchedulerName() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String tooLongName = "a".repeat(101);
            Map<String, Object> request = new HashMap<>();
            request.put("sellerId", 1L);
            request.put("schedulerName", tooLongName);
            request.put("cronExpression", "cron(0 3 * * ? *)");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 셀러 ID로 등록 시 400을 반환한다")
        void shouldReturn400ForNegativeSellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("sellerId", -1L);
            request.put("schedulerName", "test-scheduler");
            request.put("cronExpression", "cron(0 3 * * ? *)");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SCHEDULES_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
