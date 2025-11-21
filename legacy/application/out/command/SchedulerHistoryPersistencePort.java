package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.eventbridge.history.SchedulerHistory;

/** SchedulerHistory Persistence Port */
public interface SchedulerHistoryPersistencePort {

    SchedulerHistory saveSchedulerHistory(SchedulerHistory history);
}
