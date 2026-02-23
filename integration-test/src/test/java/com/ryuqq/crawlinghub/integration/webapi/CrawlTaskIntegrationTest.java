package com.ryuqq.crawlinghub.integration.webapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.integration.base.WebApiIntegrationTest;
import com.ryuqq.crawlinghub.integration.helper.AuthTestHelper;
import com.ryuqq.crawlinghub.integration.helper.TestDataHelper;
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
 *   <li>POST /api/v1/crawling/tasks/{id}/retry - 크롤 태스크 재시도
 *   <li>인증 검증 (401)
 * </ul>
 *
 * <p>Note: CrawlTask Query Controller는 리팩토링으로 제거되었습니다. 현재 CrawlTaskCommandController만 존재하며 retry
 * 엔드포인트만 제공합니다.
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
    @DisplayName("POST /api/v1/crawling/tasks/{id}/retry - 재시도")
    class RetryCrawlTask {

        @Test
        @DisplayName("FAILED 상태의 태스크를 재시도할 수 있다")
        void shouldRetryFailedTask() {
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
        @DisplayName("TIMEOUT 상태의 태스크를 재시도할 수 있다")
        void shouldRetryTimeoutTask() {
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
                            url(TASKS_BASE_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인증된 사용자는 재시도할 수 있다")
        void shouldAllowRetryWithAuthentication() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(TASKS_BASE_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
