package com.ryuqq.crawlinghub.application.seller.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerPageResult;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerResult;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SellerAssembler 단위 테스트
 *
 * <p>Assembler는 stateless 컴포넌트이므로 직접 인스턴스화하여 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SellerAssembler 테스트")
class SellerAssemblerTest {

    private final SellerAssembler assembler = new SellerAssembler();

    @Nested
    @DisplayName("toResult() 테스트")
    class ToResult {

        @Test
        @DisplayName("[성공] Seller → SellerResult 변환")
        void shouldConvertSellerToResult() {
            // Given
            Seller seller = SellerFixture.anActiveSeller();

            // When
            SellerResult result = assembler.toResult(seller);

            // Then
            assertThat(result.id()).isEqualTo(seller.getSellerIdValue());
            assertThat(result.mustItSellerName()).isEqualTo(seller.getMustItSellerNameValue());
            assertThat(result.sellerName()).isEqualTo(seller.getSellerNameValue());
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.createdAt()).isEqualTo(seller.getCreatedAt());
            assertThat(result.updatedAt()).isEqualTo(seller.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("toResults() 테스트")
    class ToResults {

        @Test
        @DisplayName("[성공] Seller 목록 → SellerResult 목록 변환")
        void shouldConvertSellerListToResults() {
            // Given
            List<Seller> sellers =
                    List.of(
                            SellerFixture.anActiveSeller(1L),
                            SellerFixture.anActiveSeller(2L),
                            SellerFixture.anActiveSeller(3L));

            // When
            List<SellerResult> results = assembler.toResults(sellers);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).id()).isEqualTo(1L);
            assertThat(results.get(1).id()).isEqualTo(2L);
            assertThat(results.get(2).id()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptySellers() {
            // Given
            List<Seller> sellers = List.of();

            // When
            List<SellerResult> results = assembler.toResults(sellers);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPageResult() 테스트")
    class ToPageResult {

        @Test
        @DisplayName("[성공] Seller 목록 → SellerPageResult 변환")
        void shouldConvertSellersToPageResult() {
            // Given
            List<Seller> sellers =
                    List.of(SellerFixture.anActiveSeller(1L), SellerFixture.anActiveSeller(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            SellerPageResult result = assembler.toPageResult(sellers, page, size, totalElements);

            // Then
            assertThat(result.results()).hasSize(2);
            assertThat(result.pageMeta().page()).isZero();
            assertThat(result.pageMeta().size()).isEqualTo(10);
            assertThat(result.pageMeta().totalElements()).isEqualTo(25L);
            assertThat(result.pageMeta().totalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → hasNext = false")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<Seller> sellers = List.of(SellerFixture.anActiveSeller(1L));
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            SellerPageResult result = assembler.toPageResult(sellers, page, size, totalElements);

            // Then
            assertThat(result.pageMeta().isFirst()).isFalse();
            assertThat(result.pageMeta().isLast()).isTrue();
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
            SellerPageResult result = assembler.toPageResult(sellers, page, size, totalElements);

            // Then
            assertThat(result.pageMeta().isFirst()).isTrue();
            assertThat(result.pageMeta().isLast()).isTrue();
            assertThat(result.pageMeta().totalPages()).isEqualTo(1);
        }
    }
}
