package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlerTaskInvalidStateException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("CrawlerTaskInvalidStateException 테스트")
class CrawlerTaskInvalidStateExceptionTest {

    @Test
    @DisplayName("UUID 기반 생성자로 예외 생성")
    void shouldCreateExceptionWithUuidConstructor() {
        // given
        Long taskId = 1L;
        String currentStatus = "PUBLISHED";
        String action = "publish";
        String reason = "Task must be in WAITING status to publish";

        // when
        CrawlerTaskInvalidStateException exception = new CrawlerTaskInvalidStateException(
            taskId, currentStatus, action, reason
        );

        // then
        assertThat(exception.getMessage()).contains("Cannot " + action + " task");
        assertThat(exception.getMessage()).contains(taskId.toString());
        assertThat(exception.getMessage()).contains(currentStatus);
        assertThat(exception.getMessage()).contains(reason);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Cannot retry task. Maximum retry count exceeded.";

        // when
        CrawlerTaskInvalidStateException exception = new CrawlerTaskInvalidStateException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }
}
