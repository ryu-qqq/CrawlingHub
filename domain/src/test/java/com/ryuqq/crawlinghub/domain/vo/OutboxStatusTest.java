package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.product.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * OutboxStatus Enum 테스트
 *
 * TDD Phase: Red → Green
 * - 4가지 상태 존재 검증 (WAITING, SENDING, COMPLETED, FAILED)
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
 */
class OutboxStatusTest {

    @Test
    void shouldHaveFourStatuses() {
        assertThat(OutboxStatus.values()).containsExactly(
            OutboxStatus.WAITING,
            OutboxStatus.SENDING,
            OutboxStatus.COMPLETED,
            OutboxStatus.FAILED
        );
    }

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "WAITING";

        // When
        OutboxStatus result = OutboxStatus.of(value);

        // Then
        assertThat(result).isEqualTo(OutboxStatus.WAITING);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "sending";

        // When
        OutboxStatus result = OutboxStatus.of(value);

        // Then
        assertThat(result).isEqualTo(OutboxStatus.SENDING);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> OutboxStatus.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("OutboxStatus cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_STATUS";

        // When & Then
        assertThatThrownBy(() -> OutboxStatus.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
