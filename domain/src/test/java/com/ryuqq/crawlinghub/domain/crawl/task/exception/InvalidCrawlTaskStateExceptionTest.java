package com.ryuqq.crawlinghub.domain.crawl.task.exception;

import com.ryuqq.crawlinghub.domain.crawl.task.vo.CrawlTaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InvalidCrawlTaskStateException 단위 테스트
 *
 * <p>Kent Beck TDD - Red Phase
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("InvalidCrawlTaskStateException 테스트")
class InvalidCrawlTaskStateExceptionTest {

    @Test
    @DisplayName("현재 상태와 기대 상태로 예외 생성")
    void shouldCreateWithCurrentAndExpectedStatus() {
        // given
        CrawlTaskStatus current = CrawlTaskStatus.WAITING;
        CrawlTaskStatus expected = CrawlTaskStatus.PUBLISHED;

        // when
        InvalidCrawlTaskStateException exception = new InvalidCrawlTaskStateException(current, expected);

        // then
        assertThat(exception.code()).isEqualTo("CRAWL-TASK-002");
        assertThat(exception.getMessage()).contains("상태 전환");
        assertThat(exception.args()).containsEntry("currentStatus", current.name());
        assertThat(exception.args()).containsEntry("expectedStatus", expected.name());
    }
}
