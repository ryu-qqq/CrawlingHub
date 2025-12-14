package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.GetSellerQuery;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SellerQueryApiMapper 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>API Request → Application Query 변환
 *   <li>Application Response → API Response 변환
 *   <li>페이징 응답 변환
 *   <li>Enum 변환 (String ↔ SellerStatus)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerQueryApiMapper 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class SellerQueryApiMapperTest {

    private SellerQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SellerQueryApiMapper();
    }

    @Nested
    @DisplayName("toQuery(Long) - ID → GetSellerQuery 변환")
    class ToGetSellerQueryTests {

        @Test
        @DisplayName("성공: Long ID → GetSellerQuery 변환")
        void toQuery_GetSeller_Success() {
            // Given
            Long sellerId = 1L;

            // When
            GetSellerQuery query = mapper.toQuery(sellerId);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공: 큰 ID 값 변환")
        void toQuery_GetSeller_LargeId() {
            // Given
            Long sellerId = 999999999L;

            // When
            GetSellerQuery query = mapper.toQuery(sellerId);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerId()).isEqualTo(999999999L);
        }
    }

    @Nested
    @DisplayName("toQuery(SearchSellersApiRequest) - 목록 조회 요청 변환")
    class ToSearchSellersQueryTests {

        @Test
        @DisplayName("성공: status=ACTIVE 변환")
        void toQuery_SearchSellers_StatusActive() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest("ACTIVE", 0, 20);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("성공: status=INACTIVE 변환")
        void toQuery_SearchSellers_StatusInactive() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest("INACTIVE", 0, 20);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerStatus()).isEqualTo(SellerStatus.INACTIVE);
        }

        @Test
        @DisplayName("성공: status=null (전체 조회)")
        void toQuery_SearchSellers_StatusNull() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest(null, 0, 20);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerStatus()).isNull();
        }

        @Test
        @DisplayName("성공: status=빈 문자열 (전체 조회)")
        void toQuery_SearchSellers_StatusBlank() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest("", 0, 20);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.sellerStatus()).isNull();
        }

        @Test
        @DisplayName("성공: 기본 페이징 파라미터")
        void toQuery_SearchSellers_DefaultPagination() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest(null, 0, 20);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.page()).isEqualTo(0);
            assertThat(query.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("성공: 커스텀 페이징 파라미터")
        void toQuery_SearchSellers_CustomPagination() {
            // Given
            SearchSellersApiRequest request = new SearchSellersApiRequest("ACTIVE", 5, 50);

            // When
            SearchSellersQuery query = mapper.toQuery(request);

            // Then
            assertThat(query).isNotNull();
            assertThat(query.page()).isEqualTo(5);
            assertThat(query.size()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("toApiResponse(SellerResponse) - 단건 응답 변환")
    class ToApiResponseTests {

        @Test
        @DisplayName("성공: active=true → status=ACTIVE 변환")
        void toApiResponse_ActiveTrue() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            1L,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            true,
                            Instant.parse("2024-11-27T10:00:00Z"),
                            null);

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(1L);
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇 테스트 셀러");
            assertThat(apiResponse.sellerName()).isEqualTo("테스트 셀러");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
            assertThat(apiResponse.createdAt()).isEqualTo("2024-11-27T10:00:00Z");
            assertThat(apiResponse.updatedAt()).isNull();
        }

        @Test
        @DisplayName("성공: active=false → status=INACTIVE 변환")
        void toApiResponse_ActiveFalse() {
            // Given
            SellerResponse appResponse =
                    new SellerResponse(
                            2L,
                            "머스트잇 테스트 셀러",
                            "테스트 셀러",
                            false,
                            Instant.parse("2024-11-27T10:00:00Z"),
                            Instant.parse("2024-11-27T11:00:00Z"));

            // When
            SellerApiResponse apiResponse = mapper.toApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.status()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse(SellerSummaryResponse) - 요약 응답 변환")
    class ToSummaryApiResponseTests {

        @Test
        @DisplayName("성공: active=true → status=ACTIVE 변환")
        void toSummaryApiResponse_ActiveTrue() {
            // Given
            SellerSummaryResponse appResponse =
                    new SellerSummaryResponse(1L, "머스트잇 테스트 셀러", "테스트 셀러", true, Instant.now());

            // When
            SellerSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(1L);
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇 테스트 셀러");
            assertThat(apiResponse.sellerName()).isEqualTo("테스트 셀러");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("성공: active=false → status=INACTIVE 변환")
        void toSummaryApiResponse_ActiveFalse() {
            // Given
            SellerSummaryResponse appResponse =
                    new SellerSummaryResponse(2L, "머스트잇 테스트 셀러", "테스트 셀러", false, Instant.now());

            // When
            SellerSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(appResponse);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(2L);
            assertThat(apiResponse.status()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("toPageApiResponse(PageResponse) - 페이징 응답 변환")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("성공: 첫 페이지 변환 (first=true, last=false)")
        void toPageApiResponse_FirstPage() {
            // Given
            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(1L, "머스트잇 셀러1", "셀러1", true, Instant.now()),
                            new SellerSummaryResponse(2L, "머스트잇 셀러2", "셀러2", true, Instant.now()));

            PageResponse<SellerSummaryResponse> appPageResponse =
                    new PageResponse<>(content, 0, 20, 100L, 5, true, false);

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(2);
            assertThat(apiPageResponse.content().get(0).sellerId()).isEqualTo(1L);
            assertThat(apiPageResponse.content().get(0).status()).isEqualTo("ACTIVE");
            assertThat(apiPageResponse.content().get(1).sellerId()).isEqualTo(2L);
            assertThat(apiPageResponse.page()).isEqualTo(0);
            assertThat(apiPageResponse.size()).isEqualTo(20);
            assertThat(apiPageResponse.totalElements()).isEqualTo(100L);
            assertThat(apiPageResponse.totalPages()).isEqualTo(5);
            assertThat(apiPageResponse.first()).isTrue();
            assertThat(apiPageResponse.last()).isFalse();
        }

        @Test
        @DisplayName("성공: 마지막 페이지 변환 (first=false, last=true)")
        void toPageApiResponse_LastPage() {
            // Given
            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(
                                    99L, "머스트잇 셀러99", "셀러99", true, Instant.now()),
                            new SellerSummaryResponse(
                                    100L, "머스트잇 셀러100", "셀러100", false, Instant.now()));

            PageResponse<SellerSummaryResponse> appPageResponse =
                    new PageResponse<>(content, 4, 20, 100L, 5, false, true);

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(2);
            assertThat(apiPageResponse.page()).isEqualTo(4);
            assertThat(apiPageResponse.first()).isFalse();
            assertThat(apiPageResponse.last()).isTrue();
        }

        @Test
        @DisplayName("성공: 빈 결과 변환")
        void toPageApiResponse_EmptyContent() {
            // Given
            PageResponse<SellerSummaryResponse> appPageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).isEmpty();
            assertThat(apiPageResponse.totalElements()).isEqualTo(0L);
            assertThat(apiPageResponse.totalPages()).isEqualTo(0);
            assertThat(apiPageResponse.first()).isTrue();
            assertThat(apiPageResponse.last()).isTrue();
        }

        @Test
        @DisplayName("성공: 단일 페이지 (first=true, last=true)")
        void toPageApiResponse_SinglePage() {
            // Given
            List<SellerSummaryResponse> content =
                    List.of(new SellerSummaryResponse(1L, "머스트잇 셀러1", "셀러1", true, Instant.now()));

            PageResponse<SellerSummaryResponse> appPageResponse =
                    new PageResponse<>(content, 0, 20, 1L, 1, true, true);

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(1);
            assertThat(apiPageResponse.totalElements()).isEqualTo(1L);
            assertThat(apiPageResponse.totalPages()).isEqualTo(1);
            assertThat(apiPageResponse.first()).isTrue();
            assertThat(apiPageResponse.last()).isTrue();
        }

        @Test
        @DisplayName("성공: ACTIVE와 INACTIVE 혼합 변환")
        void toPageApiResponse_MixedStatus() {
            // Given
            List<SellerSummaryResponse> content =
                    List.of(
                            new SellerSummaryResponse(1L, "머스트잇 셀러1", "셀러1", true, Instant.now()),
                            new SellerSummaryResponse(2L, "머스트잇 셀러2", "셀러2", false, Instant.now()),
                            new SellerSummaryResponse(3L, "머스트잇 셀러3", "셀러3", true, Instant.now()));

            PageResponse<SellerSummaryResponse> appPageResponse =
                    new PageResponse<>(content, 0, 20, 3L, 1, true, true);

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageApiResponse(appPageResponse);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(3);
            assertThat(apiPageResponse.content().get(0).status()).isEqualTo("ACTIVE");
            assertThat(apiPageResponse.content().get(1).status()).isEqualTo("INACTIVE");
            assertThat(apiPageResponse.content().get(2).status()).isEqualTo("ACTIVE");
        }
    }
}
