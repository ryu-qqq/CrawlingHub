package com.ryuqq.crawlinghub.application.task.manager.query;

import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 조회 전용 Manager
 *
 * <p><strong>책임</strong>: CrawlTask 조회 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 QueryPort만 의존, 트랜잭션 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskReadManager {

    private final CrawlTaskQueryPort crawlTaskQueryPort;

    public CrawlTaskReadManager(CrawlTaskQueryPort crawlTaskQueryPort) {
        this.crawlTaskQueryPort = crawlTaskQueryPort;
    }

    /**
     * CrawlTask ID로 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTask (Optional)
     */
    public Optional<CrawlTask> findById(CrawlTaskId crawlTaskId) {
        return crawlTaskQueryPort.findById(crawlTaskId);
    }

    /**
     * Schedule ID와 상태 목록으로 존재 여부 확인
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    public boolean existsByScheduleIdAndStatusIn(
            CrawlSchedulerId crawlSchedulerId, List<CrawlTaskStatus> statuses) {
        return crawlTaskQueryPort.existsByScheduleIdAndStatusIn(crawlSchedulerId, statuses);
    }

    /**
     * 조건으로 CrawlTask 목록 조회
     *
     * @param criteria 조회 조건
     * @return CrawlTask 목록
     */
    public List<CrawlTask> findByCriteria(CrawlTaskCriteria criteria) {
        return crawlTaskQueryPort.findByCriteria(criteria);
    }

    /**
     * 조건으로 CrawlTask 총 개수 조회
     *
     * @param criteria 조회 조건
     * @return 총 개수
     */
    public long countByCriteria(CrawlTaskCriteria criteria) {
        return crawlTaskQueryPort.countByCriteria(criteria);
    }
}
