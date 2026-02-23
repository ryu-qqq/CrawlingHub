package com.ryuqq.crawlinghub.application.schedule.manager;

import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 크롤 스케줄러 아웃박스 조회 관리자.
 *
 * <p><strong>책임</strong>: CrawlSchedulerOutBoxQueryPort 래핑, 트랜잭션 읽기 전용 보장
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerOutBoxReadManager {

    private final CrawlSchedulerOutBoxQueryPort outBoxQueryPort;

    public CrawlSchedulerOutBoxReadManager(CrawlSchedulerOutBoxQueryPort outBoxQueryPort) {
        this.outBoxQueryPort = outBoxQueryPort;
    }

    @Transactional(readOnly = true)
    public Optional<CrawlSchedulerOutBox> findByHistoryId(CrawlSchedulerHistoryId historyId) {
        return outBoxQueryPort.findByHistoryId(historyId);
    }

    @Transactional(readOnly = true)
    public List<CrawlSchedulerOutBox> findByStatus(CrawlSchedulerOubBoxStatus status, int limit) {
        return outBoxQueryPort.findByStatus(status, limit);
    }

    @Transactional(readOnly = true)
    public List<CrawlSchedulerOutBox> findPendingOlderThan(int limit, int delaySeconds) {
        return outBoxQueryPort.findPendingOlderThan(limit, delaySeconds);
    }

    @Transactional(readOnly = true)
    public List<CrawlSchedulerOutBox> findStaleProcessing(int limit, long timeoutSeconds) {
        return outBoxQueryPort.findStaleProcessing(limit, timeoutSeconds);
    }
}
