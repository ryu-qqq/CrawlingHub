package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlerTaskType Enum 테스트
 *
 * TDD Phase: Red → Green
 * - 3가지 타입 존재 검증 (MINISHOP, PRODUCT_DETAIL, PRODUCT_OPTION)
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
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

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "MINISHOP";

        // When
        CrawlerTaskType result = CrawlerTaskType.of(value);

        // Then
        assertThat(result).isEqualTo(CrawlerTaskType.MINISHOP);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "product_detail";

        // When
        CrawlerTaskType result = CrawlerTaskType.of(value);

        // Then
        assertThat(result).isEqualTo(CrawlerTaskType.PRODUCT_DETAIL);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> CrawlerTaskType.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CrawlerTaskType cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_TYPE";

        // When & Then
        assertThatThrownBy(() -> CrawlerTaskType.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
