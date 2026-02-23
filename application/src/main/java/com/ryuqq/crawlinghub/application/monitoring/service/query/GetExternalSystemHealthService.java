package com.ryuqq.crawlinghub.application.monitoring.service.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetExternalSystemHealthUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class GetExternalSystemHealthService implements GetExternalSystemHealthUseCase {

    private final MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    public GetExternalSystemHealthService(
            MonitoringCompositeQueryPort monitoringCompositeQueryPort) {
        this.monitoringCompositeQueryPort = monitoringCompositeQueryPort;
    }

    @Override
    public ExternalSystemHealthResult execute(Duration lookbackDuration) {
        return monitoringCompositeQueryPort.getExternalSystemHealth(lookbackDuration);
    }
}
