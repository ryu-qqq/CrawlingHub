package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.query.GetSchedulerHistoryQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerHistoryResponse;

public interface GetSchedulerHistoryUseCase {
    PageResult<SchedulerHistoryResponse> execute(GetSchedulerHistoryQuery query);
}
