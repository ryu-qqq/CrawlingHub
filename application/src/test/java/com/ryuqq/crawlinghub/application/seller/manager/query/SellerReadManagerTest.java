package com.ryuqq.crawlinghub.application.seller.manager.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.seller.manager.SellerReadManager;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
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
 * SellerReadManager 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerReadManager 테스트")
class SellerReadManagerTest {

    @Mock private SellerQueryPort sellerQueryPort;
    @Mock private Seller seller;
    @Mock private SellerQueryCriteria criteria;

    private SellerReadManager manager;

    @BeforeEach
    void setUp() {
        manager = new SellerReadManager(sellerQueryPort);
    }

    @Nested
    @DisplayName("findById() 테스트")
    class FindById {

        @Test
        @DisplayName("[성공] ID로 Seller 조회 위임")
        void shouldDelegateToQueryPort() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(sellerQueryPort.findById(sellerId)).willReturn(Optional.of(seller));

            // When
            Optional<Seller> result = manager.findById(sellerId);

            // Then
            assertThat(result).isPresent().contains(seller);
            verify(sellerQueryPort).findById(sellerId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않는 경우 empty 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(sellerQueryPort.findById(sellerId)).willReturn(Optional.empty());

            // When
            Optional<Seller> result = manager.findById(sellerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById() 테스트")
    class ExistsById {

        @Test
        @DisplayName("[성공] 존재하면 true 반환")
        void shouldReturnTrueWhenExists() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(sellerQueryPort.existsById(sellerId)).willReturn(true);

            // When
            boolean result = manager.existsById(sellerId);

            // Then
            assertThat(result).isTrue();
            verify(sellerQueryPort).existsById(sellerId);
        }

        @Test
        @DisplayName("[성공] 존재하지 않으면 false 반환")
        void shouldReturnFalseWhenNotExists() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(sellerQueryPort.existsById(sellerId)).willReturn(false);

            // When
            boolean result = manager.existsById(sellerId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByMustItSellerName() 테스트")
    class ExistsByMustItSellerName {

        @Test
        @DisplayName("[성공] MustItSellerName 존재 확인 위임")
        void shouldDelegateToQueryPort() {
            // Given
            MustItSellerName mustItSellerName = MustItSellerName.of("test-seller");
            given(sellerQueryPort.existsByMustItSellerName(mustItSellerName)).willReturn(true);

            // When
            boolean result = manager.existsByMustItSellerName(mustItSellerName);

            // Then
            assertThat(result).isTrue();
            verify(sellerQueryPort).existsByMustItSellerName(mustItSellerName);
        }
    }

    @Nested
    @DisplayName("existsBySellerName() 테스트")
    class ExistsBySellerName {

        @Test
        @DisplayName("[성공] SellerName 존재 확인 위임")
        void shouldDelegateToQueryPort() {
            // Given
            SellerName sellerName = SellerName.of("테스트셀러");
            given(sellerQueryPort.existsBySellerName(sellerName)).willReturn(true);

            // When
            boolean result = manager.existsBySellerName(sellerName);

            // Then
            assertThat(result).isTrue();
            verify(sellerQueryPort).existsBySellerName(sellerName);
        }
    }

    @Nested
    @DisplayName("existsByMustItSellerNameExcludingId() 테스트")
    class ExistsByMustItSellerNameExcludingId {

        @Test
        @DisplayName("[성공] 특정 ID 제외 MustItSellerName 중복 확인")
        void shouldDelegateToQueryPort() {
            // Given
            MustItSellerName mustItSellerName = MustItSellerName.of("test-seller");
            SellerId excludeId = SellerId.of(1L);
            given(sellerQueryPort.existsByMustItSellerNameExcludingId(mustItSellerName, excludeId))
                    .willReturn(false);

            // When
            boolean result =
                    manager.existsByMustItSellerNameExcludingId(mustItSellerName, excludeId);

            // Then
            assertThat(result).isFalse();
            verify(sellerQueryPort)
                    .existsByMustItSellerNameExcludingId(mustItSellerName, excludeId);
        }
    }

    @Nested
    @DisplayName("existsBySellerNameExcludingId() 테스트")
    class ExistsBySellerNameExcludingId {

        @Test
        @DisplayName("[성공] 특정 ID 제외 SellerName 중복 확인")
        void shouldDelegateToQueryPort() {
            // Given
            SellerName sellerName = SellerName.of("테스트셀러");
            SellerId excludeId = SellerId.of(1L);
            given(sellerQueryPort.existsBySellerNameExcludingId(sellerName, excludeId))
                    .willReturn(false);

            // When
            boolean result = manager.existsBySellerNameExcludingId(sellerName, excludeId);

            // Then
            assertThat(result).isFalse();
            verify(sellerQueryPort).existsBySellerNameExcludingId(sellerName, excludeId);
        }
    }

    @Nested
    @DisplayName("findByCriteria() 테스트")
    class FindByCriteria {

        @Test
        @DisplayName("[성공] 조건으로 Seller 목록 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(sellerQueryPort.findByCriteria(criteria)).willReturn(List.of(seller));

            // When
            List<Seller> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1).contains(seller);
            verify(sellerQueryPort).findByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 결과 없으면 빈 리스트 반환")
        void shouldReturnEmptyListWhenNoResults() {
            // Given
            given(sellerQueryPort.findByCriteria(criteria)).willReturn(List.of());

            // When
            List<Seller> result = manager.findByCriteria(criteria);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByCriteria() 테스트")
    class CountByCriteria {

        @Test
        @DisplayName("[성공] 조건에 맞는 Seller 개수 조회")
        void shouldDelegateToQueryPort() {
            // Given
            given(sellerQueryPort.countByCriteria(criteria)).willReturn(10L);

            // When
            long result = manager.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(10L);
            verify(sellerQueryPort).countByCriteria(criteria);
        }
    }
}
