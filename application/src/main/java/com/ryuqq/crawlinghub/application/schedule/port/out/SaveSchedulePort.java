package com.ryuqq.crawlinghub.application.crawl.schedule.port.out;

import com.ryuqq.crawlinghub.domain.schedule.CrawlSchedule;

/**
 * 스케줄 저장 Port
 *
 * <p>Persistence Adapter에 의해 구현됩니다.
 *
 * @author ryu-qqq
 * @since 2025-10-31
 */
public interface SaveSchedulePort {

    /**
     * 스케줄 저장 (신규 생성 또는 수정)
     *
     * @param schedule 저장할 스케줄
     * @return 저장된 스케줄 (ID 포함)
     */
    CrawlSchedule save(CrawlSchedule schedule);
}
