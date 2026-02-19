package com.ryuqq.crawlinghub.application.schedule.port.out.command;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;

public interface PersistCrawlScheduleHistoryPort {

    CrawlSchedulerHistoryId persist(CrawlSchedulerHistory crawlSchedulerHistory);
}
