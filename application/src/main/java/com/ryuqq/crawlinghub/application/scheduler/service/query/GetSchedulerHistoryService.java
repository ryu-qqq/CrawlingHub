package com.ryuqq.crawlinghub.application.scheduler.service.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerHistoryQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerHistoryQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerHistoryResponse;
import com.ryuqq.crawlinghub.application.scheduler.port.in.query.GetSchedulerHistoryUseCase;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class GetSchedulerHistoryService implements GetSchedulerHistoryUseCase {

    private final SchedulerHistoryQueryPort schedulerHistoryQueryPort;
    private final SchedulerAssembler schedulerAssembler;

    public GetSchedulerHistoryService(
        SchedulerHistoryQueryPort schedulerHistoryQueryPort,
        SchedulerAssembler schedulerAssembler
    ) {
        this.schedulerHistoryQueryPort = schedulerHistoryQueryPort;
        this.schedulerAssembler = schedulerAssembler;
    }

    @Override
    public PageResult<SchedulerHistoryResponse> execute(GetSchedulerHistoryQuery query) {
        PageResult<SchedulerHistory> pageResult = schedulerHistoryQueryPort.findBySchedulerId(
            SchedulerId.of(query.schedulerId()),
            query.page(),
            query.size()
        );

        List<SchedulerHistoryResponse> content = pageResult.content().stream()
            .map(schedulerAssembler::toHistoryResponse)
            .collect(Collectors.toList());

        return new PageResult<SchedulerHistoryResponse>() {
            @Override
            public List<SchedulerHistoryResponse> content() {
                return content;
            }

            @Override
            public int page() {
                return pageResult.page();
            }

            @Override
            public int size() {
                return pageResult.size();
            }

            @Override
            public long totalElements() {
                return pageResult.totalElements();
            }
        };
    }
}

