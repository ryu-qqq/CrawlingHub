package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.product.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductId VO 테스트
 *
 * TDD Phase: Red → Green
 * - UUID 고유성 검증
 * - forNew() 정적 팩토리 메서드 검증
 * - isNew() 메서드 검증
 */
class ProductIdTest {

    @Test
    void shouldGenerateUniqueProductId() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateIdUsingForNew() {
        // When
        ProductId id = ProductId.forNew();

        // Then
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void shouldReturnTrueForIsNew() {
        // Given
        ProductId id = ProductId.forNew();

        // When
        boolean result = id.isNew();

        // Then
        assertThat(result).isTrue();
    }
}
