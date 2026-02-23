package com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query.SearchSellersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerSearchParams;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
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
    @DisplayName("toDetailApiResponse(SellerDetailResult) - 상세 응답 변환")
    class ToDetailApiResponseTests {

        @Test
        @DisplayName("성공: SellerDetailResult → SellerDetailApiResponse 변환")
        void toDetailApiResponse_Success() {
            // Given
            Instant now = Instant.parse("2025-11-19T10:30:00Z");
            SellerDetailResult result =
                    new SellerDetailResult(
                            new SellerDetailResult.SellerInfo(
                                    1L, "머스트잇셀러", "커머스셀러", "ACTIVE", 100, now, now),
                            List.of(
                                    new SellerDetailResult.SchedulerSummary(
                                            1L, "일일 크롤링", "ACTIVE", "0 0 9 * * ?")),
                            List.of(
                                    new SellerDetailResult.TaskSummary(
                                            1L, "SUCCESS", "FULL_SYNC", now, now)),
                            new SellerDetailResult.SellerStatistics(100L, 95L, 5L, 0.95));

            // When
            SellerDetailApiResponse apiResponse = mapper.toDetailApiResponse(result);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(1L);
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇셀러");
            assertThat(apiResponse.sellerName()).isEqualTo("커머스셀러");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
            assertThat(apiResponse.schedulers()).hasSize(1);
            assertThat(apiResponse.schedulers().get(0).schedulerId()).isEqualTo(1L);
            assertThat(apiResponse.recentTasks()).hasSize(1);
            assertThat(apiResponse.recentTasks().get(0).taskId()).isEqualTo(1L);
            assertThat(apiResponse.statistics().totalProducts()).isEqualTo(100L);
            assertThat(apiResponse.statistics().successRate()).isEqualTo(0.95);
        }

        @Test
        @DisplayName("성공: 빈 목록 포함 결과 변환")
        void toDetailApiResponse_EmptyLists() {
            // Given
            Instant now = Instant.now();
            SellerDetailResult result =
                    new SellerDetailResult(
                            new SellerDetailResult.SellerInfo(
                                    2L, "머스트잇셀러2", "커머스셀러2", "INACTIVE", 0, now, null),
                            List.of(),
                            List.of(),
                            new SellerDetailResult.SellerStatistics(0L, 0L, 0L, 0.0));

            // When
            SellerDetailApiResponse apiResponse = mapper.toDetailApiResponse(result);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(2L);
            assertThat(apiResponse.status()).isEqualTo("INACTIVE");
            assertThat(apiResponse.schedulers()).isEmpty();
            assertThat(apiResponse.recentTasks()).isEmpty();
            assertThat(apiResponse.statistics().totalProducts()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("toSearchParams(SearchSellersApiRequest) - 목록 조회 요청 변환")
    class ToSearchParamsTests {

        @Test
        @DisplayName("성공: status=ACTIVE 변환")
        void toSearchParams_StatusActive() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(
                            null, null, List.of("ACTIVE"), null, null, null, null, 0, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.statuses()).containsExactly("ACTIVE");
            assertThat(params.page()).isEqualTo(0);
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("성공: status=null (전체 조회)")
        void toSearchParams_StatusNull() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(null, null, null, null, null, null, null, null, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.statuses()).isNull();
        }

        @Test
        @DisplayName("성공: status=빈 리스트 (전체 조회)")
        void toSearchParams_StatusBlank() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(
                            null, null, List.of(), null, null, null, null, 0, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.statuses()).isEmpty();
        }

        @Test
        @DisplayName("성공: 기본 페이징 파라미터")
        void toSearchParams_DefaultPagination() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(null, null, null, null, null, null, null, null, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.page()).isEqualTo(0);
            assertThat(params.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("성공: 커스텀 페이징 파라미터")
        void toSearchParams_CustomPagination() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(
                            null, null, List.of("ACTIVE"), null, null, null, null, 5, 50);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.page()).isEqualTo(5);
            assertThat(params.size()).isEqualTo(50);
        }

        @Test
        @DisplayName("성공: sellerName, mustItSellerName 필터 변환")
        void toSearchParams_WithNameFilters() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(
                            "테스트셀러", "머스트잇셀러", List.of("ACTIVE"), null, null, null, null, 0, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.sellerName()).isEqualTo("테스트셀러");
            assertThat(params.mustItSellerName()).isEqualTo("머스트잇셀러");
            assertThat(params.statuses()).containsExactly("ACTIVE");
        }

        @Test
        @DisplayName("성공: createdFrom, createdTo 필터 변환")
        void toSearchParams_WithDateFilters() {
            // Given
            Instant from = Instant.parse("2024-01-01T00:00:00Z");
            Instant to = Instant.parse("2024-12-31T23:59:59Z");
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(null, null, null, from, to, null, null, 0, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.createdFrom()).isEqualTo(from);
            assertThat(params.createdTo()).isEqualTo(to);
        }

        @Test
        @DisplayName("성공: sortKey, sortDirection 변환")
        void toSearchParams_WithSortParams() {
            // Given
            SearchSellersApiRequest request =
                    new SearchSellersApiRequest(
                            null, null, null, null, null, "sellerName", "ASC", 0, 20);

            // When
            SellerSearchParams params = mapper.toSearchParams(request);

            // Then
            assertThat(params).isNotNull();
            assertThat(params.sortKey()).isEqualTo("sellerName");
            assertThat(params.sortDirection()).isEqualTo("ASC");
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse(SellerResult) - 요약 응답 변환")
    class ToSummaryApiResponseTests {

        @Test
        @DisplayName("성공: ACTIVE 셀러 변환")
        void toSummaryApiResponse_Active() {
            // Given
            SellerResult result =
                    new SellerResult(
                            1L, "머스트잇 테스트 셀러", "테스트 셀러", "ACTIVE", Instant.now(), Instant.now());

            // When
            SellerSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(result);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(1L);
            assertThat(apiResponse.mustItSellerName()).isEqualTo("머스트잇 테스트 셀러");
            assertThat(apiResponse.sellerName()).isEqualTo("테스트 셀러");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
        }

        @Test
        @DisplayName("성공: INACTIVE 셀러 변환")
        void toSummaryApiResponse_Inactive() {
            // Given
            SellerResult result =
                    new SellerResult(2L, "머스트잇 테스트 셀러", "테스트 셀러", "INACTIVE", Instant.now(), null);

            // When
            SellerSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(result);

            // Then
            assertThat(apiResponse).isNotNull();
            assertThat(apiResponse.sellerId()).isEqualTo(2L);
            assertThat(apiResponse.status()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("toPageResponse(SellerPageResult) - 페이징 응답 변환")
    class ToPageResponseTests {

        @Test
        @DisplayName("성공: 첫 페이지 변환 (first=true, last=false)")
        void toPageResponse_FirstPage() {
            // Given
            List<SellerResult> results =
                    List.of(
                            new SellerResult(
                                    1L, "머스트잇 셀러1", "셀러1", "ACTIVE", Instant.now(), Instant.now()),
                            new SellerResult(
                                    2L, "머스트잇 셀러2", "셀러2", "ACTIVE", Instant.now(), Instant.now()));

            SellerPageResult pageResult = SellerPageResult.of(results, PageMeta.of(0, 20, 100L));

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

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
        void toPageResponse_LastPage() {
            // Given
            List<SellerResult> results =
                    List.of(
                            new SellerResult(
                                    99L,
                                    "머스트잇 셀러99",
                                    "셀러99",
                                    "ACTIVE",
                                    Instant.now(),
                                    Instant.now()),
                            new SellerResult(
                                    100L, "머스트잇 셀러100", "셀러100", "INACTIVE", Instant.now(), null));

            SellerPageResult pageResult = SellerPageResult.of(results, PageMeta.of(4, 20, 100L));

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(2);
            assertThat(apiPageResponse.page()).isEqualTo(4);
            assertThat(apiPageResponse.first()).isFalse();
            assertThat(apiPageResponse.last()).isTrue();
        }

        @Test
        @DisplayName("성공: 빈 결과 변환")
        void toPageResponse_EmptyContent() {
            // Given
            SellerPageResult pageResult = SellerPageResult.empty();

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

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
        void toPageResponse_SinglePage() {
            // Given
            List<SellerResult> results =
                    List.of(
                            new SellerResult(
                                    1L, "머스트잇 셀러1", "셀러1", "ACTIVE", Instant.now(), Instant.now()));

            SellerPageResult pageResult = SellerPageResult.of(results, PageMeta.of(0, 20, 1L));

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

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
        void toPageResponse_MixedStatus() {
            // Given
            List<SellerResult> results =
                    List.of(
                            new SellerResult(
                                    1L, "머스트잇 셀러1", "셀러1", "ACTIVE", Instant.now(), Instant.now()),
                            new SellerResult(
                                    2L, "머스트잇 셀러2", "셀러2", "INACTIVE", Instant.now(), null),
                            new SellerResult(
                                    3L, "머스트잇 셀러3", "셀러3", "ACTIVE", Instant.now(), Instant.now()));

            SellerPageResult pageResult = SellerPageResult.of(results, PageMeta.of(0, 20, 3L));

            // When
            PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

            // Then
            assertThat(apiPageResponse).isNotNull();
            assertThat(apiPageResponse.content()).hasSize(3);
            assertThat(apiPageResponse.content().get(0).status()).isEqualTo("ACTIVE");
            assertThat(apiPageResponse.content().get(1).status()).isEqualTo("INACTIVE");
            assertThat(apiPageResponse.content().get(2).status()).isEqualTo("ACTIVE");
        }
    }
}
