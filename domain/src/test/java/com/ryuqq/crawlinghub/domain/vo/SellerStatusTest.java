package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SellerStatus Enum 테스트
 *
 * TDD Phase: Red
 * - ACTIVE, INACTIVE 상태 존재 검증
 */
class SellerStatusTest {

    @Test
    void shouldHaveActiveAndInactiveStatus() {
        assertThat(SellerStatus.values()).containsExactly(
            SellerStatus.ACTIVE,
            SellerStatus.INACTIVE
        );
    }
}
