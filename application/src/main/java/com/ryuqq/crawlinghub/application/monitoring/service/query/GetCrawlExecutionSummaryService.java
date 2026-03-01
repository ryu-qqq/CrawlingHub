package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlExecutionSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawlExecutionSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GetCrawlExecutionSummaryService implements GetCrawlExecutionSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetCrawlExecutionSummaryService(
            MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public CrawlExecutionSummaryResult execute(Duration lookbackDuration) {
        return monitoringCompositeQueryPort.getCrawlExecutionSummary(lookbackDuration);
    }
}
