package com.ryuqq.crawlinghub.application.schedule.port.out.command;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;

public interface PersistCrawlScheduleHistoryPort {

    CrawlSchedulerHistoryId persist(CrawlSchedulerHistory crawlSchedulerHistory);
}
