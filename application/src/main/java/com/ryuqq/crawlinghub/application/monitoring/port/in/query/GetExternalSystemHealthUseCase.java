package com.ryuqq.crawlinghub.application.monitoring.port.in.query;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import java.time.Duration;

public interface GetExternalSystemHealthUseCase {

    ExternalSystemHealthResult execute(Duration lookbackDuration);
}
