package com.ryuqq.crawlinghub.application.scheduler.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerSummaryResponseFixture;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerSummaryResponseTest {

    @Test
    @DisplayName("should define summary response as record type")
    void shouldDefineRecordType() {
        assertThat(SchedulerSummaryResponse.class.isRecord()).isTrue();
    }

    @Test
    @DisplayName("should create summary response from domain scheduler")
    void shouldCreateFromDomainScheduler() {
        var scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerSummaryResponse response = SchedulerSummaryResponse.from(scheduler);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
    }

    @Test
    @DisplayName("should use fixture for summary response")
    void shouldUseFixture() {
        SchedulerSummaryResponse response = SchedulerSummaryResponseFixture.create();

        assertThat(response.schedulerId()).isNotNull();
        assertThat(response.schedulerName()).isNotBlank();
    }
}

