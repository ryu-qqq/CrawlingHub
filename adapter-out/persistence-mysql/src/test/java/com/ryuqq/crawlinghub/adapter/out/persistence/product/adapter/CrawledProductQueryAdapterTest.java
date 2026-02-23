package com.ryuqq.crawlinghub.adapter.out.persistence.product.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper.CrawledProductJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.repository.CrawledProductQueryDslRepository;
import com.ryuqq.crawlinghub.domain.common.vo.DeletionStatus;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlCompletionStatus;
import com.ryuqq.crawlinghub.domain.product.vo.ProductChangeType;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImages;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOptions;
import com.ryuqq.crawlinghub.domain.product.vo.ProductPrice;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawledProductQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("CrawledProductQueryAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class CrawledProductQueryAdapterTest {

    @Mock private CrawledProductQueryDslRepository queryDslRepository;

    @Mock private CrawledProductJpaEntityMapper mapper;

    private CrawledProductQueryAdapter queryAdapter;

    @BeforeEach
    void setUp() {
        queryAdapter = new CrawledProductQueryAdapter(queryDslRepository, mapper);
    }

    @Test
    @DisplayName("성공 - ID로 CrawledProduct 조회")
    void shouldFindById() {
        // Given
        CrawledProductId productId = CrawledProductId.of(1L);
        CrawledProductJpaEntity entity = createTestEntity(1L, 100L, 12345L);
        CrawledProduct domain = createTestDomain(1L, 100L, 12345L);

        given(queryDslRepository.findById(1L)).willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProduct> result = queryAdapter.findById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getIdValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공 - ID로 CrawledProduct 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        CrawledProductId productId = CrawledProductId.of(999L);
        given(queryDslRepository.findById(999L)).willReturn(Optional.empty());

        // When
        Optional<CrawledProduct> result = queryAdapter.findById(productId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - SellerId와 ItemNo로 CrawledProduct 조회")
    void shouldFindBySellerIdAndItemNo() {
        // Given
        SellerId sellerId = SellerId.of(100L);
        long itemNo = 12345L;
        CrawledProductJpaEntity entity = createTestEntity(1L, 100L, 12345L);
        CrawledProduct domain = createTestDomain(1L, 100L, 12345L);

        given(queryDslRepository.findBySellerIdAndItemNo(100L, 12345L))
                .willReturn(Optional.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        Optional<CrawledProduct> result = queryAdapter.findBySellerIdAndItemNo(sellerId, itemNo);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSellerIdValue()).isEqualTo(100L);
        assertThat(result.get().getItemNo()).isEqualTo(12345L);
    }

    @Test
    @DisplayName("성공 - SellerId와 ItemNo로 CrawledProduct 조회 (없는 경우)")
    void shouldReturnEmptyWhenNotFoundBySellerIdAndItemNo() {
        // Given
        SellerId sellerId = SellerId.of(100L);
        long itemNo = 99999L;
        given(queryDslRepository.findBySellerIdAndItemNo(100L, 99999L))
                .willReturn(Optional.empty());

        // When
        Optional<CrawledProduct> result = queryAdapter.findBySellerIdAndItemNo(sellerId, itemNo);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - SellerId로 CrawledProduct 목록 조회")
    void shouldFindBySellerId() {
        // Given
        SellerId sellerId = SellerId.of(100L);
        CrawledProductJpaEntity entity1 = createTestEntity(1L, 100L, 12345L);
        CrawledProductJpaEntity entity2 = createTestEntity(2L, 100L, 12346L);
        CrawledProduct domain1 = createTestDomain(1L, 100L, 12345L);
        CrawledProduct domain2 = createTestDomain(2L, 100L, 12346L);

        given(queryDslRepository.findBySellerId(100L)).willReturn(List.of(entity1, entity2));
        given(mapper.toDomain(entity1)).willReturn(domain1);
        given(mapper.toDomain(entity2)).willReturn(domain2);

        // When
        List<CrawledProduct> result = queryAdapter.findBySellerId(sellerId);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("성공 - SellerId로 CrawledProduct 목록 조회 (빈 목록)")
    void shouldReturnEmptyListWhenNoProductsForSeller() {
        // Given
        SellerId sellerId = SellerId.of(999L);
        given(queryDslRepository.findBySellerId(999L)).willReturn(List.of());

        // When
        List<CrawledProduct> result = queryAdapter.findBySellerId(sellerId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 동기화 필요 상품 조회")
    void shouldFindNeedsSyncProducts() {
        // Given
        int limit = 10;
        CrawledProductJpaEntity entity = createTestEntity(1L, 100L, 12345L);
        CrawledProduct domain = createTestDomain(1L, 100L, 12345L);

        given(queryDslRepository.findNeedsSyncProducts(limit)).willReturn(List.of(entity));
        given(mapper.toDomain(entity)).willReturn(domain);

        // When
        List<CrawledProduct> result = queryAdapter.findNeedsSyncProducts(limit);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("성공 - SellerId와 ItemNo로 존재 여부 확인 (존재함)")
    void shouldReturnTrueWhenExists() {
        // Given
        SellerId sellerId = SellerId.of(100L);
        long itemNo = 12345L;

        given(queryDslRepository.existsBySellerIdAndItemNo(100L, 12345L)).willReturn(true);

        // When
        boolean result = queryAdapter.existsBySellerIdAndItemNo(sellerId, itemNo);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공 - SellerId와 ItemNo로 존재 여부 확인 (존재하지 않음)")
    void shouldReturnFalseWhenNotExists() {
        // Given
        SellerId sellerId = SellerId.of(100L);
        long itemNo = 99999L;

        given(queryDslRepository.existsBySellerIdAndItemNo(100L, 99999L)).willReturn(false);

        // When
        boolean result = queryAdapter.existsBySellerIdAndItemNo(sellerId, itemNo);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("성공 - SellerId로 CrawledProduct 개수 조회")
    void shouldCountBySellerId() {
        // Given - 셀러별 상품 개수 조회
        SellerId sellerId = SellerId.of(100L);
        given(queryDslRepository.countBySellerId(100L)).willReturn(25L);

        // When
        long result = queryAdapter.countBySellerId(sellerId);

        // Then - 상품 개수가 반환되어야 함
        assertThat(result).isEqualTo(25L);
    }

    @Test
    @DisplayName("성공 - 상품 없는 셀러는 0 반환")
    void shouldReturnZeroWhenNoProducts() {
        // Given
        SellerId sellerId = SellerId.of(999L);
        given(queryDslRepository.countBySellerId(999L)).willReturn(0L);

        // When
        long result = queryAdapter.countBySellerId(sellerId);

        // Then
        assertThat(result).isZero();
    }

    private CrawledProductJpaEntity createTestEntity(long id, long sellerId, long itemNo) {
        LocalDateTime now = LocalDateTime.now();
        return CrawledProductJpaEntity.of(
                id, sellerId, itemNo, "테스트 상품", "테스트 브랜드", 100000L, 90000L, 10, null, false, null,
                null, null, null, "NORMAL", "대한민국", null, null, now, now, now, null, null, true,
                null, null, now, now);
    }

    private CrawledProduct createTestDomain(long id, long sellerId, long itemNo) {
        Instant now = Instant.now();
        ProductPrice price = ProductPrice.of(90000, 100000, 100000, 90000, 10, 10);
        CrawlCompletionStatus completionStatus = new CrawlCompletionStatus(now, now, now);

        return CrawledProduct.reconstitute(
                CrawledProductId.of(id),
                SellerId.of(sellerId),
                itemNo,
                "테스트 상품",
                "테스트 브랜드",
                price,
                ProductImages.empty(),
                false,
                null,
                null,
                null,
                null,
                "NORMAL",
                "대한민국",
                null,
                ProductOptions.empty(),
                completionStatus,
                null,
                null,
                true,
                EnumSet.noneOf(ProductChangeType.class),
                DeletionStatus.active(),
                now,
                now);
    }
}
