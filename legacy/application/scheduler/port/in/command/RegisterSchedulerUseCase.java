package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.RegisterSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerResponse;

public interface RegisterSchedulerUseCase {
    SchedulerResponse execute(RegisterSchedulerCommand command);
}
