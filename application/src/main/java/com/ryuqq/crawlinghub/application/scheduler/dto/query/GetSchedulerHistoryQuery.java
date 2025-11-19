package com.ryuqq.crawlinghub.application.scheduler.dto.query;

public record GetSchedulerHistoryQuery(
        Long schedulerId,
        int page,
        int size
) {
}

