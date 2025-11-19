package com.ryuqq.crawlinghub.application.scheduler.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerResponseFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchedulerResponseTest {

    @Test
    @DisplayName("should define scheduler response as record type")
    void shouldDefineSchedulerResponseAsRecord() {
        assertThat(SchedulerResponse.class.isRecord()).isTrue();
    }

    @Test
    @DisplayName("should create response from domain scheduler aggregate")
    void shouldCreateResponseFromDomain() {
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerResponse response = SchedulerResponse.from(scheduler, "arn:aws:events:rule/fixture");

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
        assertThat(response.eventBridgeRuleName()).isEqualTo("arn:aws:events:rule/fixture");
        assertThat(response.createdAt()).isEqualTo(scheduler.getCreatedAt());
    }

    @Test
    @DisplayName("should support optional eventBridgeRuleName field")
    void shouldSupportOptionalEventBridgeRuleName() {
        SchedulerResponse response = SchedulerResponseFixture.withoutEventBridgeRule();

        assertThat(response.eventBridgeRuleName()).isNull();
    }
}

