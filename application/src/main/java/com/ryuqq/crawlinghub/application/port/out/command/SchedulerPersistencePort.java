package com.ryuqq.crawlinghub.application.port.out.command;

import com.ryuqq.crawlinghub.domain.eventbridge.aggregate.CrawlingScheduler;

/**
 * Scheduler Persistence Port
 */
public interface SchedulerPersistencePort {

    CrawlingScheduler saveScheduler(CrawlingScheduler scheduler);

    void deleteScheduler(Long schedulerId);
}

