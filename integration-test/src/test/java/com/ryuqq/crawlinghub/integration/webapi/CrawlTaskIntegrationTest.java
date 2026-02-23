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
 * CrawlTask API 통합 테스트
 *
 * <p>테스트 시나리오:
 *
 * <ul>
 *   <li>GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회 (페이징, 필터링)
 *   <li>GET /api/v1/crawling/tasks/{id} - 크롤 태스크 상세 조회
 *   <li>POST /api/v1/crawling/tasks/{id}/retry - 크롤 태스크 재시도
 *   <li>인증/권한 검증 (401, 403)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
class CrawlTaskIntegrationTest extends WebApiIntegrationTest {

    private static final String TASKS_BASE_URL = "/api/v1/crawling/tasks";

    @Autowired private TestDataHelper testDataHelper;

    @BeforeEach
    void setUpTestData() {
        testDataHelper.insertTaskTestData();
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/tasks - 목록 조회")
    class ListCrawlTasks {

        @Test
        @DisplayName("권한이 있는 사용자는 태스크 목록을 조회할 수 있다")
        void shouldReturnTaskListWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
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
        @DisplayName("스케줄러 ID 필터로 해당 스케줄러의 태스크만 조회된다")
        void shouldFilterBySchedulerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 스케줄러 1의 태스크만 조회됨 (task 1, 2, 3)
            content.forEach(
                    task ->
                            assertThat(((Number) task.get("crawlSchedulerId")).longValue())
                                    .isEqualTo(1L));
        }

        @Test
        @DisplayName("상태 필터로 해당 상태의 태스크만 조회된다")
        void shouldFilterByStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&statuses=WAITING"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(task -> assertThat(task.get("status")).isEqualTo("WAITING"));
        }

        @Test
        @DisplayName("태스크 유형 필터로 해당 유형의 태스크만 조회된다")
        void shouldFilterByTaskType() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&taskTypes=META"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            content.forEach(task -> assertThat(task.get("taskType")).isEqualTo("META"));
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&page=0&size=2"),
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
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/tasks/{id} - 상세 조회")
    class GetCrawlTask {

        @Test
        @DisplayName("권한이 있는 사용자는 태스크 상세를 조회할 수 있다")
        void shouldReturnTaskDetailWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long taskId = 1L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + taskId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(((Number) data.get("crawlTaskId")).longValue()).isEqualTo(taskId);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 조회 시 404를 반환한다")
        void shouldReturn404ForNonExistentTask() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long nonExistentTaskId = 99999L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + nonExistentTaskId),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/tasks/{id}/retry - 재시도")
    class RetryCrawlTask {

        @Test
        @DisplayName("권한이 있는 사용자는 FAILED 상태의 태스크를 재시도할 수 있다")
        void shouldRetryFailedTaskWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long failedTaskId = 4L; // FAILED 상태인 태스크

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + failedTaskId + "/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("권한이 있는 사용자는 TIMEOUT 상태의 태스크를 재시도할 수 있다")
        void shouldRetryTimeoutTaskWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long timeoutTaskId = 5L; // TIMEOUT 상태인 태스크

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + timeoutTaskId + "/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("WAITING 상태의 태스크는 재시도할 수 없다 (400)")
        void shouldNotRetryWaitingTask() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long waitingTaskId = 1L; // WAITING 상태인 태스크

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + waitingTaskId + "/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("task:read 권한만 있으면 재시도할 수 없다 (403)")
        void shouldNotRetryWithReadOnlyPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long failedTaskId = 4L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + failedTaskId + "/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 태스크 재시도 시 404를 반환한다")
        void shouldReturn404ForNonExistentTask() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();
            Long nonExistentTaskId = 99999L;

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/" + nonExistentTaskId + "/retry"),
                            HttpMethod.POST,
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
        @DisplayName("인증 헤더가 없으면 401을 반환한다")
        void shouldReturn401WithoutAuthHeader() {
            // given
            HttpHeaders headers = AuthTestHelper.unauthenticated();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("권한이 없으면 403을 반환한다")
        void shouldReturn403WithoutPermission() {
            // given - 인증은 되어 있지만 task:read 권한이 없음
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("잘못된 권한으로는 접근할 수 없다")
        void shouldReturn403WithWrongPermission() {
            // given - scheduler:read 권한만 있고 task:read는 없음
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
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
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when - 읽기
            ResponseEntity<Map<String, Object>> readResponse =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // when - 쓰기 (재시도)
            ResponseEntity<Map<String, Object>> writeResponse =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(writeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
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
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&statuses=INVALID_STATUS"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 태스크 유형으로 조회 시 400을 반환한다")
        void shouldReturn400ForInvalidTaskType() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&taskTypes=INVALID_TYPE"),
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
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&page=-1"),
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
                            url(TASKS_BASE_URL + "?crawlSchedulerId=1&size=101"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 태스크 ID로 조회 시 400을 반환한다")
        void shouldReturn400ForNegativeTaskId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
