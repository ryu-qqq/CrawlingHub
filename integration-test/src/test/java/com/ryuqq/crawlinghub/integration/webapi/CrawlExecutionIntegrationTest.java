package com.ryuqq.crawlinghub.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.integration.base.WebApiIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.AuthTestHelper;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
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
import org.springframework.http.ResponseEntity;

/**
 * CrawlExecution API 통합 테스트
 *
 * <p>테스트 시나리오:
 *
 * <ul>
 *   <li>GET /api/v1/crawling/executions - 크롤 실행 기록 목록 조회 (페이징, 필터링)
 *   <li>GET /api/v1/crawling/executions/{id} - 크롤 실행 기록 상세 조회
 *   <li>인증/권한 검증 (401, 403)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
class CrawlExecutionIntegrationTest extends WebApiIntegrationTest {

    private static final String EXECUTIONS_BASE_URL = "/api/v1/crawling/executions";

    @Autowired private TestDataHelper testDataHelper;

    @BeforeEach
    void setUpTestData() {
        testDataHelper.insertExecutionTestData();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/executions - 목록 조회")
    class ListCrawlExecutions {

        @Test
        @DisplayName("권한이 있는 사용자는 실행 기록 목록을 조회할 수 있다")
        void shouldReturnExecutionListWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(true);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).isNotEmpty();
        }

        @Test
        @DisplayName("태스크 ID 필터로 해당 태스크의 실행 기록만 조회된다")
        void shouldFilterByTaskId() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?crawlTaskId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 태스크 1의 실행 기록만 조회됨
            content.forEach(
                    execution ->
                            assertThat(((Number) execution.get("crawlTaskId")).longValue())
                                    .isEqualTo(1L));
        }

        @Test
        @DisplayName("스케줄러 ID 필터로 해당 스케줄러의 실행 기록만 조회된다")
        void shouldFilterBySchedulerId() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 스케줄러 1의 실행 기록만 조회됨 (execution 1, 2, 3)
            content.forEach(
                    execution ->
                            assertThat(((Number) execution.get("crawlSchedulerId")).longValue())
                                    .isEqualTo(1L));
        }

        @Test
        @DisplayName("셀러 ID 필터로 해당 셀러의 실행 기록만 조회된다")
        void shouldFilterBySellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?sellerId=2"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 셀러 2의 실행 기록만 조회됨 (execution 6)
            content.forEach(
                    execution ->
                            assertThat(((Number) execution.get("sellerId")).longValue())
                                    .isEqualTo(2L));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 실행 기록만 조회된다 (SUCCESS)")
        void shouldFilterBySuccessStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?statuses=SUCCESS"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(execution -> assertThat(execution.get("status")).isEqualTo("SUCCESS"));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 실행 기록만 조회된다 (FAILED)")
        void shouldFilterByFailedStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?statuses=FAILED"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(execution -> assertThat(execution.get("status")).isEqualTo("FAILED"));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 실행 기록만 조회된다 (RUNNING)")
        void shouldFilterByRunningStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?statuses=RUNNING"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(execution -> assertThat(execution.get("status")).isEqualTo("RUNNING"));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 실행 기록만 조회된다 (TIMEOUT)")
        void shouldFilterByTimeoutStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?statuses=TIMEOUT"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(execution -> assertThat(execution.get("status")).isEqualTo("TIMEOUT"));
        }

        @Test
        @DisplayName("복합 필터로 조건에 맞는 실행 기록만 조회된다")
        void shouldFilterByMultipleConditions() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?crawlSchedulerId=1&statuses=SUCCESS"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(
                    execution -> {
                        assertThat(((Number) execution.get("crawlSchedulerId")).longValue())
                                .isEqualTo(1L);
                        assertThat(execution.get("status")).isEqualTo("SUCCESS");
                    });
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?page=0&size=2"),
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

        @Test
        @DisplayName("SUPER_ADMIN은 권한 없이도 조회할 수 있다")
        void superAdminShouldAccessWithoutPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.superAdmin();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/executions/{id} - 상세 조회")
    class GetCrawlExecution {

        @Test
        @DisplayName("권한이 있는 사용자는 실행 기록 상세를 조회할 수 있다")
        void shouldReturnExecutionDetailWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");
            Long executionId = 1L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/" + executionId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(true);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(((Number) data.get("crawlExecutionId")).longValue()).isEqualTo(executionId);
            assertThat(data.get("status")).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("상세 조회 시 responseBody와 errorMessage가 포함된다")
        void shouldReturnDetailedFields() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");
            Long executionId = 1L; // SUCCESS 상태, responseBody 있음

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/" + executionId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // SUCCESS 상태의 상세 정보 필드들이 포함되어 있는지 확인
            // Jackson의 non_null 설정으로 인해 null 필드는 응답에서 제외됨
            assertThat(data.containsKey("responseBody")).isTrue(); // SUCCESS이므로 responseBody 있음
            assertThat(data.get("responseBody")).isEqualTo("{\"products\": []}");
            assertThat(data.containsKey("httpStatusCode")).isTrue();
            assertThat(data.get("httpStatusCode")).isEqualTo(200);
            assertThat(data.containsKey("durationMs")).isTrue();
            // errorMessage는 SUCCESS 상태에서 null이므로 응답에 포함되지 않음 (non_null 설정)
            assertThat(data.containsKey("errorMessage")).isFalse();
        }

        @Test
        @DisplayName("실패한 실행 기록 조회 시 에러 메시지가 포함된다")
        void shouldReturnErrorMessageForFailedExecution() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");
            Long failedExecutionId = 4L; // FAILED 상태

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/" + failedExecutionId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            assertThat(data.get("status")).isEqualTo("FAILED");
            assertThat(data.get("errorMessage")).isNotNull();
            assertThat(((Number) data.get("httpStatusCode")).intValue()).isEqualTo(500);
        }

        @Test
        @DisplayName("RUNNING 상태의 실행 기록은 completedAt이 null이다")
        void runningExecutionShouldHaveNullCompletedAt() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");
            Long runningExecutionId = 2L; // RUNNING 상태

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/" + runningExecutionId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            assertThat(data.get("status")).isEqualTo("RUNNING");
            assertThat(data.get("completedAt")).isNull();
            assertThat(data.get("durationMs")).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 실행 기록 조회 시 404를 반환한다")
        void shouldReturn404ForNonExistentExecution() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");
            Long nonExistentExecutionId = 99999L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/" + nonExistentExecutionId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("인증/권한 검증")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다 (목록 조회)")
        void shouldReturn401WithoutAuthHeaderForList() {
            // given
            HttpHeaders headers = AuthTestHelper.unauthenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인증 헤더가 없으면 401을 반환한다 (상세 조회)")
        void shouldReturn401WithoutAuthHeaderForDetail() {
            // given
            HttpHeaders headers = AuthTestHelper.unauthenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("권한이 없으면 403을 반환한다 (목록 조회)")
        void shouldReturn403WithoutPermissionForList() {
            // given - 인증은 되어 있지만 execution:read 권한이 없음
            HttpHeaders headers = AuthTestHelper.authenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("권한이 없으면 403을 반환한다 (상세 조회)")
        void shouldReturn403WithoutPermissionForDetail() {
            // given - 인증은 되어 있지만 execution:read 권한이 없음
            HttpHeaders headers = AuthTestHelper.authenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("잘못된 권한으로는 접근할 수 없다")
        void shouldReturn403WithWrongPermission() {
            // given - task:read 권한만 있고 execution:read는 없음
            HttpHeaders headers = AuthTestHelper.withPermissions("task:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("SUPER_ADMIN 역할은 모든 권한을 우회한다")
        void superAdminShouldBypassAllPermissions() {
            // given
            HttpHeaders headers = AuthTestHelper.superAdmin();

            // when - 목록 조회
            ResponseEntity<Map<String, Object>> listResponse =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // when - 상세 조회
            ResponseEntity<Map<String, Object>> detailResponse =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("입력값 유효성 검증")
    class InputValidation {

        @Test
        @DisplayName("잘못된 상태값으로 조회 시 400을 반환한다")
        void shouldReturn400ForInvalidStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?statuses=INVALID_STATUS"),
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
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?page=-1"),
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
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?size=101"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("0 이하의 페이지 크기로 조회 시 400을 반환한다")
        void shouldReturn400ForZeroPageSize() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?size=0"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 실행 기록 ID로 조회 시 400을 반환한다")
        void shouldReturn400ForNegativeExecutionId() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "/-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 태스크 ID 필터 시 400을 반환한다")
        void shouldReturn400ForNegativeTaskIdFilter() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?crawlTaskId=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 스케줄러 ID 필터 시 400을 반환한다")
        void shouldReturn400ForNegativeSchedulerIdFilter() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?crawlSchedulerId=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 셀러 ID 필터 시 400을 반환한다")
        void shouldReturn400ForNegativeSellerIdFilter() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("execution:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(EXECUTIONS_BASE_URL + "?sellerId=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
