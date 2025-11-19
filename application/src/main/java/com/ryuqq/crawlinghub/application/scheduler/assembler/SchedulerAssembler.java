package com.ryuqq.crawlinghub.application.scheduler.assembler;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerHistoryResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.CronExpression;
import java.time.Clock;
import org.springframework.stereotype.Component;

@Component
public class SchedulerAssembler {

    private final Clock clock;

    public SchedulerAssembler(Clock clock) {
        this.clock = clock;
    }

    public SchedulerResponse toResponse(CrawlingScheduler scheduler) {
        return new SchedulerResponse(
            scheduler.getSchedulerId(),
            scheduler.getSellerId(),
            scheduler.getSchedulerName(),
            scheduler.getCronExpression().value(),
            scheduler.getStatus(),
            null,
            scheduler.getCreatedAt()
        );
    }

    public SchedulerDetailResponse toDetailResponse(CrawlingScheduler scheduler) {
        return new SchedulerDetailResponse(
            scheduler.getSchedulerId(),
            scheduler.getSellerId(),
            scheduler.getSchedulerName(),
            scheduler.getCronExpression().value(),
            scheduler.getStatus(),
            null,
            scheduler.getCreatedAt(),
            scheduler.getUpdatedAt()
        );
    }

    public SchedulerSummaryResponse toSummaryResponse(CrawlingScheduler scheduler) {
        return new SchedulerSummaryResponse(
            scheduler.getSchedulerId(),
            scheduler.getSellerId(),
            scheduler.getSchedulerName(),
            scheduler.getCronExpression().value(),
            scheduler.getStatus()
        );
    }

    public CrawlingScheduler toScheduler(RegisterSchedulerCommand command) {
        return CrawlingScheduler.forNew(
            command.sellerId(),
            command.schedulerName(),
            CronExpression.of(command.cronExpression()),
            clock
        );
    }

    public SchedulerHistoryResponse toHistoryResponse(SchedulerHistory history) {
        return SchedulerHistoryResponse.of(
            history.historyId(),
            history.schedulerId(),
            history.attributeName(),
            history.previousValue(),
            history.currentValue(),
            history.occurredAt()
        );
    }
}

