package com.ryuqq.crawlinghub.application.scheduler.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.fixture.assembler.SchedulerAssemblerFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.command.RegisterSchedulerCommandFixture;
import com.ryuqq.crawlinghub.application.scheduler.fixture.response.SchedulerResponseFixture;
import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.service.command.RegisterSchedulerService;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.fixture.eventbridge.CrawlingSchedulerFixture;
import com.ryuqq.crawlinghub.domain.fixture.seller.SellerFixture;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("RegisterSchedulerUseCase")
class RegisterSchedulerUseCaseTest {

    private final SchedulerPersistencePort schedulerPersistencePort = Mockito.mock(SchedulerPersistencePort.class);
    private final SchedulerQueryPort schedulerQueryPort = Mockito.mock(SchedulerQueryPort.class);
    private final SellerQueryPort sellerQueryPort = Mockito.mock(SellerQueryPort.class);
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort = Mockito.mock(SchedulerHistoryPersistencePort.class);
    private final OutboxEventPersistencePort outboxEventPersistencePort = Mockito.mock(OutboxEventPersistencePort.class);
    private final EventBridgeClientPort eventBridgeClientPort = Mockito.mock(EventBridgeClientPort.class);
    private final SchedulerAssembler schedulerAssembler = SchedulerAssemblerFixture.create();

    private RegisterSchedulerService useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterSchedulerService(
            schedulerPersistencePort,
            schedulerQueryPort,
            sellerQueryPort,
            schedulerHistoryPersistencePort,
            outboxEventPersistencePort,
            eventBridgeClientPort,
            schedulerAssembler
        );
    }

    @Test
    @DisplayName("should register scheduler successfully")
    void shouldRegisterSchedulerSuccessfully() {
        RegisterSchedulerCommand command = RegisterSchedulerCommandFixture.create();
        Seller seller = SellerFixture.anActiveSeller();
        CrawlingScheduler scheduler = CrawlingSchedulerFixture.of();

        given(sellerQueryPort.findById(any())).willReturn(Optional.of(seller));
        given(schedulerQueryPort.findBySellerIdAndSchedulerName(any(), eq(command.schedulerName())))
            .willReturn(Optional.empty());
        given(schedulerPersistencePort.saveScheduler(any(CrawlingScheduler.class))).willReturn(scheduler);
        given(outboxEventPersistencePort.saveOutboxEvent(any(OutboxEvent.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        SchedulerResponse response = useCase.execute(command);

        assertThat(response.schedulerId()).isEqualTo(scheduler.getSchedulerId());
        verify(schedulerPersistencePort).saveScheduler(any(CrawlingScheduler.class));
        verify(outboxEventPersistencePort).saveOutboxEvent(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("should throw when seller is not active")
    void shouldThrowWhenSellerNotActive() {
        RegisterSchedulerCommand command = RegisterSchedulerCommandFixture.create();
        Seller inactiveSeller = SellerFixture.anInactiveSeller();

        given(sellerQueryPort.findById(any())).willReturn(Optional.of(inactiveSeller));

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("should throw when scheduler name duplicates per seller")
    void shouldThrowWhenDuplicateName() {
        RegisterSchedulerCommand command = RegisterSchedulerCommandFixture.create();
        Seller seller = SellerFixture.anActiveSeller();

        given(sellerQueryPort.findById(any())).willReturn(Optional.of(seller));
        given(schedulerQueryPort.findBySellerIdAndSchedulerName(any(), eq(command.schedulerName())))
            .willReturn(Optional.of(CrawlingSchedulerFixture.of()));

        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class);
    }
}

