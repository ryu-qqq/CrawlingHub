package com.ryuqq.crawlinghub.application.seller.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.SellerStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SellerAssembler 단위 테스트
 *
 * <p>Assembler는 stateless 컴포넌트이므로 직접 인스턴스화하여 테스트
 *
 * <p><strong>주의</strong>: Command → Domain 변환 테스트는 {@code SellerCommandFactoryTest}에서 담당
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerAssembler 테스트")
class SellerAssemblerTest {

    private final SellerAssembler assembler = new SellerAssembler();

    @Nested
    @DisplayName("toResponse() 테스트")
    class ToResponse {

        @Test
        @DisplayName("[성공] Seller → SellerResponse 변환")
        void shouldConvertSellerToResponse() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();

            // When
            SellerResponse result = assembler.toResponse(seller);

            // Then
            assertThat(result.sellerId()).isEqualTo(seller.getSellerIdValue());
            assertThat(result.mustItSellerName()).isEqualTo(seller.getMustItSellerNameValue());
            assertThat(result.sellerName()).isEqualTo(seller.getSellerNameValue());
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(seller.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(seller.getUpdatedAt());
        }

        @Test
        @DisplayName("[성공] 비활성 Seller → SellerResponse 변환")
        void shouldConvertInactiveSellerToResponse() {
            // Given
            Seller seller = SellerFixture.anInactiveSeller();

            // When
            SellerResponse result = assembler.toResponse(seller);

            // Then
            assertThat(result.active()).isFalse();
        }
    }

    @Nested
    @DisplayName("toSummaryResponse() 테스트")
    class ToSummaryResponse {

        @Test
        @DisplayName("[성공] Seller + 통계 → SellerSummaryResponse 변환")
        void shouldConvertSellerToSummaryResponse() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();
            SellerStatistics statistics =
                    new SellerStatistics(3, 5, "COMPLETED", Instant.now(), 100L);

            // When
            SellerSummaryResponse result = assembler.toSummaryResponse(seller, statistics);

            // Then
            assertThat(result.sellerId()).isEqualTo(seller.getSellerIdValue());
            assertThat(result.mustItSellerName()).isEqualTo(seller.getMustItSellerNameValue());
            assertThat(result.sellerName()).isEqualTo(seller.getSellerNameValue());
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(seller.getCreatedAt());
            assertThat(result.activeSchedulerCount()).isEqualTo(3);
            assertThat(result.totalSchedulerCount()).isEqualTo(5);
            assertThat(result.lastTaskStatus()).isEqualTo("COMPLETED");
            assertThat(result.totalProductCount()).isEqualTo(100L);
        }

        @Test
        @DisplayName("[성공] 빈 통계 → 기본값 포함 변환")
        void shouldConvertSellerWithEmptyStatistics() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();
            SellerStatistics statistics = SellerStatistics.empty();

            // When
            SellerSummaryResponse result = assembler.toSummaryResponse(seller, statistics);

            // Then
            assertThat(result.activeSchedulerCount()).isZero();
            assertThat(result.totalSchedulerCount()).isZero();
            assertThat(result.lastTaskStatus()).isNull();
            assertThat(result.lastTaskExecutedAt()).isNull();
            assertThat(result.totalProductCount()).isZero();
        }
    }

    @Nested
    @DisplayName("toSummaryResponses() 테스트")
    class ToSummaryResponses {

        @Test
        @DisplayName("[성공] Seller 목록 + 통계 맵 → SellerSummaryResponse 목록 변환")
        void shouldConvertSellerListToSummaryResponses() {
            // Given
            List<Seller> sellers =
                    List.of(
                            SellerFixture.anActiveSeller(1L),
                            SellerFixture.anActiveSeller(2L),
                            SellerFixture.anActiveSeller(3L));
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();
            statisticsMap.put(
                    SellerId.of(1L), new SellerStatistics(2, 3, "COMPLETED", Instant.now(), 50L));
            statisticsMap.put(
                    SellerId.of(2L), new SellerStatistics(1, 2, "RUNNING", Instant.now(), 30L));
            statisticsMap.put(SellerId.of(3L), SellerStatistics.empty());

            // When
            List<SellerSummaryResponse> result =
                    assembler.toSummaryResponses(sellers, statisticsMap);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).sellerId()).isEqualTo(1L);
            assertThat(result.get(0).activeSchedulerCount()).isEqualTo(2);
            assertThat(result.get(1).sellerId()).isEqualTo(2L);
            assertThat(result.get(1).lastTaskStatus()).isEqualTo("RUNNING");
            assertThat(result.get(2).sellerId()).isEqualTo(3L);
            assertThat(result.get(2).totalProductCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptySellers() {
            // Given
            List<Seller> sellers = List.of();
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();

            // When
            List<SellerSummaryResponse> result =
                    assembler.toSummaryResponses(sellers, statisticsMap);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[성공] 통계 맵에 없는 셀러 → 빈 통계 적용")
        void shouldApplyEmptyStatisticsForMissingSeller() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>(); // 빈 맵

            // When
            List<SellerSummaryResponse> result =
                    assembler.toSummaryResponses(sellers, statisticsMap);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).activeSchedulerCount()).isZero();
            assertThat(result.get(0).totalSchedulerCount()).isZero();
            assertThat(result.get(0).lastTaskStatus()).isNull();
            assertThat(result.get(0).totalProductCount()).isZero();
        }
    }

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteria {

        @Test
        @DisplayName("[성공] SearchSellersQuery → SellerQueryCriteria 변환 (전체 필드)")
        void shouldConvertQueryToCriteria() {
            // Given
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            SearchSellersQuery query =
                    new SearchSellersQuery(
                            "MUSTIT_001",
                            "테스트셀러",
                            List.of(SellerStatus.ACTIVE),
                            createdFrom,
                            createdTo,
                            0,
                            20);

            // When
            SellerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.mustItSellerName()).isNotNull();
            assertThat(result.sellerName()).isNotNull();
            assertThat(result.status()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(result.createdFrom()).isEqualTo(createdFrom);
            assertThat(result.createdTo()).isEqualTo(createdTo);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] SearchSellersQuery → SellerQueryCriteria 변환 (null 필드)")
        void shouldConvertQueryToCriteriaWithNullFields() {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, null, null, 0, 10);

            // When
            SellerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.mustItSellerName()).isNull();
            assertThat(result.sellerName()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.createdFrom()).isNull();
            assertThat(result.createdTo()).isNull();
        }

        @Test
        @DisplayName("[성공] SearchSellersQuery → SellerQueryCriteria 변환 (날짜 필터만)")
        void shouldConvertQueryToCriteriaWithDateFiltersOnly() {
            // Given
            Instant createdFrom = Instant.parse("2024-06-01T00:00:00Z");
            SearchSellersQuery query =
                    new SearchSellersQuery(null, null, null, createdFrom, null, 0, 10);

            // When
            SellerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.mustItSellerName()).isNull();
            assertThat(result.sellerName()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.createdFrom()).isEqualTo(createdFrom);
            assertThat(result.createdTo()).isNull();
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponse {

        @Test
        @DisplayName("[성공] Seller 목록 + 통계 맵 → PageResponse 변환")
        void shouldConvertSellersToPageResponse() {
            // Given
            List<Seller> sellers =
                    List.of(SellerFixture.anActiveSeller(1L), SellerFixture.anActiveSeller(2L));
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();
            statisticsMap.put(
                    SellerId.of(1L), new SellerStatistics(2, 3, "COMPLETED", Instant.now(), 50L));
            statisticsMap.put(SellerId.of(2L), SellerStatistics.empty());
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, statisticsMap, page, size, totalElements);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25L);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isFalse();
            // 통계 검증
            assertThat(result.content().get(0).activeSchedulerCount()).isEqualTo(2);
            assertThat(result.content().get(1).activeSchedulerCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → last = true")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();
            statisticsMap.put(SellerId.of(1L), SellerStatistics.empty());
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, statisticsMap, page, size, totalElements);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] 첫 페이지이자 마지막 페이지")
        void shouldReturnFirstAndLastForSinglePage() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            Map<SellerId, SellerStatistics> statisticsMap = new HashMap<>();
            statisticsMap.put(SellerId.of(1L), SellerStatistics.empty());
            int page = 0;
            int size = 10;
            long totalElements = 5L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, statisticsMap, page, size, totalElements);

            // Then
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            assertThat(result.totalPages()).isEqualTo(1);
        }
    }
}
