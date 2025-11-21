package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.DeactivateSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerResponse;

public interface DeactivateSchedulerUseCase {
    SchedulerResponse execute(DeactivateSchedulerCommand command);
}
