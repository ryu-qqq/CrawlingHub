package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerHistoryQueryPort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.DeactivateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.UpdateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.DeactivateSchedulerCommandFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.RegisterSchedulerCommandFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.UpdateSchedulerCommandFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.query.GetSchedulerQueryFixture;
import com.ryuqq.crawlinghub.application.scheduler.service.command.DeactivateSchedulerService;
import com.ryuqq.crawlinghub.application.scheduler.service.command.RegisterSchedulerService;
import com.ryuqq.crawlinghub.application.scheduler.service.command.UpdateSchedulerService;
import com.ryuqq.crawlinghub.application.scheduler.service.query.GetSchedulerService;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("SchedulerUseCase Integration Test")
class SchedulerUseCaseIntegrationTest {

    // Shared Ports
    private final SchedulerPersistencePort schedulerPersistencePort = Mockito.mock(SchedulerPersistencePort.class);
    private final SchedulerQueryPort schedulerQueryPort = Mockito.mock(SchedulerQueryPort.class);
    private final SellerQueryPort sellerQueryPort = Mockito.mock(SellerQueryPort.class);
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort = Mockito.mock(SchedulerHistoryPersistencePort.class);
    private final OutboxEventPersistencePort outboxEventPersistencePort = Mockito.mock(OutboxEventPersistencePort.class);
    private final EventBridgeClientPort eventBridgeClientPort = Mockito.mock(EventBridgeClientPort.class);
    private final SchedulerHistoryQueryPort schedulerHistoryQueryPort = Mockito.mock(SchedulerHistoryQueryPort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    // UseCases
    private RegisterSchedulerService registerUseCase;
    private GetSchedulerService getUseCase;
    private UpdateSchedulerService updateUseCase;
    private DeactivateSchedulerService deactivateUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterSchedulerService(
            schedulerPersistencePort,
            schedulerQueryPort,
            sellerQueryPort,
            schedulerHistoryPersistencePort,
            outboxEventPersistencePort,
            eventBridgeClientPort,
            schedulerAssembler
        );

        getUseCase = new GetSchedulerService(
            schedulerQueryPort,
            schedulerAssembler
        );

        updateUseCase = new UpdateSchedulerService(
            schedulerPersistencePort,
            schedulerQueryPort,
            schedulerHistoryPersistencePort,
            outboxEventPersistencePort,
            schedulerAssembler
        );

        deactivateUseCase = new DeactivateSchedulerService(
            schedulerPersistencePort,
            schedulerQueryPort,
            schedulerHistoryPersistencePort,
            outboxEventPersistencePort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should complete full flow: Register → Get → Update → Get → Deactivate")
    void shouldCompleteFullFlow() {
        // Given: 초기 설정
        RegisterSchedulerCommand registerCommand = RegisterSchedulerCommandFixture.create();
        Seller activeSeller = SellerFixture.anActiveSeller();
        CrawlingScheduler createdScheduler = CrawlingSchedulerFixture.aReconstitutedScheduler();

        // 1. Register
        given(sellerQueryPort.findById(any())).willReturn(Optional.of(activeSeller));
        given(schedulerQueryPort.findBySellerIdAndSchedulerName(any(), eq(registerCommand.schedulerName())))
            .willReturn(Optional.empty());
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class)))
            .willReturn(createdScheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        SchedulerResponse registeredResponse = registerUseCase.execute(registerCommand);

        assertThat(registeredResponse.schedulerId()).isNotNull();
        Long schedulerId = createdScheduler.getSchedulerId();
        assertThat(registeredResponse.schedulerId()).isEqualTo(schedulerId);
        verify(schedulerPersistencePort).saveScheduler(any(CrawlingScheduler.class));
        verify(outboxEventPersistencePort).saveOutboxEvent(argThat(event ->
            event.eventType() == OutboxEventType.SCHEDULER_CREATED &&
            event.status() == OutboxStatus.PENDING
        ));

        // 2. Get (첫 번째 조회)
        GetSchedulerQuery getQuery = new GetSchedulerQuery(schedulerId);

        given(schedulerQueryPort.findById(SchedulerId.of(schedulerId)))
            .willReturn(Optional.of(createdScheduler));

        SchedulerDetailResponse firstGetResponse = getUseCase.execute(getQuery);

        assertThat(firstGetResponse.schedulerId()).isEqualTo(schedulerId);
        assertThat(firstGetResponse.schedulerName()).isEqualTo(createdScheduler.getSchedulerName());

        // 3. Update
        UpdateSchedulerCommand updateCommand = UpdateSchedulerCommandFixture.create();
        UpdateSchedulerCommand updateCommandWithId = new UpdateSchedulerCommand(
            schedulerId,
            updateCommand.schedulerName(),
            updateCommand.cronExpression(),
            updateCommand.status()
        );

        given(schedulerQueryPort.findById(SchedulerId.of(schedulerId)))
            .willReturn(Optional.of(createdScheduler));
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class)))
            .willReturn(createdScheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        SchedulerResponse updatedResponse = updateUseCase.execute(updateCommandWithId);

        assertThat(updatedResponse.schedulerId()).isEqualTo(schedulerId);
        verify(schedulerHistoryPersistencePort).saveSchedulerHistory(any());
        verify(outboxEventPersistencePort).saveOutboxEvent(argThat(event ->
            event.eventType() == OutboxEventType.SCHEDULER_UPDATED &&
            event.status() == OutboxStatus.PENDING
        ));

        // 4. Get (두 번째 조회 - 업데이트 후)
        SchedulerDetailResponse secondGetResponse = getUseCase.execute(getQuery);

        assertThat(secondGetResponse.schedulerId()).isEqualTo(schedulerId);

        // 5. Deactivate
        DeactivateSchedulerCommand deactivateCommand = DeactivateSchedulerCommandFixture.create();
        DeactivateSchedulerCommand deactivateCommandWithId = new DeactivateSchedulerCommand(schedulerId);

        given(schedulerQueryPort.findById(SchedulerId.of(schedulerId)))
            .willReturn(Optional.of(createdScheduler));
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class)))
            .willReturn(createdScheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        SchedulerResponse deactivatedResponse = deactivateUseCase.execute(deactivateCommandWithId);

        assertThat(deactivatedResponse.schedulerId()).isEqualTo(schedulerId);
        assertThat(deactivatedResponse.status()).isEqualTo(SchedulerStatus.INACTIVE);
        verify(schedulerHistoryPersistencePort, Mockito.atLeastOnce()).saveSchedulerHistory(any());
        verify(outboxEventPersistencePort).saveOutboxEvent(argThat(event ->
            event.eventType() == OutboxEventType.SCHEDULER_DELETED &&
            event.status() == OutboxStatus.PENDING
        ));
    }
}

