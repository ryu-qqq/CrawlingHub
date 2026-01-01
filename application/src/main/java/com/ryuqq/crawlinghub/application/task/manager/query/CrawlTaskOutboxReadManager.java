package com.ryuqq.crawlinghub.application.task.manager.query;

import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutbox 조회 전용 Manager
 *
 * <p><strong>책임</strong>: CrawlTaskOutbox 조회 작업 위임
 *
 * <p><strong>규칙</strong>:
 *
 * <ul>
 *   <li>단일 QueryPort만 의존
 *   <li>트랜잭션 없음 (조회 전용)
 *   <li>Service에서 직접 Port 접근 대신 Manager 통해 조회
 * </ul>
 *
 * <p><strong>제공 기능</strong>:
 *
 * <ul>
 *   <li>CrawlTaskId로 단건 조회
 *   <li>Criteria 기반 목록 조회
 *   <li>Criteria 기반 개수 조회 (페이징용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxReadManager {

    private final CrawlTaskOutboxQueryPort outboxQueryPort;

    /**
     * CrawlTaskOutboxReadManager 생성자
     *
     * @param outboxQueryPort Outbox 조회 Port
     */
    public CrawlTaskOutboxReadManager(CrawlTaskOutboxQueryPort outboxQueryPort) {
        this.outboxQueryPort = outboxQueryPort;
    }

    /**
     * CrawlTask ID로 Outbox 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return Outbox (Optional)
     */
    public Optional<CrawlTaskOutbox> findByCrawlTaskId(CrawlTaskId crawlTaskId) {
        return outboxQueryPort.findByCrawlTaskId(crawlTaskId);
    }

    /**
     * 조건으로 Outbox 목록 조회
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return Outbox 목록
     */
    public List<CrawlTaskOutbox> findByCriteria(CrawlTaskOutboxCriteria criteria) {
        return outboxQueryPort.findByCriteria(criteria);
    }

    /**
     * 조건에 맞는 Outbox 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회에 사용됩니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return 조건에 맞는 Outbox 개수
     */
    public long countByCriteria(CrawlTaskOutboxCriteria criteria) {
        return outboxQueryPort.countByCriteria(criteria);
    }
}
