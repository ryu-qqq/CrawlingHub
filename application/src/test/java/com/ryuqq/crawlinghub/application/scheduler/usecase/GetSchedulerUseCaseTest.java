package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.query.GetSchedulerQueryFixture;
import com.ryuqq.crawlinghub.application.scheduler.service.query.GetSchedulerService;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("GetSchedulerUseCase")
class GetSchedulerUseCaseTest {

    private final SchedulerQueryPort schedulerQueryPort = Mockito.mock(SchedulerQueryPort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    private GetSchedulerService useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetSchedulerService(
            schedulerQueryPort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should get scheduler by id")
    void shouldGetSchedulerById() {
        GetSchedulerQuery query = GetSchedulerQueryFixture.create();
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        given(schedulerQueryPort.findById(SchedulerId.of(query.schedulerId()))).willReturn(Optional.of(scheduler));

        SchedulerDetailResponse response = useCase.execute(query);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        assertThat(response.schedulerName()).isEqualTo(scheduler.getSchedulerName());
        assertThat(response.cronExpression()).isEqualTo(scheduler.getCronExpression().value());
    }

    @Test
    @DisplayName("should throw exception when scheduler not found")
    void shouldThrowExceptionWhenNotFound() {
        GetSchedulerQuery query = GetSchedulerQueryFixture.create();

        given(schedulerQueryPort.findById(SchedulerId.of(query.schedulerId()))).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(query))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Scheduler not found");
    }
}

