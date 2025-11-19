package com.ryuqq.crawlinghub.application.scheduler.service.command;

import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.port.in.command.RegisterSchedulerUseCase;
import com.ryuqq.crawlinghub.application.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.port.out.command.OutboxEventPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerHistoryPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.command.SchedulerPersistencePort;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEvent;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxEventType;
import com.ryuqq.crawlinghub.domain.eventbridge.outbox.OutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class RegisterSchedulerService implements RegisterSchedulerUseCase {

    private final SchedulerPersistencePort schedulerPersistencePort;
    private final SchedulerQueryPort schedulerQueryPort;
    private final SellerQueryPort sellerQueryPort;
    private final SchedulerHistoryPersistencePort schedulerHistoryPersistencePort;
    private final OutboxEventPersistencePort outboxEventPersistencePort;
    private final EventBridgeClientPort eventBridgeClientPort;
    private final SchedulerAssembler schedulerAssembler;

    public RegisterSchedulerService(
        SchedulerPersistencePort schedulerPersistencePort,
        SchedulerQueryPort schedulerQueryPort,
        SellerQueryPort sellerQueryPort,
        SchedulerHistoryPersistencePort schedulerHistoryPersistencePort,
        OutboxEventPersistencePort outboxEventPersistencePort,
        EventBridgeClientPort eventBridgeClientPort,
        SchedulerAssembler schedulerAssembler
    ) {
        this.schedulerPersistencePort = schedulerPersistencePort;
        this.schedulerQueryPort = schedulerQueryPort;
        this.sellerQueryPort = sellerQueryPort;
        this.schedulerHistoryPersistencePort = schedulerHistoryPersistencePort;
        this.outboxEventPersistencePort = outboxEventPersistencePort;
        this.eventBridgeClientPort = eventBridgeClientPort;
        this.schedulerAssembler = schedulerAssembler;
    }

    @Override
    public SchedulerResponse execute(RegisterSchedulerCommand command) {
        Seller seller = sellerQueryPort.findById(SellerId.of(command.sellerId()))
            .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new IllegalStateException("Seller not active");
        }

        if (schedulerQueryPort.findBySellerIdAndSchedulerName(SellerId.of(command.sellerId()), command.schedulerName()).isPresent()) {
            throw new IllegalArgumentException("Duplicated scheduler name");
        }

        CrawlingScheduler scheduler = schedulerAssembler.toScheduler(command);
        CrawlingScheduler savedScheduler = schedulerPersistencePort.saveScheduler(scheduler);

        OutboxEvent outboxEvent = OutboxEvent.of(
            null,
            OutboxEventType.SCHEDULER_CREATED,
            savedScheduler.getSchedulerId(),
            "payload",
            OutboxStatus.PENDING,
            0,
            3,
            LocalDateTime.now(),
            null,
            null
        );

        outboxEventPersistencePort.saveOutboxEvent(outboxEvent);

        return schedulerAssembler.toResponse(savedScheduler);
    }
}

