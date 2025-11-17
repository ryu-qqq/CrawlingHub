package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.product.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductId VO 테스트
 *
 * TDD Phase: Red → Green
 * - Long 기반 auto-increment ID
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ProductIdTest {

    @Test
    void shouldCreateNewIdWithNullValue() {
        // When
        ProductId id = ProductId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNull();
    }

    @Test
    void shouldReturnTrueForIsNewWhenValueIsNull() {
        // Given
        ProductId id = ProductId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForIsNewWhenValueIsNotNull() {
        // Given
        ProductId id = new ProductId(1L);

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldCreateIdWithSpecificValue() {
        // When
        ProductId id = new ProductId(123L);

        // Then
        assertThat(id.value()).isEqualTo(123L);
        assertThat(id.isNew()).isFalse();
    }
}
