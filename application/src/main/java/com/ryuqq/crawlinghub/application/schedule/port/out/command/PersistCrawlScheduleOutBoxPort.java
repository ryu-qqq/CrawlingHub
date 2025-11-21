package com.ryuqq.crawlinghub.application.schedule.port.out.command;

import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOutBoxId;

public interface PersistCrawlScheduleOutBoxPort {

    CrawlSchedulerOutBoxId persist(CrawlSchedulerOutBox crawlSchedulerOutBox);
}
