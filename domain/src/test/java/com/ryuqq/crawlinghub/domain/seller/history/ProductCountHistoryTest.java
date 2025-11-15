package com.ryuqq.crawlinghub.domain.seller.history;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ryuqq.crawlinghub.domain.seller.MustItSellerId;

/**
 * ProductCountHistoryTest - ProductCountHistory 도메인 객체 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Tag("unit")
@Tag("domain")
@DisplayName("ProductCountHistory 도메인 객체 테스트")
class ProductCountHistoryTest {

    @Test
    @DisplayName("정상 케이스: 상품 수 이력 생성 성공")
    void record_success() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        Integer productCount = 100;
        LocalDateTime executedDate = LocalDateTime.now();

        // When
        ProductCountHistory history = ProductCountHistory.record(sellerId, productCount, executedDate);

        // Then
        assertThat(history).isNotNull();
        assertThat(history.getSellerIdValue()).isEqualTo(1L);
        assertThat(history.getProductCount()).isEqualTo(100);
        assertThat(history.getExecutedDate()).isEqualTo(executedDate);
    }

    @Test
    @DisplayName("예외 케이스: 상품 수 null 시 예외 발생")
    void record_fail_whenProductCountIsNull() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        Integer productCount = null;
        LocalDateTime executedDate = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> ProductCountHistory.record(sellerId, productCount, executedDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품 수는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("예외 케이스: 상품 수 음수 시 예외 발생")
    void record_fail_whenProductCountIsNegative() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        Integer productCount = -1;
        LocalDateTime executedDate = LocalDateTime.now();

        // When & Then
        assertThatThrownBy(() -> ProductCountHistory.record(sellerId, productCount, executedDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("상품 수는 0 이상이어야 합니다");
    }

    @Test
    @DisplayName("예외 케이스: 실행 날짜 null 시 예외 발생")
    void record_fail_whenExecutedDateIsNull() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        Integer productCount = 100;
        LocalDateTime executedDate = null;

        // When & Then
        assertThatThrownBy(() -> ProductCountHistory.record(sellerId, productCount, executedDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("실행 날짜는 필수입니다");
    }

    @Test
    @DisplayName("정상 케이스: 동일 날짜 체크 성공")
    void isSameDate_success() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        LocalDateTime date1 = LocalDateTime.of(2025, 11, 5, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 11, 5, 15, 0);
        ProductCountHistory history = ProductCountHistory.record(sellerId, 100, date1);

        // When
        boolean isSameDate = history.isSameDate(date2);

        // Then
        assertThat(isSameDate).isTrue();
    }

    @Test
    @DisplayName("정상 케이스: 다른 날짜 체크 성공")
    void isSameDate_fail_whenDifferentDate() {
        // Given
        MustItSellerId sellerId = MustItSellerId.of(1L);
        LocalDateTime date1 = LocalDateTime.of(2025, 11, 5, 10, 0);
        LocalDateTime date2 = LocalDateTime.of(2025, 11, 6, 10, 0);
        ProductCountHistory history = ProductCountHistory.record(sellerId, 100, date1);

        // When
        boolean isSameDate = history.isSameDate(date2);

        // Then
        assertThat(isSameDate).isFalse();
    }

    @Test
    @DisplayName("정상 케이스: reconstitute 성공")
    void reconstitute_success() {
        // Given
        ProductCountHistoryId id = ProductCountHistoryId.of(1L);
        MustItSellerId sellerId = MustItSellerId.of(1L);
        Integer productCount = 100;
        LocalDateTime executedDate = LocalDateTime.now();

        // When
        ProductCountHistory history = ProductCountHistory.reconstitute(id, sellerId, productCount, executedDate);

        // Then
        assertThat(history).isNotNull();
        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getSellerIdValue()).isEqualTo(1L);
        assertThat(history.getProductCount()).isEqualTo(100);
    }
}

