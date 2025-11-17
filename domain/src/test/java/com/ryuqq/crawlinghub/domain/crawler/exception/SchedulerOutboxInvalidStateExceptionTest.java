package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SchedulerOutboxInvalidStateException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("SchedulerOutboxInvalidStateException 테스트")
class SchedulerOutboxInvalidStateExceptionTest {

    @Test
    @DisplayName("UUID 기반 생성자로 예외 생성")
    void shouldCreateExceptionWithUuidConstructor() {
        // given
        UUID outboxId = UUID.randomUUID();
        String currentStatus = "SENDING";
        String action = "send";
        String reason = "Outbox must be in WAITING status to send";

        // when
        SchedulerOutboxInvalidStateException exception = new SchedulerOutboxInvalidStateException(
            outboxId, currentStatus, action, reason
        );

        // then
        assertThat(exception.getMessage()).contains("Cannot " + action + " outbox");
        assertThat(exception.getMessage()).contains(outboxId.toString());
        assertThat(exception.getMessage()).contains(currentStatus);
        assertThat(exception.getMessage()).contains(reason);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Cannot retry outbox. Maximum retry count exceeded.";

        // when
        SchedulerOutboxInvalidStateException exception = new SchedulerOutboxInvalidStateException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }
}
