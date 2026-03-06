package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ProductSyncFailureSummaryResult;
import java.time.Duration;

public interface GetProductSyncFailureSummaryUseCase {

    ProductSyncFailureSummaryResult execute(Duration lookbackDuration);
}
