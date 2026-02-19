package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlScheduleOutBoxPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크롤 스케줄러 아웃박스 관리자.
 *
 * <p><strong>책임</strong>: 아웃박스 영속성 및 상태 변경 관리
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxTransactionManager {

    private final PersistCrawlScheduleOutBoxPort persistCrawlScheduleOutBoxPort;
    private final TimeProvider timeProvider;

    public CrawlSchedulerOutBoxTransactionManager(
            PersistCrawlScheduleOutBoxPort persistCrawlScheduleOutBoxPort,
            TimeProvider timeProvider) {
        this.persistCrawlScheduleOutBoxPort = persistCrawlScheduleOutBoxPort;
        this.timeProvider = timeProvider;
    }

    /**
     * 아웃박스 저장.
     *
     * @param crawlSchedulerOutBox 저장할 아웃박스
     * @return 저장된 아웃박스 ID
     */
    @Transactional
    public CrawlSchedulerOutBoxId persist(CrawlSchedulerOutBox crawlSchedulerOutBox) {
        return persistCrawlScheduleOutBoxPort.persist(crawlSchedulerOutBox);
    }

    /**
     * 아웃박스 완료 처리.
     *
     * <p>AWS EventBridge 동기화 성공 시 호출
     *
     * @param outBox 완료 처리할 아웃박스
     */
    @Transactional
    public void markAsCompleted(CrawlSchedulerOutBox outBox) {
        outBox.markAsCompleted(timeProvider.now());
        persistCrawlScheduleOutBoxPort.persist(outBox);
    }

    /**
     * 아웃박스 실패 처리.
     *
     * <p>AWS EventBridge 동기화 실패 시 호출
     *
     * @param outBox 실패 처리할 아웃박스
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void markAsFailed(CrawlSchedulerOutBox outBox, String errorMessage) {
        outBox.markAsFailed(errorMessage, timeProvider.now());
        persistCrawlScheduleOutBoxPort.persist(outBox);
    }
}
