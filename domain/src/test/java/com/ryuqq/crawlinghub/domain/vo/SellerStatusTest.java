package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.seller.vo.*;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SellerStatus Enum 테스트
 *
 * TDD Phase: Red → Green
 * - ACTIVE, INACTIVE 상태 존재 검증
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
 */
class SellerStatusTest {

    @Test
    void shouldHaveActiveAndInactiveStatus() {
        assertThat(SellerStatus.values()).containsExactly(
            SellerStatus.ACTIVE,
            SellerStatus.INACTIVE
        );
    }

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "ACTIVE";

        // When
        SellerStatus result = SellerStatus.of(value);

        // Then
        assertThat(result).isEqualTo(SellerStatus.ACTIVE);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "inactive";

        // When
        SellerStatus result = SellerStatus.of(value);

        // Then
        assertThat(result).isEqualTo(SellerStatus.INACTIVE);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> SellerStatus.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("SellerStatus cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_STATUS";

        // When & Then
        assertThatThrownBy(() -> SellerStatus.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
