package com.ryuqq.crawlinghub.application.schedule.port.in.query;

import com.ryuqq.crawlinghub.application.schedule.dto.query.GetSchedulerQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerDetailResponse;

public interface GetSchedulerUseCase {
    SchedulerDetailResponse execute(GetSchedulerQuery query);
}
