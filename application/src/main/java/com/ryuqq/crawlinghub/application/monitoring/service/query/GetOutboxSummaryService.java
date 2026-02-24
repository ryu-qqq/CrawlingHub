package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetOutboxSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import org.springframework.stereotype.Service;

@Service
public class GetOutboxSummaryService implements GetOutboxSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetOutboxSummaryService(MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public OutboxSummaryResult execute() {
        return monitoringCompositeQueryPort.getOutboxSummary();
    }
}
