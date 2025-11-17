package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.seller.aggregate.seller.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.*;
import com.ryuqq.crawlinghub.domain.seller.vo.CrawlingInterval;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Seller Aggregate Root 테스트
 *
 * TDD Phase: Red → Green
 * - Seller 생성 (register) 테스트
 * - Seller 주기 변경 (updateInterval) 테스트
 * - Seller 활성화/비활성화 (activate/deactivate) 테스트
 * - Seller 상품 수 업데이트 (updateTotalProductCount) 테스트
 * - 리팩토링: 정적 팩토리 메서드 패턴 (forNew/of/reconstitute) 테스트
 * - 리팩토링: Clock 의존성 테스트 (테스트 가능성)
 */
class SellerTest {

    // ========== Clock 고정 (테스트 재현성) ==========

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2024-01-01T00:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    // ========== 리팩토링: Clock 의존성 테스트 ==========

    @Test
    void shouldCreateSellerWithFixedClock() {
        // Given
        SellerId sellerId = new SellerId("seller_clock_001");
        String name = "시계 테스트 셀러";
        CrawlingInterval crawlingInterval = new CrawlingInterval(1);
        LocalDateTime expectedTime = LocalDateTime.now(FIXED_CLOCK);

        // When
        Seller seller = Seller.forNew(sellerId, name, crawlingInterval, FIXED_CLOCK);

        // Then
        assertThat(seller.getCreatedAt()).isEqualTo(expectedTime);
        assertThat(seller.getUpdatedAt()).isEqualTo(expectedTime);
    }

    @Test
    void shouldPreserveCreatedAtWhenStateChanges() {
        // Given
        SellerId sellerId = new SellerId("seller_clock_002");
        String name = "불변 테스트 셀러";
        CrawlingInterval crawlingInterval = new CrawlingInterval(1);

        Seller seller = Seller.forNew(sellerId, name, crawlingInterval, FIXED_CLOCK);
        LocalDateTime createdTime = seller.getCreatedAt();

        // When - 상태 변경
        seller.updateInterval(7);

        // Then - createdAt은 불변, updatedAt은 갱신됨
        assertThat(seller.getCreatedAt()).isEqualTo(createdTime);
        assertThat(seller.getUpdatedAt()).isNotNull();
    }

    // ========== 리팩토링: 정적 팩토리 메서드 패턴 테스트 ==========

    @Test
    void shouldCreateSellerUsingForNew() {
        // Given
        SellerId sellerId = new SellerId("seller_new_001");
        String name = "새 셀러";
        CrawlingInterval crawlingInterval = new CrawlingInterval(7);

        // When
        Seller seller = Seller.forNew(sellerId, name, crawlingInterval);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(7);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(seller.getTotalProductCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateSellerUsingOf() {
        // Given
        SellerId sellerId = new SellerId("seller_of_001");
        String name = "테스트 셀러";
        CrawlingInterval crawlingInterval = new CrawlingInterval(3);

        // When
        Seller seller = Seller.of(sellerId, name, crawlingInterval);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(3);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldReconstituteSellerWithAllFields() {
        // Given
        SellerId sellerId = new SellerId("seller_recon_001");
        String name = "재구성 셀러";
        CrawlingInterval crawlingInterval = new CrawlingInterval(5);
        SellerStatus status = SellerStatus.INACTIVE;
        Integer totalProductCount = 100;

        // When
        Seller seller = Seller.reconstitute(sellerId, name, crawlingInterval, status, totalProductCount);

        // Then
        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(5);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
        assertThat(seller.getTotalProductCount()).isEqualTo(100);
    }

    // ========== 기존 테스트 (레거시, 유지보수용) ==========


    @Test
    void shouldRegisterSellerWithValidData() {
        SellerId sellerId = new SellerId("seller_123");
        String name = "테스트 셀러";
        Integer intervalDays = 1;

        Seller seller = Seller.register(sellerId, name, intervalDays);

        assertThat(seller.getSellerId()).isEqualTo(sellerId);
        assertThat(seller.getName()).isEqualTo(name);
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(1);
    }

    @Test
    void shouldUpdateCrawlingInterval() {
        // Given
        SellerId sellerId = new SellerId("seller_001");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);
        Integer newIntervalDays = 7;

        // When
        seller.updateInterval(newIntervalDays);

        // Then
        assertThat(seller.getCrawlingIntervalDays()).isEqualTo(7);
    }

    @Test
    void shouldThrowExceptionWhenUpdateIntervalWithInvalidDays() {
        // Given
        SellerId sellerId = new SellerId("seller_002");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);

        // When & Then
        assertThatThrownBy(() -> seller.updateInterval(31))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("크롤링 주기는 1-30일 사이여야 합니다");
    }

    @Test
    void shouldActivateSeller() {
        // Given - INACTIVE 상태의 Seller 생성
        SellerId sellerId = new SellerId("seller_003");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);
        seller.deactivate(); // 먼저 비활성화

        // When
        seller.activate();

        // Then
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldDeactivateSeller() {
        // Given - ACTIVE 상태의 Seller (register 시 기본값)
        SellerId sellerId = new SellerId("seller_004");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);

        // When
        seller.deactivate();

        // Then
        assertThat(seller.getStatus()).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void shouldUpdateTotalProductCount() {
        // Given
        SellerId sellerId = new SellerId("seller_005");
        Seller seller = Seller.register(sellerId, "테스트 셀러", 1);

        // When
        seller.updateTotalProductCount(100);

        // Then
        assertThat(seller.getTotalProductCount()).isEqualTo(100);
    }
}
