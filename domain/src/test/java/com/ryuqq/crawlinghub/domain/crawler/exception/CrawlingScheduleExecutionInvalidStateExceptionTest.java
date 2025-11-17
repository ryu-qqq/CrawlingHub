package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlingScheduleExecutionInvalidStateException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("CrawlingScheduleExecutionInvalidStateException 테스트")
class CrawlingScheduleExecutionInvalidStateExceptionTest {

    @Test
    @DisplayName("UUID 기반 생성자로 예외 생성")
    void shouldCreateExceptionWithUuidConstructor() {
        // given
        UUID executionId = UUID.randomUUID();
        String currentStatus = "PENDING";
        String action = "complete";
        String reason = "Execution must be in RUNNING status to complete";

        // when
        CrawlingScheduleExecutionInvalidStateException exception = new CrawlingScheduleExecutionInvalidStateException(
            executionId, currentStatus, action, reason
        );

        // then
        assertThat(exception.getMessage()).contains("Cannot " + action + " execution");
        assertThat(exception.getMessage()).contains(executionId.toString());
        assertThat(exception.getMessage()).contains(currentStatus);
        assertThat(exception.getMessage()).contains(reason);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Cannot start execution. Execution must be in PENDING status.";

        // when
        CrawlingScheduleExecutionInvalidStateException exception = new CrawlingScheduleExecutionInvalidStateException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }
}
