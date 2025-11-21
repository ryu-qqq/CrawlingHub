package com.ryuqq.crawlinghub.application.schedule.port.in.command;

import com.ryuqq.crawlinghub.application.schedule.dto.command.UpdateSchedulerCommand;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerResponse;

public interface UpdateSchedulerUseCase {
    SchedulerResponse execute(UpdateSchedulerCommand command);
}
