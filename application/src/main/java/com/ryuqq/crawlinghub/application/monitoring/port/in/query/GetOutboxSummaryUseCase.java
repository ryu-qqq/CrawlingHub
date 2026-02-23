package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;

public interface GetOutboxSummaryUseCase {

    OutboxSummaryResult execute();
}
