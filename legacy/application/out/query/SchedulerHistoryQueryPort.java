package com.ryuqq.crawlinghub.application.port.out.query;

import com.ryuqq.crawlinghub.application.common.pagination.PageResult;
import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;
import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerId;

/** SchedulerHistory Query Port */
public interface SchedulerHistoryQueryPort {

    PageResult<SchedulerHistory> findBySchedulerId(SchedulerId schedulerId, int page, int size);
}
