package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawledRawSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import org.springframework.stereotype.Service;

@Service
public class GetCrawledRawSummaryService implements GetCrawledRawSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetCrawledRawSummaryService(MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public CrawledRawSummaryResult execute() {
        return monitoringCompositeQueryPort.getCrawledRawSummary();
    }
}
