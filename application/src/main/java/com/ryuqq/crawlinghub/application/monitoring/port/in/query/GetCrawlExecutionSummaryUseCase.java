package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlExecutionSummaryResult;
import java.time.Duration;

public interface GetCrawlExecutionSummaryUseCase {

    CrawlExecutionSummaryResult execute(Duration lookbackDuration);
}
