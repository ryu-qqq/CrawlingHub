package com.ryuqq.crawlinghub.application.task.port.out.query;

import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import java.util.Optional;

/**
 * CrawlTask Outbox 조회 Port (Port Out)
 *
 * <p><strong>용도</strong>: 재시도 스케줄러에서 PENDING/FAILED 상태 Outbox 조회
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlTaskOutboxQueryPort {

    /**
     * CrawlTask ID로 Outbox 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return Outbox (Optional)
     */
    Optional<CrawlTaskOutbox> findByCrawlTaskId(CrawlTaskId crawlTaskId);

    /**
     * 조건으로 Outbox 목록 조회
     *
     * <p>Criteria 객체를 통해 다양한 조건을 조합하여 조회합니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return Outbox 목록
     */
    List<CrawlTaskOutbox> findByCriteria(CrawlTaskOutboxCriteria criteria);
}
