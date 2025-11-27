package com.ryuqq.crawlinghub.adapter.out.persistence.execution.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.mapper.CrawlExecutionJpaEntityMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository.CrawlExecutionQueryDslRepository;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * CrawlExecutionQueryAdapter - CrawlExecution Query Adapter
 *
 * <p>CQRS의 Query(읽기) 담당 Adapter입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>단건 조회 (findById)
 *   <li>조건별 목록 조회 (findByCriteria)
 *   <li>조건별 개수 조회 (countByCriteria)
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
public class CrawlExecutionQueryAdapter implements CrawlExecutionQueryPort {

    private final CrawlExecutionQueryDslRepository queryDslRepository;
    private final CrawlExecutionJpaEntityMapper mapper;

    public CrawlExecutionQueryAdapter(
            CrawlExecutionQueryDslRepository queryDslRepository,
            CrawlExecutionJpaEntityMapper mapper) {
        this.queryDslRepository = queryDslRepository;
        this.mapper = mapper;
    }

    /**
     * ID로 CrawlExecution 단건 조회
     *
     * @param crawlExecutionId CrawlExecution ID
     * @return CrawlExecution Domain (Optional)
     */
    @Override
    public Optional<CrawlExecution> findById(CrawlExecutionId crawlExecutionId) {
        return queryDslRepository.findById(crawlExecutionId.value()).map(mapper::toDomain);
    }

    /**
     * 조건으로 CrawlExecution 목록 조회
     *
     * <p>Criteria 객체를 통해 다양한 조건을 조합하여 조회합니다.
     *
     * @param criteria 조회 조건 (CrawlExecutionCriteria)
     * @return CrawlExecution Domain 목록
     */
    @Override
    public List<CrawlExecution> findByCriteria(CrawlExecutionCriteria criteria) {
        List<CrawlExecutionJpaEntity> entities = queryDslRepository.findByCriteria(criteria);
        return entities.stream().map(mapper::toDomain).toList();
    }

    /**
     * 조건으로 CrawlExecution 총 개수 조회
     *
     * @param criteria 조회 조건 (CrawlExecutionCriteria)
     * @return 총 개수
     */
    @Override
    public long countByCriteria(CrawlExecutionCriteria criteria) {
        return queryDslRepository.countByCriteria(criteria);
    }
}
