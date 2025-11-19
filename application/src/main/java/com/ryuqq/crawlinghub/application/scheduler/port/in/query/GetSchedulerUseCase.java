package com.ryuqq.crawlinghub.application.scheduler.port.in.query;

import com.ryuqq.crawlinghub.application.scheduler.dto.query.GetSchedulerQuery;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerDetailResponse;

public interface GetSchedulerUseCase {
    SchedulerDetailResponse execute(GetSchedulerQuery query);
}

