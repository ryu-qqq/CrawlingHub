package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper.SellerJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository.SellerJpaRepository;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SellerCommandAdapter 단위 테스트
 *
 * <p>Mock을 사용한 Adapter 동작 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@DisplayName("SellerCommandAdapter 단위 테스트")
@ExtendWith(MockitoExtension.class)
class SellerCommandAdapterTest {

    @Mock private SellerJpaRepository sellerJpaRepository;

    @Mock private SellerJpaEntityMapper sellerJpaEntityMapper;

    private SellerCommandAdapter sellerCommandAdapter;

    @BeforeEach
    void setUp() {
        sellerCommandAdapter = new SellerCommandAdapter(sellerJpaRepository, sellerJpaEntityMapper);
    }

    @Test
    @DisplayName("성공 - Seller 저장 시 ID 반환")
    void shouldReturnSellerIdWhenPersist() {
        // Given
        Seller seller = SellerFixture.anActiveSeller();
        LocalDateTime now = LocalDateTime.now();
        SellerJpaEntity entity =
                SellerJpaEntity.of(
                        null, "mustit-seller", "commerce-seller", SellerStatus.ACTIVE, 0, now, now);
        SellerJpaEntity savedEntity =
                SellerJpaEntity.of(
                        1L, "mustit-seller", "commerce-seller", SellerStatus.ACTIVE, 0, now, now);

        given(sellerJpaEntityMapper.toEntity(seller)).willReturn(entity);
        given(sellerJpaRepository.save(entity)).willReturn(savedEntity);

        // When
        SellerId result = sellerCommandAdapter.persist(seller);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.value()).isEqualTo(1L);
        verify(sellerJpaEntityMapper).toEntity(seller);
        verify(sellerJpaRepository).save(entity);
    }

    @Test
    @DisplayName("성공 - 기존 Seller 수정 시 동일 ID 반환")
    void shouldReturnSameIdWhenUpdateExistingSeller() {
        // Given
        Seller seller = SellerFixture.anActiveSeller();
        LocalDateTime now = LocalDateTime.now();
        SellerJpaEntity entity =
                SellerJpaEntity.of(
                        100L,
                        "mustit-seller",
                        "commerce-seller",
                        SellerStatus.ACTIVE,
                        50,
                        now,
                        now);

        given(sellerJpaEntityMapper.toEntity(seller)).willReturn(entity);
        given(sellerJpaRepository.save(entity)).willReturn(entity);

        // When
        SellerId result = sellerCommandAdapter.persist(seller);

        // Then
        assertThat(result.value()).isEqualTo(100L);
    }
}
