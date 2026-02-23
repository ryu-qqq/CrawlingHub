package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import java.time.Duration;

public interface GetCrawlTaskSummaryUseCase {

    CrawlTaskSummaryResult execute(Duration lookbackDuration);
}
