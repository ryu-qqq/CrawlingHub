package com.ryuqq.crawlinghub.application.scheduler.port.in.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.UpdateSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;

public interface UpdateSchedulerUseCase {
    SchedulerResponse execute(UpdateSchedulerCommand command);
}

