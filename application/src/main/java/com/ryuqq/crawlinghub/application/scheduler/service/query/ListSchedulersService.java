package com.ryuqq.crawlinghub.application.scheduler.service.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.port.out.query.SchedulerQueryPort;
import com.ryuqq.crawlinghub.application.scheduler.assembler.SchedulerAssembler;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.ListSchedulersQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;
import com.ryuqq.crawlinghub.application.scheduler.port.in.query.ListSchedulersUseCase;
import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ListSchedulersService implements ListSchedulersUseCase {

    private final SchedulerQueryPort schedulerQueryPort;
    private final SchedulerAssembler schedulerAssembler;

    public ListSchedulersService(
        SchedulerQueryPort schedulerQueryPort,
        SchedulerAssembler schedulerAssembler
    ) {
        this.schedulerQueryPort = schedulerQueryPort;
        this.schedulerAssembler = schedulerAssembler;
    }

    @Override
    public PageResult<SchedulerSummaryResponse> execute(ListSchedulersQuery query) {
        PageResult<CrawlingScheduler> pageResult = schedulerQueryPort.findAllBySellerIdAndStatus(
            SellerId.of(query.sellerId()),
            query.status(),
            query.page(),
            query.size()
        );

        List<SchedulerSummaryResponse> content = pageResult.content().stream()
            .map(schedulerAssembler::toSummaryResponse)
            .collect(Collectors.toList());

        return new PageResult<SchedulerSummaryResponse>() {
            @Override
            public List<SchedulerSummaryResponse> content() {
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

