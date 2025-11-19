package com.ryuqq.crawlinghub.application.scheduler.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.query.GetSchedulerHistoryQueryFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GetSchedulerHistoryQueryTest {

    @Test
    @DisplayName("should create history query with paging options")
    void shouldCreateHistoryQuery() {
        GetSchedulerHistoryQuery query = GetSchedulerHistoryQueryFixture.create();

        assertThat(query.schedulerId()).isEqualTo(77L);
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.size()).isEqualTo(50);
    }
}

