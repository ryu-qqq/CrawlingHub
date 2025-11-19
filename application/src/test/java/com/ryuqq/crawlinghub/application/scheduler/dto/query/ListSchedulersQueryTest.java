package com.ryuqq.crawlinghub.application.scheduler.dto.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.query.ListSchedulersQueryFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ListSchedulersQueryTest {

    @Test
    @DisplayName("should create list query with filters and pagination")
    void shouldCreateListQuery() {
        ListSchedulersQuery query = ListSchedulersQueryFixture.create();

        assertThat(query.sellerId()).isEqualTo(99L);
        assertThat(query.status()).isEqualTo(SchedulerStatus.ACTIVE);
        assertThat(query.page()).isEqualTo(0);
        assertThat(query.size()).isEqualTo(20);
    }
}

