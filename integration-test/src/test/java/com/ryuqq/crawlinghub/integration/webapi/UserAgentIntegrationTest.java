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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * UserAgent API 통합 테스트
 *
 * <p>UserAgent API의 전체 동작을 검증하는 통합 테스트입니다.
 *
 * <p><strong>테스트 대상 엔드포인트:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/user-agents - UserAgent 목록 조회 (useragent:read)
 *   <li>GET /api/v1/crawling/user-agents/pool-status - Pool 상태 조회 (useragent:read)
 *   <li>POST /api/v1/crawling/user-agents/recover - 정지된 UserAgent 복구 (useragent:manage)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("UserAgent API 통합 테스트")
class UserAgentIntegrationTest extends WebApiIntegrationTest {

    private static final String USER_AGENTS_BASE_URL = "/api/v1/crawling/user-agents";
    private static final String POOL_STATUS_URL = USER_AGENTS_BASE_URL + "/pool-status";
    private static final String RECOVER_URL = USER_AGENTS_BASE_URL + "/recover";

    @Autowired private TestDataHelper testDataHelper;

    @Nested
    @DisplayName("GET /api/v1/crawling/user-agents - UserAgent 목록 조회")
    class ListUserAgents {

        @BeforeEach
        void setUp() {
            testDataHelper.insertUserAgents();
        }

        @Test
        @DisplayName("전체 UserAgent 목록 조회 성공")
        void shouldListAllUserAgentsSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL),
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
            assertThat(data.get("totalElements")).isEqualTo(4);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(4);
        }

        @Test
        @DisplayName("AVAILABLE 상태 필터링 조회")
        void shouldFilterByAvailableStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL + "?status=AVAILABLE"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(2); // AVAILABLE 2개

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).allMatch(ua -> "AVAILABLE".equals(ua.get("status")));
        }

        @Test
        @DisplayName("BLOCKED 상태 필터링 조회")
        void shouldFilterByBlockedStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL + "?status=BLOCKED"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(1); // BLOCKED 1개
        }

        @Test
        @DisplayName("페이지네이션 정상 동작")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when - 페이지 크기 2로 첫 페이지 조회
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL + "?page=0&size=2"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(4);
            assertThat(data.get("totalPages")).isEqualTo(2);
            assertThat(data.get("size")).isEqualTo(2);
            assertThat(data.get("page")).isEqualTo(0);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(2);
        }

        @Test
        @DisplayName("잘못된 status 값으로 조회 시 400 반환")
        void shouldReturn400WhenInvalidStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL + "?status=INVALID"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("useragent:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given - seller:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when - 인증 헤더 없음
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/user-agents/pool-status - Pool 상태 조회")
    class GetPoolStatus {

        @BeforeEach
        void setUp() {
            testDataHelper.insertUserAgents();
        }

        @Test
        @DisplayName("Pool 상태 조회 성공")
        void shouldGetPoolStatusSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(POOL_STATUS_URL),
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
            assertThat(data.get("totalAgents")).isNotNull();
            assertThat(data.get("availableAgents")).isNotNull();
            assertThat(data.get("suspendedAgents")).isNotNull();
            assertThat(data.get("availableRate")).isNotNull();
            assertThat(data.get("isHealthy")).isNotNull();
        }

        @Test
        @DisplayName("useragent:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(POOL_STATUS_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(POOL_STATUS_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/user-agents/recover - UserAgent 복구")
    class RecoverUserAgents {

        @BeforeEach
        void setUp() {
            testDataHelper.insertUserAgents();
        }

        @Test
        @DisplayName("UserAgent 복구 성공")
        void shouldRecoverUserAgentsSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:manage");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(RECOVER_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("success")).isEqualTo(true);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("recoveredCount")).isNotNull();
            assertThat(data.get("message")).isNotNull();
        }

        @Test
        @DisplayName("useragent:manage 권한 없으면 403 반환")
        void shouldReturn403WithoutManagePermission() {
            // given - useragent:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(RECOVER_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("인증 없이 복구 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(RECOVER_URL),
                            HttpMethod.POST,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("인증 및 권한 검증")
    class AuthenticationAndAuthorization {

        @BeforeEach
        void setUp() {
            testDataHelper.insertUserAgents();
        }

        @Test
        @DisplayName("useragent:read 권한이 있으면 목록 조회 가능")
        void shouldAllowListWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("useragent:read 권한이 있으면 Pool 상태 조회 가능")
        void shouldAllowPoolStatusWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(POOL_STATUS_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("useragent:manage 권한이 있으면 복구 가능")
        void shouldAllowRecoverWithManagePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("useragent:manage");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(RECOVER_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("여러 권한이 있는 경우 정상 동작")
        void shouldWorkWithMultiplePermissions() {
            // given - 여러 권한을 가진 사용자
            HttpHeaders headers =
                    AuthTestHelper.withPermissions("useragent:read", "useragent:manage");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when - 읽기 요청
            ResponseEntity<Map<String, Object>> readResponse =
                    restTemplate.exchange(
                            url(USER_AGENTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - 읽기 성공
            assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // when - 관리 요청
            ResponseEntity<Map<String, Object>> manageResponse =
                    restTemplate.exchange(
                            url(RECOVER_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - 관리 성공
            assertThat(manageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
