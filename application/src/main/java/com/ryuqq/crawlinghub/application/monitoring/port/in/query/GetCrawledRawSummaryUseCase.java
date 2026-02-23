package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;

public interface GetCrawledRawSummaryUseCase {

    CrawledRawSummaryResult execute();
}
