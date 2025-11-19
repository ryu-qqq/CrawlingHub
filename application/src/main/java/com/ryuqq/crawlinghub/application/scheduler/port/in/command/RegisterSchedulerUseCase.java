package com.ryuqq.crawlinghub.application.scheduler.port.in.command;

import com.ryuqq.crawlinghub.application.scheduler.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.scheduler.dto.response.SchedulerResponse;

public interface RegisterSchedulerUseCase {
    SchedulerResponse execute(RegisterSchedulerCommand command);
}

