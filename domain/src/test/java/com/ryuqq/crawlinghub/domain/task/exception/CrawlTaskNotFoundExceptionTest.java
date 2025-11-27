package com.ryuqq.crawlinghub.domain.task.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskNotFoundException 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTaskNotFoundException 테스트")
class CrawlTaskNotFoundExceptionTest {

    @Test
    @DisplayName("CrawlTask ID로 예외 생성")
    void shouldCreateWithCrawlTaskId() {
        // given
        Long crawlTaskId = 12345L;

        // when
        CrawlTaskNotFoundException exception = new CrawlTaskNotFoundException(crawlTaskId);

        // then
        assertThat(exception.code()).isEqualTo("CRAWL-TASK-001");
        assertThat(exception.getMessage()).contains("존재하지 않는");
        assertThat(exception.args()).containsEntry("crawlTaskId", crawlTaskId);
    }
}
