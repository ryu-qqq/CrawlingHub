package com.ryuqq.crawlinghub.application.scheduler.dto.query;

import com.ryuqq.crawlinghub.domain.eventbridge.vo.SchedulerStatus;

public record ListSchedulersQuery(
        Long sellerId,
        SchedulerStatus status,
        int page,
        int size
) {
}

