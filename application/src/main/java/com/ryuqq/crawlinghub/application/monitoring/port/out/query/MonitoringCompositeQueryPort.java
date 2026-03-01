package com.ryuqq.crawlinghub.application.monitoring.port.out.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlExecutionSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ProductSyncFailureSummaryResult;
import java.time.Duration;

public interface MonitoringCompositeQueryPort {

    DashboardSummaryResult getDashboardSummary(Duration lookback);

    CrawlTaskSummaryResult getCrawlTaskSummary(Duration lookback);

    OutboxSummaryResult getOutboxSummary();

    CrawledRawSummaryResult getCrawledRawSummary();

    ExternalSystemHealthResult getExternalSystemHealth(Duration lookback);

    ProductSyncFailureSummaryResult getProductSyncFailureSummary(Duration lookback);

    CrawlExecutionSummaryResult getCrawlExecutionSummary(Duration lookback);
}
