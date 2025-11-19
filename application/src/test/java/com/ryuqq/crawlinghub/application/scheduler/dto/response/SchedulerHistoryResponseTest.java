package com.ryuqq.crawlinghub.application.scheduler.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerHistoryResponseFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerHistoryResponseTest {

    @Test
    @DisplayName("should define history response as record type")
    void shouldDefineRecordType() {
        assertThat(SchedulerHistoryResponse.class.isRecord()).isTrue();
    }

    @Test
    @DisplayName("should create history response with factory")
    void shouldCreateHistoryResponse() {
        SchedulerHistoryResponse expected = SchedulerHistoryResponseFixture.create();

        SchedulerHistoryResponse response = SchedulerHistoryResponse.of(
            expected.historyId(),
            expected.schedulerId(),
            expected.attributeName(),
            expected.previousValue(),
            expected.currentValue(),
            expected.occurredAt()
        );

        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("should use fixture for history response")
    void shouldUseFixture() {
        SchedulerHistoryResponse response = SchedulerHistoryResponseFixture.create();

        assertThat(response.historyId()).isNotNull();
        assertThat(response.occurredAt()).isNotNull();
    }
}

