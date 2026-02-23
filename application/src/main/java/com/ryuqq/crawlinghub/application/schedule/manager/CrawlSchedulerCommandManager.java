package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CrawlSchedulerCommandManager {

    private final CrawlScheduleCommandPort crawlSchedulerPersistencePort;

    public CrawlSchedulerCommandManager(CrawlScheduleCommandPort crawlSchedulerPersistencePort) {
        this.crawlSchedulerPersistencePort = crawlSchedulerPersistencePort;
    }

    @Transactional
    public CrawlSchedulerId persist(CrawlScheduler crawlScheduler) {
        return crawlSchedulerPersistencePort.persist(crawlScheduler);
    }
}
