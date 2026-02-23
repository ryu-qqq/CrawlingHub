package com.ryuqq.crawlinghub.adapter.out.persistence.task.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.mapper.CrawlTaskOutboxJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.repository.CrawlTaskOutboxQueryDslRepository;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
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

    /**
     * 조건에 맞는 Outbox 개수 조회
     *
     * <p>페이징 처리를 위한 전체 개수 조회에 사용됩니다.
     *
     * @param criteria 조회 조건 (CrawlTaskOutboxCriteria)
     * @return 조건에 맞는 Outbox 개수
     */
    @Override
    public long countByCriteria(CrawlTaskOutboxCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }

    /**
     * delaySeconds 이상 경과한 PENDING 상태 Outbox 조회
     *
     * @param limit 최대 조회 건수
     * @param delaySeconds 생성 후 경과해야 할 최소 시간 (초)
     * @return PENDING 상태의 Outbox Domain 목록
     */
    @Override
    public List<CrawlTaskOutbox> findPendingOlderThan(int limit, int delaySeconds) {
        return queryDslRepository.findPendingOlderThan(limit, delaySeconds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * timeoutSeconds 이상 PROCESSING 상태인 좀비 Outbox 조회
     *
     * @param limit 최대 조회 건수
     * @param timeoutSeconds PROCESSING 상태 타임아웃 기준 (초)
     * @return PROCESSING 좀비 Outbox Domain 목록
     */
    @Override
    public List<CrawlTaskOutbox> findStaleProcessing(int limit, long timeoutSeconds) {
        return queryDslRepository.findStaleProcessing(limit, timeoutSeconds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    /**
     * FAILED 상태에서 delaySeconds 이상 경과한 재시도 가능 Outbox 조회
     *
     * @param limit 최대 조회 건수
     * @param delaySeconds FAILED 후 경과해야 할 최소 시간 (초)
     * @return FAILED 상태의 재시도 가능 Outbox Domain 목록
     */
    @Override
    public List<CrawlTaskOutbox> findFailedOlderThan(int limit, int delaySeconds) {
        return queryDslRepository.findFailedOlderThan(limit, delaySeconds).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
