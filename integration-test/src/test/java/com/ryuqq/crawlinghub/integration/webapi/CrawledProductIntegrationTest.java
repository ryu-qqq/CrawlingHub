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
 * CrawledProduct API 통합 테스트
 *
 * <p>테스트 시나리오:
 *
 * <ul>
 *   <li>GET /api/v1/crawling/crawled-products - 크롤링 상품 목록 조회 (페이징, 필터링)
 *   <li>GET /api/v1/crawling/crawled-products/{id} - 크롤링 상품 상세 조회
 *   <li>POST /api/v1/crawling/crawled-products/{id}/sync - 수동 동기화 트리거
 *   <li>인증/권한 검증 (401, 403)
 *   <li>입력 값 검증 (400)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
class CrawledProductIntegrationTest extends WebApiIntegrationTest {

    private static final String PRODUCTS_BASE_URL = "/api/v1/crawling/crawled-products";

    @Autowired private TestDataHelper testDataHelper;

    @BeforeEach
    void setUpTestData() {
        testDataHelper.insertCrawledProductTestData();
    }

    // ===============================
    // 인증/권한 테스트
    // ===============================

    @Nested
    @DisplayName("인증 및 권한 검증")
    class AuthenticationAndAuthorization {

        @Test
        @DisplayName("인증 없이 목록 조회 시 401 Unauthorized 반환")
        void shouldReturn401WhenNoAuthentication() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(new HttpHeaders()),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("권한 없이 목록 조회 시 403 Forbidden 반환")
        void shouldReturn403WhenNoReadPermission() {
            // given - 다른 권한으로 인증
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("product:read 권한이 있으면 목록 조회 가능")
        void shouldAllowListWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("권한 없이 동기화 시도 시 403 Forbidden 반환")
        void shouldReturn403WhenNoUpdatePermissionForSync() {
            // given - 읽기 권한만 있음
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("product:update 권한이 있으면 동기화 가능")
        void shouldAllowSyncWithUpdatePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("SUPER_ADMIN은 모든 권한을 가진다")
        void superAdminShouldHaveAllPermissions() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when - 목록 조회
            ResponseEntity<Map<String, Object>> listResponse =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // when - 동기화
            ResponseEntity<Map<String, Object>> syncResponse =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    // ===============================
    // 목록 조회 테스트
    // ===============================

    @Nested
    @DisplayName("GET /api/v1/crawling/crawled-products - 목록 조회")
    class ListCrawledProducts {

        @Test
        @DisplayName("권한이 있으면 상품 목록을 조회할 수 있다")
        void shouldReturnProductListWithPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL),
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
            assertThat(content.size()).isEqualTo(5); // 5개의 테스트 데이터
        }

        @Test
        @DisplayName("sellerId 필터로 해당 셀러의 상품만 조회된다")
        void shouldFilterBySellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?sellerId=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // seller 1의 상품만 조회됨 (3개: id 1, 2, 3)
            assertThat(content.size()).isEqualTo(3);
            content.forEach(
                    product ->
                            assertThat(((Number) product.get("sellerId")).longValue())
                                    .isEqualTo(1L));
        }

        @Test
        @DisplayName("brandName 필터로 해당 브랜드의 상품만 조회된다")
        void shouldFilterByBrandName() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?brandName=테스트 브랜드 A"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // "테스트 브랜드 A"의 상품 (2개: id 1, 2)
            assertThat(content.size()).isEqualTo(2);
            content.forEach(product -> assertThat(product.get("brandName")).isEqualTo("테스트 브랜드 A"));
        }

        @Test
        @DisplayName("needsSync=true 필터로 동기화 필요한 상품만 조회된다")
        void shouldFilterByNeedsSync() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?needsSync=true"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // needs_sync=true 상품 (2개: id 1, 4)
            assertThat(content.size()).isEqualTo(2);
            content.forEach(product -> assertThat(product.get("needsSync")).isEqualTo(true));
        }

        @Test
        @DisplayName("allCrawled=true 필터로 모든 크롤링 완료된 상품만 조회된다")
        void shouldFilterByAllCrawled() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?allCrawled=true"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 모든 크롤링 완료된 상품 (3개: id 1, 2, 4)
            assertThat(content.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("hasExternalId=true 필터로 외부 서버 등록된 상품만 조회된다")
        void shouldFilterByHasExternalId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?hasExternalId=true"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // 외부 서버 등록된 상품 (1개: id 2)
            assertThat(content.size()).isEqualTo(1);
            assertThat(content.get(0).get("externalProductId")).isNotNull();
        }

        @Test
        @DisplayName("페이지네이션이 정상 동작한다")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?page=0&size=2"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            assertThat(((Number) data.get("page")).intValue()).isEqualTo(0);
            assertThat(((Number) data.get("size")).intValue()).isEqualTo(2);
            assertThat(((Number) data.get("totalElements")).longValue()).isEqualTo(5L);
            assertThat(((Number) data.get("totalPages")).intValue()).isEqualTo(3);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("복합 필터링이 정상 동작한다")
        void shouldWorkWithMultipleFilters() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when - sellerId=1이면서 allCrawled=true인 상품
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?sellerId=1&allCrawled=true"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

            // seller 1의 모든 크롤링 완료 상품 (2개: id 1, 2)
            assertThat(content.size()).isEqualTo(2);
        }
    }

    // ===============================
    // 상세 조회 테스트
    // ===============================

    @Nested
    @DisplayName("GET /api/v1/crawling/crawled-products/{id} - 상세 조회")
    class GetCrawledProductDetail {

        @Test
        @DisplayName("존재하는 상품을 상세 조회할 수 있다")
        void shouldReturnProductDetail() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(((Number) data.get("id")).longValue()).isEqualTo(1L);
            assertThat(data.get("itemName")).isEqualTo("테스트 상품 1");
            assertThat(data.get("brandName")).isEqualTo("테스트 브랜드 A");
        }

        @Test
        @DisplayName("상세 조회 시 가격 정보가 포함된다")
        void shouldIncludePriceInfo() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // 가격 정보는 중첩 구조 (price 필드 아래)
            @SuppressWarnings("unchecked")
            Map<String, Object> priceInfo = (Map<String, Object>) data.get("price");
            assertThat(priceInfo).isNotNull();
            assertThat(((Number) priceInfo.get("originalPrice")).intValue()).isEqualTo(100000);
            // discountPrice는 price 필드에 해당
            assertThat(((Number) priceInfo.get("price")).intValue()).isEqualTo(80000);
            assertThat(((Number) priceInfo.get("discountRate")).intValue()).isEqualTo(20);
        }

        @Test
        @DisplayName("상세 조회 시 크롤링 상태가 포함된다")
        void shouldIncludeCrawlStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // 크롤링 상태는 중첩 구조 (crawlStatus 필드 아래)
            @SuppressWarnings("unchecked")
            Map<String, Object> crawlStatus = (Map<String, Object>) data.get("crawlStatus");
            assertThat(crawlStatus).isNotNull();
            assertThat(crawlStatus.get("miniShopCrawledAt")).isNotNull();
            assertThat(crawlStatus.get("detailCrawledAt")).isNotNull();
            assertThat(crawlStatus.get("optionCrawledAt")).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 시 404 반환")
        void shouldReturn404WhenProductNotFound() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/9999"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ===============================
    // 수동 동기화 테스트
    // ===============================

    @Nested
    @DisplayName("POST /api/v1/crawling/crawled-products/{id}/sync - 수동 동기화")
    class TriggerManualSync {

        @Test
        @DisplayName("모든 크롤링 완료된 상품은 동기화할 수 있다")
        void shouldSyncCompletedProduct() {
            // given - id=1: 모든 크롤링 완료, needs_sync=true
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(((Number) data.get("crawledProductId")).longValue()).isEqualTo(1L);
            assertThat(data.get("syncType")).isNotNull();
        }

        @Test
        @DisplayName("외부 서버에 등록되지 않은 상품은 CREATE 타입으로 동기화된다")
        void shouldUseCREATETypeForNewProduct() {
            // given - id=1: externalProductId가 null
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/1/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("syncType")).isEqualTo("CREATE");
        }

        @Test
        @DisplayName("외부 서버에 등록된 상품은 UPDATE 타입으로 동기화된다")
        void shouldUseUPDATETypeForExistingProduct() {
            // given - id=2: externalProductId가 99001
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/2/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("syncType")).isEqualTo("UPDATE");
        }

        @Test
        @DisplayName("크롤링 미완료 상품 동기화 시 409 Conflict 반환")
        void shouldReturn409WhenCrawlNotCompleted() {
            // given - id=3: MINI_SHOP만 완료 (동기화 불가)
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/3/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - IllegalStateException은 409 CONFLICT로 매핑됨
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("OPTION 미완료 상품 동기화 시 409 Conflict 반환")
        void shouldReturn409WhenOptionCrawlNotCompleted() {
            // given - id=5: MINI_SHOP, DETAIL 완료, OPTION 미완료
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/5/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - IllegalStateException은 409 CONFLICT로 매핑됨
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("존재하지 않는 상품 동기화 시 404 반환")
        void shouldReturn404WhenProductNotFoundForSync() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/9999/sync"),
                            HttpMethod.POST,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    // ===============================
    // 입력 값 검증 테스트
    // ===============================

    @Nested
    @DisplayName("입력 값 검증")
    class InputValidation {

        @Test
        @DisplayName("음수 sellerId는 400 Bad Request 반환")
        void shouldReturn400ForNegativeSellerId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?sellerId=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 page는 400 Bad Request 반환")
        void shouldReturn400ForNegativePage() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?page=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - @Min(0) validation이 적용됨
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("size가 100을 초과하면 400 Bad Request 반환")
        void shouldReturn400ForSizeOver100() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "?size=200"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then - @Max(100) validation이 적용됨
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 ID로 상세 조회 시 400 Bad Request 반환")
        void shouldReturn400ForNegativeId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("0 ID로 상세 조회 시 400 Bad Request 반환")
        void shouldReturn400ForZeroId() {
            // given
            HttpHeaders headers = AuthTestHelper.serviceAuth();

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(PRODUCTS_BASE_URL + "/0"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}
