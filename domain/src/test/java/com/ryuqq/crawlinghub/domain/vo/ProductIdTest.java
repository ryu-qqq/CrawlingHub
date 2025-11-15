package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductId VO 테스트
 *
 * TDD Phase: Red
 * - UUID 고유성 검증
 */
class ProductIdTest {

    @Test
    void shouldGenerateUniqueProductId() {
        ProductId id1 = ProductId.generate();
        ProductId id2 = ProductId.generate();
        assertThat(id1).isNotEqualTo(id2);
    }
}
