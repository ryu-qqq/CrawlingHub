package com.ryuqq.crawlinghub.application.seller.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.util.List;
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
        @DisplayName("[성공] Seller → SellerSummaryResponse 변환")
        void shouldConvertSellerToSummaryResponse() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();

            // When
            SellerSummaryResponse result = assembler.toSummaryResponse(seller);

            // Then
            assertThat(result.sellerId()).isEqualTo(seller.getSellerIdValue());
            assertThat(result.mustItSellerName()).isEqualTo(seller.getMustItSellerNameValue());
            assertThat(result.sellerName()).isEqualTo(seller.getSellerNameValue());
            assertThat(result.active()).isTrue();
            assertThat(result.createdAt()).isEqualTo(seller.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toSummaryResponses() 테스트")
    class ToSummaryResponses {

        @Test
        @DisplayName("[성공] Seller 목록 → SellerSummaryResponse 목록 변환")
        void shouldConvertSellerListToSummaryResponses() {
            // Given
            List<Seller> sellers =
                    List.of(
                            SellerFixture.anActiveSeller(1L),
                            SellerFixture.anActiveSeller(2L),
                            SellerFixture.anActiveSeller(3L));

            // When
            List<SellerSummaryResponse> result = assembler.toSummaryResponses(sellers);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).sellerId()).isEqualTo(1L);
            assertThat(result.get(1).sellerId()).isEqualTo(2L);
            assertThat(result.get(2).sellerId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptySellers() {
            // Given
            List<Seller> sellers = List.of();

            // When
            List<SellerSummaryResponse> result = assembler.toSummaryResponses(sellers);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteria {

        @Test
        @DisplayName("[성공] SearchSellersQuery → SellerQueryCriteria 변환 (전체 필드)")
        void shouldConvertQueryToCriteria() {
            // Given
            SearchSellersQuery query =
                    new SearchSellersQuery("MUSTIT_001", "테스트셀러", SellerStatus.ACTIVE, 0, 20);

            // When
            SellerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.mustItSellerName()).isNotNull();
            assertThat(result.sellerName()).isNotNull();
            assertThat(result.status()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] SearchSellersQuery → SellerQueryCriteria 변환 (null 필드)")
        void shouldConvertQueryToCriteriaWithNullFields() {
            // Given
            SearchSellersQuery query = new SearchSellersQuery(null, null, null, 0, 10);

            // When
            SellerQueryCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.mustItSellerName()).isNull();
            assertThat(result.sellerName()).isNull();
            assertThat(result.status()).isNull();
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponse {

        @Test
        @DisplayName("[성공] Seller 목록 → PageResponse 변환")
        void shouldConvertSellersToPageResponse() {
            // Given
            List<Seller> sellers =
                    List.of(SellerFixture.anActiveSeller(1L), SellerFixture.anActiveSeller(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, page, size, totalElements);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25L);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → last = true")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, page, size, totalElements);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] 첫 페이지이자 마지막 페이지")
        void shouldReturnFirstAndLastForSinglePage() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            int page = 0;
            int size = 10;
            long totalElements = 5L;

            // When
            PageResponse<SellerSummaryResponse> result =
                    assembler.toPageResponse(sellers, page, size, totalElements);

            // Then
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            assertThat(result.totalPages()).isEqualTo(1);
        }
    }
}
