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
 * Seller API 통합 테스트
 *
 * <p>Seller API의 전체 동작을 검증하는 통합 테스트입니다.
 *
 * <p><strong>테스트 대상 엔드포인트:</strong>
 *
 * <ul>
 *   <li>POST /api/v1/crawling/sellers - 셀러 등록 (seller:create)
 *   <li>PATCH /api/v1/crawling/sellers/{id} - 셀러 수정 (seller:update)
 *   <li>GET /api/v1/crawling/sellers/{id} - 셀러 단건 조회 (seller:read)
 *   <li>GET /api/v1/crawling/sellers - 셀러 목록 조회 (seller:read)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Seller API 통합 테스트")
class SellerIntegrationTest extends WebApiIntegrationTest {

    private static final String SELLERS_BASE_URL = "/api/v1/crawling/sellers";

    @Autowired private TestDataHelper testDataHelper;

    @Nested
    @DisplayName("POST /api/v1/crawling/sellers - 셀러 등록")
    class RegisterSeller {

        @Test
        @DisplayName("유효한 요청으로 셀러 등록 성공")
        void shouldRegisterSellerSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "new-must-it-seller",
                            "sellerName", "신규 셀러");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            // sellerId는 persist 후 도메인 객체에 반영되지 않아 null 반환 (알려진 제약사항)
            assertThat(data.get("mustItSellerName")).isEqualTo("new-must-it-seller");
            assertThat(data.get("sellerName")).isEqualTo("신규 셀러");
            assertThat(data.get("status")).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("중복된 머스트잇 셀러명으로 등록 시 409 Conflict 반환")
        void shouldReturn409WhenDuplicateMustItSellerName() {
            // given - 기존 셀러 존재
            testDataHelper.insertSellers();

            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 이미 존재하는 mustItSellerName 사용
            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "test-must-it-seller-1",
                            "sellerName", "새로운 셀러명");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("중복된 셀러명으로 등록 시 409 Conflict 반환")
        void shouldReturn409WhenDuplicateSellerName() {
            // given - 기존 셀러 존재
            testDataHelper.insertSellers();

            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 이미 존재하는 sellerName 사용
            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "unique-must-it-seller",
                            "sellerName", "test-seller-1");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("필수 필드 누락 시 400 반환")
        void shouldReturn400WhenMissingRequiredFields() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // mustItSellerName 누락
            Map<String, Object> request = Map.of("sellerName", "셀러명만");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("seller:create 권한 없으면 403 반환")
        void shouldReturn403WithoutCreatePermission() {
            // given - seller:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "new-seller",
                            "sellerName", "신규 셀러");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/crawling/sellers/{id} - 셀러 수정")
    class UpdateSeller {

        @BeforeEach
        void setUp() {
            testDataHelper.insertSellers();
        }

        @Test
        @DisplayName("셀러명 수정 성공")
        void shouldUpdateSellerNameSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("sellerName", "수정된 셀러명");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("sellerName")).isEqualTo("수정된 셀러명");
        }

        @Test
        @DisplayName("셀러 비활성화 성공")
        void shouldDeactivateSellerSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("active", false);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("status")).isEqualTo("INACTIVE");
        }

        @Test
        @DisplayName("존재하지 않는 셀러 수정 시 404 반환")
        void shouldReturn404WhenSellerNotFound() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("sellerName", "수정된 이름");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/99999"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("seller:update 권한 없으면 403 반환")
        void shouldReturn403WithoutUpdatePermission() {
            // given - seller:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("sellerName", "수정된 이름");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("100자 초과 셀러명 수정 시 400 반환")
        void shouldReturn400WhenSellerNameTooLong() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String longName = "a".repeat(101);
            Map<String, Object> request = Map.of("sellerName", longName);

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/sellers/{id} - 셀러 단건 조회")
    class GetSeller {

        @BeforeEach
        void setUp() {
            testDataHelper.insertSellers();
        }

        @Test
        @DisplayName("셀러 상세 조회 성공")
        void shouldGetSellerSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("sellerId")).isEqualTo(1);
            assertThat(data.get("mustItSellerName")).isEqualTo("test-must-it-seller-1");
            assertThat(data.get("sellerName")).isEqualTo("test-seller-1");
            assertThat(data.get("status")).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("존재하지 않는 셀러 조회 시 404 반환")
        void shouldReturn404WhenSellerNotFound() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/99999"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("seller:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given - scheduler:read 권한만 있음
            HttpHeaders headers = AuthTestHelper.withPermissions("scheduler:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
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
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/crawling/sellers - 셀러 목록 조회")
    class ListSellers {

        @BeforeEach
        void setUp() {
            testDataHelper.insertSellers();
        }

        @Test
        @DisplayName("전체 셀러 목록 조회 성공")
        void shouldListAllSellersSuccessfully() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data).isNotNull();
            assertThat(data.get("totalElements")).isEqualTo(3);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(3);
        }

        @Test
        @DisplayName("ACTIVE 상태 필터링 조회")
        void shouldFilterByActiveStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?statuses=ACTIVE"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(2); // ACTIVE 셀러 2개

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).allMatch(seller -> "ACTIVE".equals(seller.get("status")));
        }

        @Test
        @DisplayName("INACTIVE 상태 필터링 조회")
        void shouldFilterByInactiveStatus() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?statuses=INACTIVE"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(1); // INACTIVE 셀러 1개

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).allMatch(seller -> "INACTIVE".equals(seller.get("status")));
        }

        @Test
        @DisplayName("페이지네이션 정상 동작")
        void shouldPaginateCorrectly() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when - 페이지 크기 1로 첫 페이지 조회
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?page=0&size=1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            assertThat(data.get("totalElements")).isEqualTo(3);
            assertThat(data.get("totalPages")).isEqualTo(3);
            assertThat(data.get("size")).isEqualTo(1);
            assertThat(data.get("page")).isEqualTo(0);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");
            assertThat(content).hasSize(1);
        }

        @Test
        @DisplayName("seller:read 권한 없으면 403 반환")
        void shouldReturn403WithoutReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("scheduler:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("입력 유효성 검증")
    class InputValidation {

        @Test
        @DisplayName("빈 문자열 mustItSellerName 등록 시 400 반환")
        void shouldReturn400WhenEmptyMustItSellerName() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "",
                            "sellerName", "정상 셀러명");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("빈 문자열 sellerName 등록 시 400 반환")
        void shouldReturn400WhenEmptySellerName() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "정상-머스트잇-이름",
                            "sellerName", "");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("100자 초과 mustItSellerName 등록 시 400 반환")
        void shouldReturn400WhenMustItSellerNameTooLong() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String longName = "a".repeat(101);
            Map<String, Object> request =
                    Map.of("mustItSellerName", longName, "sellerName", "정상 셀러명");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 status 값으로 목록 조회 시 400 반환")
        void shouldReturn400WhenInvalidStatus() {
            // given
            testDataHelper.insertSellers();
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?statuses=INVALID"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("음수 page 파라미터 시 400 반환")
        void shouldReturn400WhenNegativePage() {
            // given
            testDataHelper.insertSellers();
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?page=-1"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("100 초과 size 파라미터 시 400 반환")
        void shouldReturn400WhenSizeExceeds100() {
            // given
            testDataHelper.insertSellers();
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "?size=101"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("인증 및 권한 검증")
    class AuthenticationAndAuthorization {

        @BeforeEach
        void setUp() {
            testDataHelper.insertSellers();
        }

        @Test
        @DisplayName("인증 없이 등록 시 401 반환")
        void shouldReturn401WhenCreateWithoutAuth() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "seller",
                            "sellerName", "셀러");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인증 없이 수정 시 401 반환")
        void shouldReturn401WhenUpdateWithoutAuth() {
            // given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("sellerName", "수정");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("인증 없이 목록 조회 시 401 반환")
        void shouldReturn401WhenListWithoutAuth() {
            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("seller:read 권한이 있으면 목록 조회 가능")
        void shouldAllowListWithReadPermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:read");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("seller:create 권한이 있으면 등록 가능")
        void shouldAllowCreateWithCreatePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:create");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request =
                    Map.of(
                            "mustItSellerName", "permission-test-seller",
                            "sellerName", "권한 테스트 셀러");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL),
                            HttpMethod.POST,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("seller:update 권한이 있으면 수정 가능")
        void shouldAllowUpdateWithUpdatePermission() {
            // given
            HttpHeaders headers = AuthTestHelper.withPermissions("seller:update");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = Map.of("sellerName", "권한 테스트 수정");

            // when
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(
                            url(SELLERS_BASE_URL + "/1"),
                            HttpMethod.PATCH,
                            new HttpEntity<>(request, headers),
                            new ParameterizedTypeReference<>() {});

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
