package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutboxQueryAdapter - CrawlTaskOutbox Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Task ID로 Outbox 조회 (findByCrawlTaskId)
 *   <li>조건별 목록 조회 (findByCriteria)
 *   <li>QueryDslRepository 호출
 *   <li>Mapper를 통한 Entity → Domain 변환
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직
 *   <li>❌ 저장/수정/삭제 (CommandAdapter로 분리)
 *   <li>❌ JPAQueryFactory 직접 사용 (QueryDslRepository에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxQueryAdapter implements CrawlTaskOutboxQueryPort {

    private final CrawlTaskOutboxQueryDslRepository queryDslRepository;
    private final CrawlTaskOutboxJpaEntityMapper mapper;

    public CrawlTaskOutboxQueryAdapter(
            CrawlTaskOutboxQueryDslRepository queryDslRepository,
            CrawlTaskOutboxJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * CrawlTask ID로 Outbox 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return Outbox Domain (Optional)
     */
    @Override
    public Optional<CrawlTaskOutbox> findByCrawlTaskId(CrawlTaskId crawlTaskId) {
        return queryDslRepository.findByCrawlTaskId(crawlTaskId.value()).map(mapper::toDomain);
    }

    /**
     * 조건으로 Outbox 목록 조회
     *
     * <p>Criteria 객체를 통해 다양한 조건을 조합하여 조회합니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return Outbox Domain 목록
     */
    @Override
    public List<CrawlTaskOutbox> findByCriteria(CrawlTaskOutboxCriteria criteria) {
        List<CrawlTaskOutboxJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }
}
