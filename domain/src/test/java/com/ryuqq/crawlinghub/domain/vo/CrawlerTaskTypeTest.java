package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlerTaskType Enum 테스트
 *
 * TDD Phase: Red
 * - 3가지 타입 존재 검증 (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
 */
class CrawlerTaskTypeTest {

    @Test
    void shouldHaveThreeTaskTypes() {
        assertThat(CrawlerTaskType.values()).containsExactly(
            CrawlerTaskType.MINISHOP,
            CrawlerTaskType.PRODUCT_DETAIL,
            CrawlerTaskType.PRODUCT_OPTION
        );
    }
}
