package com.ryuqq.crawlinghub.domain.vo;

import com.ryuqq.crawlinghub.domain.crawler.vo.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlerTaskStatus Enum 테스트
 *
 * TDD Phase: Red → Green
 * - 6가지 상태 존재 검증
 * - of() 정적 팩토리 메서드 검증 (String → Enum 변환)
 */
class CrawlerTaskStatusTest {

    @Test
    void shouldHaveAllRequiredStatuses() {
        assertThat(CrawlerTaskStatus.values()).hasSize(6);
    }

    @Test
    void shouldCreateFromValidString() {
        // Given
        String value = "WAITING";

        // When
        CrawlerTaskStatus result = CrawlerTaskStatus.of(value);

        // Then
        assertThat(result).isEqualTo(CrawlerTaskStatus.WAITING);
    }

    @Test
    void shouldCreateFromLowercaseString() {
        // Given
        String value = "in_progress";

        // When
        CrawlerTaskStatus result = CrawlerTaskStatus.of(value);

        // Then
        assertThat(result).isEqualTo(CrawlerTaskStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        // When & Then
        assertThatThrownBy(() -> CrawlerTaskStatus.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("CrawlerTaskStatus cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenValueIsInvalid() {
        // Given
        String invalidValue = "INVALID_STATUS";

        // When & Then
        assertThatThrownBy(() -> CrawlerTaskStatus.of(invalidValue))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
