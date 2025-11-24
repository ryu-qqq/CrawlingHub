package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.query.ListSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerSummaryResponse;

public interface ListSchedulersUseCase {
    PageResult<SchedulerSummaryResponse> execute(ListSchedulersQuery query);
}
