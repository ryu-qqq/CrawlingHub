package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Seller Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Seller 생성 테스트
 * - Seller 활성화/비활성화 (activate/deactivate) 테스트
 * - Seller 상품 수 업데이트 (updateTotalProductCount) 테스트
 * - 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 * - Clock 의존성 테스트 (테스트 가능성)
 */
class SellerTest {

    // ========== Clock 고정 (테스트 재현성) ==========

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2024-01-01T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    // ========== Clock 의존성 테스트 ==========

    @Test
    void shouldCreateSellerWithFixedClock() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "시계 테스트 셀러";
        LocalDateTime expectedTime = LocalDateTime.now(FIXED_CLOCK);

        // When
        Seller seller = Seller.forNew(sellerId, name, FIXED_CLOCK);

        // Then
        assertThat(seller.getCreatedAt()).isEqualTo(expectedTime);
        assertThat(seller.getUpdatedAt()).isEqualTo(expectedTime);
    }

    @Test
    void shouldPreserveCreatedAtWhenStateChanges() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "불변 테스트 셀러";

        Seller seller = Seller.forNew(sellerId, name, FIXED_CLOCK);
        LocalDateTime createdTime = seller.getCreatedAt();

        // When - 상태 변경
        seller.activate();

        // Then - createdAt은 불변, updatedAt은 갱신됨
        assertThat(seller.getCreatedAt()).isEqualTo(createdTime);
        assertThat(seller.getUpdatedAt()).isNotNull();
    }

    // ========== 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateSellerUsingForNew() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "새 셀러";

        // When
        Seller seller = Seller.forNew(sellerId, name);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.getTotalProductCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateSellerWithInactiveStatus() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "테스트 셀러";

        // When
        Seller seller = Seller.forNew(sellerId, name);

        // Then - 초기 상태는 INACTIVE여야 함
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getTotalProductCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateSellerUsingOf() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "테스트 셀러";

        // When
        Seller seller = Seller.of(sellerId, name);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void shouldReconstituteSellerWithAllFields() {
        // Given
        SellerId sellerId = new SellerId(1L);
        String name = "재구성 셀러";
        SellerStatus status = SellerStatus.INACTIVE;
        Integer totalProductCount = 100;

        // When
        Seller seller = Seller.reconstitute(sellerId, name, status, totalProductCount);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.getTotalProductCount()).isEqualTo(100);
    }

    @Test
    void shouldActivateSeller() {
        // Given - INACTIVE 상태의 Seller 생성
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");
        seller.deactivate(); // 먼저 비활성화

        // When
        seller.activate();

        // Then
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldDeactivateSeller() {
        // Given - ACTIVE 상태의 Seller
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");

        // When
        seller.deactivate();

        // Then
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void shouldUpdateTotalProductCount() {
        // Given
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");

        // When
        seller.updateTotalProductCount(100);

        // Then
        assertThat(seller.getTotalProductCount()).isEqualTo(100);
    }

    // ========== updateName() 테스트 ==========

    @Test
    void shouldUpdateSellerName() {
        // Given
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "원래 이름");
        String newName = "변경된 이름";

        // When
        seller.updateName(newName);

        // Then
        assertThat(seller.getName()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        // Given
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");

        // When & Then
        assertThatThrownBy(() -> seller.updateName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 null일 수 없습니다");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        // Given
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");

        // When & Then
        assertThatThrownBy(() -> seller.updateName(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 빈 값일 수 없습니다");

        assertThatThrownBy(() -> seller.updateName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 빈 값일 수 없습니다");
    }

    @Test
    void shouldThrowExceptionWhenNameExceedsMaxLength() {
        // Given
        SellerId sellerId = new SellerId(1L);
        Seller seller = Seller.forNew(sellerId, "테스트 셀러");
        String longName = "a".repeat(101); // 101자

        // When & Then
        assertThatThrownBy(() -> seller.updateName(longName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름은 100자를 초과할 수 없습니다");
    }
}
