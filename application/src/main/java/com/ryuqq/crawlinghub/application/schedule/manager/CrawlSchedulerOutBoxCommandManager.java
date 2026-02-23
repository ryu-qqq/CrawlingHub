package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleOutBoxCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크롤 스케줄러 아웃박스 커맨드 관리자.
 *
 * <p><strong>책임</strong>: 아웃박스 영속화만 담당. 상태 전환은 도메인 메서드를 호출하는 쪽(Processor, Service)에서 수행.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxCommandManager {

    private final CrawlScheduleOutBoxCommandPort crawlScheduleOutBoxCommandPort;

    public CrawlSchedulerOutBoxCommandManager(
            CrawlScheduleOutBoxCommandPort crawlScheduleOutBoxCommandPort) {
        this.crawlScheduleOutBoxCommandPort = crawlScheduleOutBoxCommandPort;
    }

    /**
     * 아웃박스 저장.
     *
     * @param crawlSchedulerOutBox 저장할 아웃박스
     * @return 저장된 아웃박스 ID
     */
    @Transactional
    public CrawlSchedulerOutBoxId persist(CrawlSchedulerOutBox crawlSchedulerOutBox) {
        return crawlScheduleOutBoxCommandPort.persist(crawlSchedulerOutBox);
    }
}
