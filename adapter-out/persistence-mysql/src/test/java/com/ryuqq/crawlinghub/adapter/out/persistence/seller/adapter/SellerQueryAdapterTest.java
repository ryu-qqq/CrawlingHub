package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.SellerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.SellerQueryDslRepository;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SellerQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("SellerQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SellerQueryAdapterTest {

    @Mock private SellerQueryDslRepository queryDslRepository;

    @Mock private SellerJpaEntityMapper sellerJpaEntityMapper;

    private SellerQueryAdapter sellerQueryAdapter;

    @BeforeEach
    void setUp() {
        sellerQueryAdapter = new SellerQueryAdapter(queryDslRepository, sellerJpaEntityMapper);
    }

    @Nested
    @DisplayName("findById 테스트")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - ID로 Seller 조회")
        void shouldFindSellerById() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            LocalDateTime now = LocalDateTime.now();
            SellerJpaEntity entity =
                    SellerJpaEntity.of(1L, "mustit", "commerce", SellerStatus.ACTIVE, 0, now, now);
            Seller domain = SellerFixture.anActiveSeller();

            given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
            given(sellerJpaEntityMapper.toDomain(entity)).willReturn(domain);

            // When
            Optional<Seller> result = sellerQueryAdapter.findById(sellerId);

            // Then
            assertThat(result).isPresent();
            verify(queryDslRepository).findById(1L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 ID 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

            // When
            Optional<Seller> result = sellerQueryAdapter.findById(sellerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById 테스트")
    class ExistsByIdTests {

        @Test
        @DisplayName("성공 - ID 존재 확인 true")
        void shouldReturnTrueWhenExists() {
            // Given
            SellerId sellerId = SellerId.of(1L);
            given(queryDslRepository.existsById(1L)).willReturn(true);

            // When
            boolean result = sellerQueryAdapter.existsById(sellerId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - ID 존재 확인 false")
        void shouldReturnFalseWhenNotExists() {
            // Given
            SellerId sellerId = SellerId.of(999L);
            given(queryDslRepository.existsById(999L)).willReturn(false);

            // When
            boolean result = sellerQueryAdapter.existsById(sellerId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByMustItSellerName 테스트")
    class ExistsByMustItSellerNameTests {

        @Test
        @DisplayName("성공 - MustItSellerName 중복 확인")
        void shouldCheckMustItSellerNameExists() {
            // Given
            MustItSellerName mustItSellerName = MustItSellerName.of("test-mustit");
            given(queryDslRepository.existsByMustItSellerName("test-mustit")).willReturn(true);

            // When
            boolean result = sellerQueryAdapter.existsByMustItSellerName(mustItSellerName);

            // Then
            assertThat(result).isTrue();
            verify(queryDslRepository).existsByMustItSellerName("test-mustit");
        }
    }

    @Nested
    @DisplayName("existsBySellerName 테스트")
    class ExistsBySellerNameTests {

        @Test
        @DisplayName("성공 - SellerName 중복 확인")
        void shouldCheckSellerNameExists() {
            // Given
            SellerName sellerName = SellerName.of("test-seller");
            given(queryDslRepository.existsBySellerName("test-seller")).willReturn(false);

            // When
            boolean result = sellerQueryAdapter.existsBySellerName(sellerName);

            // Then
            assertThat(result).isFalse();
            verify(queryDslRepository).existsBySellerName("test-seller");
        }
    }

    @Nested
    @DisplayName("existsByMustItSellerNameExcludingId 테스트")
    class ExistsByMustItSellerNameExcludingIdTests {

        @Test
        @DisplayName("성공 - ID 제외 MustItSellerName 중복 확인")
        void shouldCheckExcludingId() {
            // Given
            MustItSellerName mustItSellerName = MustItSellerName.of("test-mustit");
            SellerId excludeSellerId = SellerId.of(1L);
            given(queryDslRepository.existsByMustItSellerNameExcludingId("test-mustit", 1L))
                    .willReturn(false);

            // When
            boolean result =
                    sellerQueryAdapter.existsByMustItSellerNameExcludingId(
                            mustItSellerName, excludeSellerId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("existsBySellerNameExcludingId 테스트")
    class ExistsBySellerNameExcludingIdTests {

        @Test
        @DisplayName("성공 - ID 제외 SellerName 중복 확인")
        void shouldCheckSellerNameExcludingId() {
            // Given
            SellerName sellerName = SellerName.of("test-seller");
            SellerId excludeSellerId = SellerId.of(1L);
            given(queryDslRepository.existsBySellerNameExcludingId("test-seller", 1L))
                    .willReturn(true);

            // When
            boolean result =
                    sellerQueryAdapter.existsBySellerNameExcludingId(sellerName, excludeSellerId);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findByCriteria 테스트")
    class FindByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 Seller 목록 조회")
        void shouldFindSellersByCriteria() {
            // Given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, SellerStatus.ACTIVE, null, null, 0, 10);
            LocalDateTime now = LocalDateTime.now();
            SellerJpaEntity entity =
                    SellerJpaEntity.of(1L, "mustit", "commerce", SellerStatus.ACTIVE, 0, now, now);
            Seller domain = SellerFixture.anActiveSeller();

            given(queryDslRepository.findByCriteria(criteria)).willReturn(List.of(entity));
            given(sellerJpaEntityMapper.toDomain(entity)).willReturn(domain);

            // When
            List<Seller> result = sellerQueryAdapter.findByCriteria(criteria);

            // Then
            assertThat(result).hasSize(1);
            verify(queryDslRepository).findByCriteria(criteria);
        }
    }

    @Nested
    @DisplayName("countByCriteria 테스트")
    class CountByCriteriaTests {

        @Test
        @DisplayName("성공 - 조건으로 Seller 개수 조회")
        void shouldCountSellersByCriteria() {
            // Given
            SellerQueryCriteria criteria =
                    new SellerQueryCriteria(null, null, SellerStatus.ACTIVE, null, null, 0, 10);
            given(queryDslRepository.countByCriteria(criteria)).willReturn(5L);

            // When
            long result = sellerQueryAdapter.countByCriteria(criteria);

            // Then
            assertThat(result).isEqualTo(5L);
        }
    }
}
