package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InvalidRequestUrlException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("InvalidRequestUrlException 테스트")
class InvalidRequestUrlExceptionTest {

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Invalid request URL format";

        // when
        InvalidRequestUrlException exception = new InvalidRequestUrlException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("CRAWLER-006");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지와 원인으로 예외 생성")
    void shouldCreateExceptionWithMessageAndCause() {
        // given
        String message = "Invalid request URL format";
        Throwable cause = new IllegalArgumentException("Invalid URL");

        // when
        InvalidRequestUrlException exception = new InvalidRequestUrlException(message, cause);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.code()).isEqualTo("CRAWLER-006");
        assertThat(exception.args()).isEmpty();
    }
}
