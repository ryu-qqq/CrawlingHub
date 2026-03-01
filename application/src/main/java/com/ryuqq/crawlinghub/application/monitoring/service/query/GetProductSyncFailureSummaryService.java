package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ProductSyncFailureSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetProductSyncFailureSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GetProductSyncFailureSummaryService implements GetProductSyncFailureSummaryUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetProductSyncFailureSummaryService(
            MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public ProductSyncFailureSummaryResult execute(Duration lookbackDuration) {
        return monitoringCompositeQueryPort.getProductSyncFailureSummary(lookbackDuration);
    }
}
