package com.ryuqq.crawlinghub.application.scheduler.port.in.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.ListSchedulersQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerSummaryResponse;

public interface ListSchedulersUseCase {
    PageResult<SchedulerSummaryResponse> execute(ListSchedulersQuery query);
}

