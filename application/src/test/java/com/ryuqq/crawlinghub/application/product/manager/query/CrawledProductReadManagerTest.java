package com.ryuqq.crawlinghub.application.product.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.manager.CrawledProductReadManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductReadManager 테스트")
class CrawledProductReadManagerTest {

    @Mock private CrawledProductQueryPort crawledProductQueryPort;
    @Mock private CrawledProduct crawledProduct;

    private CrawledProductReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new CrawledProductReadManager(crawledProductQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 CrawledProduct 조회")
        void shouldDelegateToQueryPort() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(1L);
            given(crawledProductQueryPort.findById(crawledProductId))
                    .willReturn(Optional.of(crawledProduct));

            // When
            Optional<CrawledProduct> result = manager.findById(crawledProductId);

            // Then
            assertThat(result).isPresent().contains(crawledProduct);
            verify(crawledProductQueryPort).findById(crawledProductId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            CrawledProductId crawledProductId = CrawledProductId.of(999L);
            given(crawledProductQueryPort.findById(crawledProductId)).willReturn(Optional.empty());

            // When
            Optional<CrawledProduct> result = manager.findById(crawledProductId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySellerIdAndItemNo() 테스트")
    class FindBySellerIdAndItemNo {

        @Test
        @DisplayName("[성공] Seller ID와 Item No로 조회")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            long itemNo = 12345L;
            given(crawledProductQueryPort.findBySellerIdAndItemNo(sellerId, itemNo))
                    .willReturn(Optional.of(crawledProduct));

            // When
            Optional<CrawledProduct> result = manager.findBySellerIdAndItemNo(sellerId, itemNo);

            // Then
            assertThat(result).isPresent().contains(crawledProduct);
            verify(crawledProductQueryPort).findBySellerIdAndItemNo(sellerId, itemNo);
        }
    }

    @Nested
    @DisplayName("findBySellerId() 테스트")
    class FindBySellerId {

        @Test
        @DisplayName("[성공] Seller ID로 CrawledProduct 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(crawledProductQueryPort.findBySellerId(sellerId))
                    .willReturn(List.of(crawledProduct));

            // When
            List<CrawledProduct> result = manager.findBySellerId(sellerId);

            // Then
            assertThat(result).hasSize(1).contains(crawledProduct);
            verify(crawledProductQueryPort).findBySellerId(sellerId);
        }
    }

    @Nested
    @DisplayName("findNeedsSyncProducts() 테스트")
    class FindNeedsSyncProducts {

        @Test
        @DisplayName("[성공] 동기화 필요한 상품 조회")
        void shouldDelegateToQueryPort() {
            // Given
            int limit = 100;
            given(crawledProductQueryPort.findNeedsSyncProducts(limit))
                    .willReturn(List.of(crawledProduct));

            // When
            List<CrawledProduct> result = manager.findNeedsSyncProducts(limit);

            // Then
            assertThat(result).hasSize(1).contains(crawledProduct);
            verify(crawledProductQueryPort).findNeedsSyncProducts(limit);
        }
    }

    @Nested
    @DisplayName("existsBySellerIdAndItemNo() 테스트")
    class ExistsBySellerIdAndItemNo {

        @Test
        @DisplayName("[성공] 존재하면 true 반환")
        void shouldReturnTrueWhenExists() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            long itemNo = 12345L;
            given(crawledProductQueryPort.existsBySellerIdAndItemNo(sellerId, itemNo))
                    .willReturn(true);

            // When
            boolean result = manager.existsBySellerIdAndItemNo(sellerId, itemNo);

            // Then
            assertThat(result).isTrue();
            verify(crawledProductQueryPort).existsBySellerIdAndItemNo(sellerId, itemNo);
        }

        @Test
        @DisplayName("[성공] 존재하지 않으면 false 반환")
        void shouldReturnFalseWhenNotExists() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            long itemNo = 99999L;
            given(crawledProductQueryPort.existsBySellerIdAndItemNo(sellerId, itemNo))
                    .willReturn(false);

            // When
            boolean result = manager.existsBySellerIdAndItemNo(sellerId, itemNo);

            // Then
            assertThat(result).isFalse();
        }
    }
}
