package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlSchedulePort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import org.springframework.stereotype.Component;

@Component
public class CrawlerSchedulerManager {

    private final PersistCrawlSchedulePort crawlSchedulerPersistencePort;

    public CrawlerSchedulerManager(PersistCrawlSchedulePort crawlSchedulerPersistencePort) {
        this.crawlSchedulerPersistencePort = crawlSchedulerPersistencePort;
    }

    public CrawlSchedulerId persist(CrawlScheduler crawlScheduler) {
        return crawlSchedulerPersistencePort.persist(crawlScheduler);
    }
}
