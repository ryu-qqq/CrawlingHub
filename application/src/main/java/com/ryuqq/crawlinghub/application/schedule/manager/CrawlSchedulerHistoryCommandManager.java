package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleHistoryCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import org.springframework.stereotype.Component;

@Component
public class CrawlSchedulerHistoryCommandManager {

    private final CrawlScheduleHistoryCommandPort crawlScheduleHistoryCommandPort;

    public CrawlSchedulerHistoryCommandManager(
            CrawlScheduleHistoryCommandPort crawlScheduleHistoryCommandPort) {
        this.crawlScheduleHistoryCommandPort = crawlScheduleHistoryCommandPort;
    }

    public CrawlSchedulerHistoryId persist(CrawlSchedulerHistory crawlSchedulerHistory) {
        return crawlScheduleHistoryCommandPort.persist(crawlSchedulerHistory);
    }
}
