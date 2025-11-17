package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.product.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OutboxEventType Enum 테스트
 *
 * TDD Phase: Red → Green
 * - 2가지 이벤트 타입 존재 검증 (PRODUCT_CREATED, PRODUCT_UPDATED)
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
 */
class OutboxEventTypeTest {

    @Test
    void shouldHaveTwoEventTypes() {
        assertThat(OutboxEventType.values()).containsExactly(
            OutboxEventType.PRODUCT_CREATED,
            OutboxEventType.PRODUCT_UPDATED
        );
    }

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "PRODUCT_CREATED";

        // When
        OutboxEventType result = OutboxEventType.of(value);

        // Then
        assertThat(result).isEqualTo(OutboxEventType.PRODUCT_CREATED);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "product_updated";

        // When
        OutboxEventType result = OutboxEventType.of(value);

        // Then
        assertThat(result).isEqualTo(OutboxEventType.PRODUCT_UPDATED);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> OutboxEventType.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OutboxEventType cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_EVENT";

        // When & Then
        assertThatThrownBy(() -> OutboxEventType.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
