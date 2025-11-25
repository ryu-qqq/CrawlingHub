package com.ryuqq.crawlinghub.application.schedule.port.out.command;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;

public interface PersistCrawlSchedulePort {

    CrawlSchedulerId persist(CrawlScheduler crawlScheduler);
}
