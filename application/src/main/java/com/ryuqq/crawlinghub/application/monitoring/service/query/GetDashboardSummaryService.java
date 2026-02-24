package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetDashboardSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GetDashboardSummaryService implements GetDashboardSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetDashboardSummaryService(MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public DashboardSummaryResult execute(Duration lookbackDuration) {
        return monitoringCompositeQueryPort.getDashboardSummary(lookbackDuration);
    }
}
