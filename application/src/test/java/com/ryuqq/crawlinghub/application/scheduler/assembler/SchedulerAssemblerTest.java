package com.ryuqq.crawlinghub.application.scheduler.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.RegisterSchedulerCommandFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SchedulerAssembler")
class SchedulerAssemblerTest {

    private final SchedulerAssembler assembler = SchedulerAssemblerFixture.create();

    @Test
    @DisplayName("도메인 Scheduler를 SchedulerResponse로 변환한다")
    void shouldAssembleDomainToResponse() {
        var scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerResponse response = assembler.toResponse(scheduler);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
        assertThat(response.eventBridgeRuleName()).isNull();
        assertThat(response.createdAt()).isEqualTo(scheduler.getCreatedAt());
    }

    @Test
    @DisplayName("도메인 Scheduler를 SchedulerDetailResponse로 변환한다")
    void shouldAssembleDomainToDetailResponse() {
        var scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerDetailResponse response = assembler.toDetailResponse(scheduler);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
        assertThat(response.eventBridgeRuleName()).isNull();
        assertThat(response.createdAt()).isEqualTo(scheduler.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(scheduler.getUpdatedAt());
    }

    @Test
    @DisplayName("도메인 Scheduler를 SchedulerSummaryResponse로 변환한다")
    void shouldAssembleDomainToSummaryResponse() {
        var scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        SchedulerSummaryResponse response = assembler.toSummaryResponse(scheduler);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.sellerId()).isEqualTo(scheduler.getSellerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
        assertThat(response.status()).isEqualTo(scheduler.getStatus());
    }

    @Test
    @DisplayName("등록 Command를 Domain Scheduler로 변환한다")
    void shouldAssembleCommandToDomain() {
        RegisterSchedulerCommand command = RegisterSchedulerCommandFixture.create();

        CrawlingScheduler scheduler = assembler.toScheduler(command);

        assertThat(scheduler.getSellerId()).isEqualTo(command.sellerId());
        assertThat(scheduler.getSchedulerName()).isEqualTo(command.schedulerName());
        assertThat(scheduler.getCronExpression().value()).isEqualTo(command.cronExpression());
        assertThat(scheduler.getStatus()).isEqualTo(SchedulerStatus.PENDING);
        assertThat(scheduler.getCreatedAt()).isEqualTo(
            LocalDateTime.ofInstant(SchedulerAssemblerFixture.fixedClock().instant(), SchedulerAssemblerFixture.fixedClock().getZone())
        );
    }
}

