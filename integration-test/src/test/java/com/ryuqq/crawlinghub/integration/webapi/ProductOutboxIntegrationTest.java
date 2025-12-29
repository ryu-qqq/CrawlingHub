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
 * ProductOutbox API 통합 테스트
 *
 * <p>ProductOutbox API의 전체 동작을 검증하는 통합 테스트입니다.
 *
 * <p><strong>테스트 대상 엔드포인트:</strong>
 *
 * <ul>
 *   <li>GET /api/v1/crawling/product-outbox/sync - SyncOutbox 목록 조회 (outbox:read)
 *   <li>GET /api/v1/crawling/product-outbox/image - ImageOutbox 목록 조회 (outbox:read)
 *   <li>POST /api/v1/crawling/product-outbox/sync/{id}/retry - SyncOutbox 재시도 (outbox:update)
 *   <li>POST /api/v1/crawling/product-outbox/image/{id}/retry - ImageOutbox 재시도 (outbox:update)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProductOutbox API 통합 테스트")
class ProductOutboxIntegrationTest extends WebApiIntegrationTest {

    private static final String PRODUCT_OUTBOX_BASE_URL = "/api/v1/crawling/product-outbox";
    private static final String SYNC_URL = PRODUCT_OUTBOX_BASE_URL + "/sync";
    private static final String IMAGE_URL = PRODUCT_OUTBOX_BASE_URL + "/image";

    @Autowired private TestDataHelper testDataHelper;

    @Nested
    @DisplayName("GET /api/v1/crawling/product-outbox/sync - SyncOutbox 목록 조회")
    class SearchSyncOutbox {

        @BeforeEach
        void setUp() {
            testDataHelper.insertProductOutboxTestData();
        }

        @Test
        @DisplayName("전체 SyncOutbox 목록 조회 성공")
        void shouldSearchAllSyncOutboxSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL),
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
            assertThat(data.get("totalElements")).isEqualTo(5);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(5);
        }

        @Test
        @DisplayName("상태별 SyncOutbox 필터링 조회")
        void shouldFilterSyncOutboxByStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "?statuses=FAILED"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(2); // FAILED 2개

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).allMatch(o -> "FAILED".equals(o.get("status")));
        }

        @Test
        @DisplayName("Seller ID로 SyncOutbox 필터링 조회")
        void shouldFilterSyncOutboxBySellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "?sellerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(3); // seller_id=1인 것 3개

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).allMatch(o -> Integer.valueOf(1).equals(o.get("sellerId")));
        }

        @Test
        @DisplayName("페이지네이션 정상 동작")
        void shouldPaginateSyncOutboxCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "?page=0&size=2"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(5);
            assertThat(data.get("totalPages")).isEqualTo(3);
            assertThat(data.get("size")).isEqualTo(2);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(2);
        }

        @Test
        @DisplayName("outbox:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL),
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
                            url(SYNC_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/product-outbox/image - ImageOutbox 목록 조회")
    class SearchImageOutbox {

        @BeforeEach
        void setUp() {
            testDataHelper.insertProductOutboxTestData();
        }

        @Test
        @DisplayName("전체 ImageOutbox 목록 조회 성공")
        void shouldSearchAllImageOutboxSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL),
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
            assertThat(data.get("totalElements")).isEqualTo(5);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(5);
        }

        @Test
        @DisplayName("상태별 ImageOutbox 필터링 조회")
        void shouldFilterImageOutboxByStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL + "?statuses=COMPLETED"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(1); // COMPLETED 1개
        }

        @Test
        @DisplayName("outbox:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL),
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
                            url(IMAGE_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/product-outbox/sync/{id}/retry - SyncOutbox 재시도")
    class RetrySyncOutbox {

        @BeforeEach
        void setUp() {
            testDataHelper.insertProductOutboxTestData();
        }

        @Test
        @DisplayName("FAILED 상태의 SyncOutbox 재시도 성공")
        void shouldRetrySyncOutboxSuccessfully() {
            // given - id=4: FAILED 상태, 재시도 1회 (재시도 가능)
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "/4/retry"),
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
            assertThat(data.get("outboxId")).isEqualTo(4);
            assertThat(data.get("previousStatus")).isEqualTo("FAILED");
            assertThat(data.get("newStatus")).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("존재하지 않는 SyncOutbox 재시도 시 404 반환")
        void shouldReturn404WhenSyncOutboxNotFound() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "/9999/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("outbox:update 권한 없으면 403 반환")
        void shouldReturn403WithoutUpdatePermission() {
            // given - outbox:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("인증 없이 재시도 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL + "/4/retry"),
                            HttpMethod.POST,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/crawling/product-outbox/image/{id}/retry - ImageOutbox 재시도")
    class RetryImageOutbox {

        @BeforeEach
        void setUp() {
            testDataHelper.insertProductOutboxTestData();
        }

        @Test
        @DisplayName("FAILED 상태의 ImageOutbox 재시도 성공")
        void shouldRetryImageOutboxSuccessfully() {
            // given - id=4: FAILED 상태, 재시도 1회 (재시도 가능)
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL + "/4/retry"),
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
            assertThat(data.get("outboxId")).isEqualTo(4);
            assertThat(data.get("previousStatus")).isEqualTo("FAILED");
            assertThat(data.get("newStatus")).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("존재하지 않는 ImageOutbox 재시도 시 404 반환")
        void shouldReturn404WhenImageOutboxNotFound() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL + "/9999/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("outbox:update 권한 없으면 403 반환")
        void shouldReturn403WithoutUpdatePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("인증 없이 재시도 시 401 반환")
        void shouldReturn401WithoutAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL + "/4/retry"),
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
            testDataHelper.insertProductOutboxTestData();
        }

        @Test
        @DisplayName("outbox:read 권한으로 SyncOutbox 조회 가능")
        void shouldAllowSyncOutboxSearchWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SYNC_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("outbox:read 권한으로 ImageOutbox 조회 가능")
        void shouldAllowImageOutboxSearchWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(IMAGE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("outbox:update 권한으로 재시도 가능")
        void shouldAllowRetryWithUpdatePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when - SyncOutbox 재시도
            ResponseEntity<Map<String, Object>> syncResponse =
                    restTemplate.exchange(
                            url(SYNC_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("여러 권한이 있는 경우 정상 동작")
        void shouldWorkWithMultiplePermissions() {
            // given - 여러 권한을 가진 사용자
            HttpHeaders headers = AuthTestHelper.withPermissions("outbox:read", "outbox:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // when - 조회 요청
            ResponseEntity<Map<String, Object>> readResponse =
                    restTemplate.exchange(
                            url(SYNC_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - 조회 성공
            assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // when - 재시도 요청
            ResponseEntity<Map<String, Object>> retryResponse =
                    restTemplate.exchange(
                            url(IMAGE_URL + "/4/retry"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - 재시도 성공
            assertThat(retryResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
