package com.ryuqq.crawlinghub.application.scheduler.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerDetailResponseFixture;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerDetailResponseTest {

    @Test
    @DisplayName("should define detail response as record type")
    void shouldDefineRecordType() {
        assertThat(SchedulerDetailResponse.class.isRecord()).isTrue();
    }

    @Test
    @DisplayName("should create detail response from domain scheduler")
    void shouldCreateFromDomainScheduler() {
        var scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerDetailResponse response = SchedulerDetailResponse.from(scheduler, "arn:aws:events:rule/detail");

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
        assertThat(response.eventBridgeRuleName()).isEqualTo("arn:aws:events:rule/detail");
        assertThat(response.createdAt()).isEqualTo(scheduler.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(scheduler.getUpdatedAt());
    }

    @Test
    @DisplayName("should use fixture for detail response")
    void shouldUseFixture() {
        SchedulerDetailResponse response = SchedulerDetailResponseFixture.create();

        assertThat(response.schedulerId()).isNotNull();
        assertThat(response.schedulerName()).isNotBlank();
    }
}

