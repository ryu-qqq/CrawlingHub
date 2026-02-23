package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawlTaskSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GetCrawlTaskSummaryService implements GetCrawlTaskSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetCrawlTaskSummaryService(MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public CrawlTaskSummaryResult execute(Duration lookbackDuration) {
        return monitoringCompositeQueryPort.getCrawlTaskSummary(lookbackDuration);
    }
}
