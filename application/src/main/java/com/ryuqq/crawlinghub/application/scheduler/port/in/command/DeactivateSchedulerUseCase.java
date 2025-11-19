package com.ryuqq.crawlinghub.application.scheduler.port.in.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.DeactivateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;

public interface DeactivateSchedulerUseCase {
    SchedulerResponse execute(DeactivateSchedulerCommand command);
}

