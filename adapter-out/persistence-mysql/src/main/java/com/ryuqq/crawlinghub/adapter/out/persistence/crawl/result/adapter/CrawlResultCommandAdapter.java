package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.adapter;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.entity.CrawlResultEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.mapper.CrawlResultMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.repository.CrawlResultJpaRepository;
import com.ryuqq.crawlinghub.application.crawl.result.port.out.SaveCrawlResultPort;
import com.ryuqq.crawlinghub.domain.crawl.result.CrawlResult;

import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * CrawlResult Command Adapter - CQRS Command Adapter (쓰기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Write 작업 전용 (save)</li>
 *   <li>✅ Domain Aggregate를 Entity로 변환하여 저장</li>
 *   <li>✅ 저장된 Entity를 Domain Aggregate로 변환하여 반환</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Read 작업은 향후 CrawlResultQueryAdapter에서 처리 예정</li>
 *   <li>❌ Query 작업은 이 Adapter에서 금지</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class CrawlResultCommandAdapter implements SaveCrawlResultPort {

    private final CrawlResultJpaRepository jpaRepository;
    private final CrawlResultMapper mapper;

    /**
     * Adapter 생성자
     *
     * @param jpaRepository JPA Repository
     * @param mapper Domain ↔ Entity 변환 Mapper
     */
    public CrawlResultCommandAdapter(
        CrawlResultJpaRepository jpaRepository,
        CrawlResultMapper mapper
    ) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
    }

    /**
     * 크롤링 결과 저장 (신규 생성 전용)
     *
     * <p>Domain Aggregate를 Entity로 변환하여 저장한 후,
     * 저장된 Entity를 다시 Domain Aggregate로 변환하여 반환합니다.</p>
     *
     * @param crawlResult 저장할 크롤링 결과 Aggregate (null 불가)
     * @return 저장된 크롤링 결과 Aggregate (ID 포함)
     * @throws NullPointerException crawlResult가 null인 경우
     */
    @Override
    public CrawlResult save(CrawlResult crawlResult) {
        Objects.requireNonNull(crawlResult, "CrawlResult must not be null");

        // 1. Domain → Entity 변환
        CrawlResultEntity entity = mapper.toEntity(crawlResult);

        // 2. 저장
        CrawlResultEntity savedEntity = jpaRepository.save(entity);

        // 3. Entity → Domain 변환 (ID 포함)
        return mapper.toDomain(savedEntity);
    }
}
