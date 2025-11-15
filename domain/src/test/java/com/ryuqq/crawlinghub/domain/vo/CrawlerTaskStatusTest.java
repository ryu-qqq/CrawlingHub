package com.ryuqq.crawlinghub.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlerTaskStatus Enum 테스트
 *
 * TDD Phase: Red
 * - 6가지 상태 존재 검증
 */
class CrawlerTaskStatusTest {

    @Test
    void shouldHaveAllRequiredStatuses() {
        assertThat(CrawlerTaskStatus.values()).hasSize(6);
    }
}
