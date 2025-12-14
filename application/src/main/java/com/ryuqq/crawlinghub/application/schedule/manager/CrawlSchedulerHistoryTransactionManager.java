package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlScheduleHistoryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import org.springframework.stereotype.Component;

@Component
public class CrawlSchedulerHistoryTransactionManager {

    private final PersistCrawlScheduleHistoryPort persistCrawlScheduleHistoryPort;

    public CrawlSchedulerHistoryTransactionManager(
            PersistCrawlScheduleHistoryPort persistCrawlScheduleHistoryPort) {
        this.persistCrawlScheduleHistoryPort = persistCrawlScheduleHistoryPort;
    }

    public CrawlSchedulerHistoryId persist(CrawlSchedulerHistory crawlSchedulerHistory) {
        return persistCrawlScheduleHistoryPort.persist(crawlSchedulerHistory);
    }
}
