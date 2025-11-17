package com.ryuqq.crawlinghub.domain.crawler.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlingScheduleInvalidStateException 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
@DisplayName("CrawlingScheduleInvalidStateException 테스트")
class CrawlingScheduleInvalidStateExceptionTest {

    @Test
    @DisplayName("UUID 기반 생성자로 예외 생성")
    void shouldCreateExceptionWithUuidConstructor() {
        // given
        UUID scheduleId = UUID.randomUUID();
        String currentStatus = "INACTIVE";
        String action = "updateInterval";
        String reason = "Schedule must be in ACTIVE status to update interval";

        // when
        CrawlingScheduleInvalidStateException exception = new CrawlingScheduleInvalidStateException(
            scheduleId, currentStatus, action, reason
        );

        // then
        assertThat(exception.getMessage()).contains("Cannot " + action + " schedule");
        assertThat(exception.getMessage()).contains(scheduleId.toString());
        assertThat(exception.getMessage()).contains(currentStatus);
        assertThat(exception.getMessage()).contains(reason);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }

    @Test
    @DisplayName("메시지로 예외 생성")
    void shouldCreateExceptionWithMessage() {
        // given
        String message = "Cannot activate schedule. Schedule is already ACTIVE.";

        // when
        CrawlingScheduleInvalidStateException exception = new CrawlingScheduleInvalidStateException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.code()).isEqualTo("CRAWLER-004");
        assertThat(exception.args()).isEmpty();
    }
}
