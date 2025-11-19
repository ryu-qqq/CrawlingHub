package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.DeactivateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.DeactivateSchedulerCommandFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerResponseFixture;
import com.ryuqq.crawlinghub.application.scheduler.service.command.DeactivateSchedulerService;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("DeactivateSchedulerUseCase")
class DeactivateSchedulerUseCaseTest {

    private final SchedulerPersistencePort schedulerPersistencePort = Mockito.mock(SchedulerPersistencePort.class);
    private final SchedulerQueryPort schedulerQueryPort = Mockito.mock(SchedulerQueryPort.class);
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort = Mockito.mock(SchedulerHistoryPersistencePort.class);
    private final OutboxEventPersistencePort outboxEventPersistencePort = Mockito.mock(OutboxEventPersistencePort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    private DeactivateSchedulerService useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeactivateSchedulerService(
            schedulerPersistencePort,
            schedulerQueryPort,
            schedulerHistoryPersistencePort,
            outboxEventPersistencePort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should deactivate scheduler successfully")
    void shouldDeactivateSchedulerSuccessfully() {
        DeactivateSchedulerCommand command = DeactivateSchedulerCommandFixture.create();
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        given(schedulerQueryPort.findById(SchedulerId.of(command.schedulerId()))).willReturn(Optional.of(scheduler));
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class))).willReturn(scheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        verify(schedulerPersistencePort).saveScheduler(any(CrawlingScheduler.class));
        verify(schedulerHistoryPersistencePort).saveSchedulerHistory(any(SchedulerHistory.class));
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should throw when scheduler not found")
    void shouldThrowWhenSchedulerNotFound() {
        DeactivateSchedulerCommand command = DeactivateSchedulerCommandFixture.create();

        given(schedulerQueryPort.findById(SchedulerId.of(command.schedulerId()))).willReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should save history when scheduler is deactivated")
    void shouldSaveHistoryWhenDeactivated() {
        DeactivateSchedulerCommand command = DeactivateSchedulerCommandFixture.create();
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        given(schedulerQueryPort.findById(SchedulerId.of(command.schedulerId()))).willReturn(Optional.of(scheduler));
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class))).willReturn(scheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        verify(schedulerHistoryPersistencePort).saveSchedulerHistory(any(SchedulerHistory.class));
    }
}

