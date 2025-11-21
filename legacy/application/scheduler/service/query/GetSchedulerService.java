package com.ryuqq.crawlinghub.application.schedule.service.query;

import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.schedule.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.GetSchedulerQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.GetSchedulerUseCase;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import org.springframework.stereotype.Service;

@Service
public class GetSchedulerService implements GetSchedulerUseCase {

    private final SchedulerQueryPort schedulerQueryPort;
    private final SchedulerAssembler schedulerAssembler;

    public GetSchedulerService(
            SchedulerQueryPort schedulerQueryPort, SchedulerAssembler schedulerAssembler) {
        this.schedulerQueryPort = schedulerQueryPort;
        this.schedulerAssembler = schedulerAssembler;
    }

    @Override
    public SchedulerDetailResponse execute(GetSchedulerQuery query) {
        return schedulerQueryPort
                .findById(SchedulerId.of(query.schedulerId()))
                .map(schedulerAssembler::toDetailResponse)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Scheduler not found: " + query.schedulerId()));
    }
}
