package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import java.time.Duration;

public interface GetDashboardSummaryUseCase {

    DashboardSummaryResult execute(Duration lookbackDuration);
}
