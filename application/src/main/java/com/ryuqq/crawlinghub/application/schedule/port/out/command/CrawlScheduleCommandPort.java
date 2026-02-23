package com.ryuqq.crawlinghub.application.schedule.port.out.command;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;

public interface CrawlScheduleCommandPort {

    CrawlSchedulerId persist(CrawlScheduler crawlScheduler);
}
