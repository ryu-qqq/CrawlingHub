package com.ryuqq.crawlinghub.application.scheduler.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.query.GetSchedulerQueryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetSchedulerQueryTest {

    @Test
    @DisplayName("should create query record with scheduler id")
    void shouldCreateRecordWithSchedulerId() {
        GetSchedulerQuery query = GetSchedulerQueryFixture.create();

        assertThat(query.schedulerId()).isEqualTo(1L);
    }
}

