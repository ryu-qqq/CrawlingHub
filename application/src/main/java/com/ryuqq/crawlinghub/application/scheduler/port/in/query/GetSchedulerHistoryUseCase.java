package com.ryuqq.crawlinghub.application.scheduler.port.in.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerHistoryQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerHistoryResponse;

public interface GetSchedulerHistoryUseCase {
    PageResult<SchedulerHistoryResponse> execute(GetSchedulerHistoryQuery query);
}

