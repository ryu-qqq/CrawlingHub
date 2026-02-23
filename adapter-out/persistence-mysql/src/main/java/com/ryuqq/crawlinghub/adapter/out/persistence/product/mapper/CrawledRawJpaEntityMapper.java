package com.ryuqq.crawlinghub.adapter.out.persistence.product.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledRaw;
import com.ryuqq.crawlinghub.domain.product.id.CrawledRawId;
import org.springframework.stereotype.Component;

/**
 * CrawledRawJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawledRaw → CrawledRawJpaEntity (저장용)
 *   <li>CrawledRawJpaEntity → CrawledRaw (조회용)
 * </ul>
 *
 * <p><strong>Hexagonal Architecture 관점:</strong>
 *
 * <ul>
 *   <li>Adapter Layer의 책임
 *   <li>Domain과 Infrastructure 기술 분리
 *   <li>Domain은 JPA 의존성 없음
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledRawJpaEntityMapper {

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 CrawledRaw 저장 (ID가 null)
     *   <li>기존 CrawledRaw 수정 (ID가 있음)
     * </ul>
     *
     * @param domain CrawledRaw 도메인
     * @return CrawledRawJpaEntity
     */
    public CrawledRawJpaEntity toEntity(CrawledRaw domain) {
        return CrawledRawJpaEntity.of(
                domain.getIdValue(),
                domain.getCrawlSchedulerId(),
                domain.getSellerId(),
                domain.getItemNo(),
                domain.getCrawlType(),
                domain.getRawData(),
                domain.getStatus(),
                domain.getErrorMessage(),
                domain.getCreatedAt(),
                domain.getProcessedAt());
    }

    /**
     * Entity → Domain 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환
     *   <li>Application Layer로 전달
     * </ul>
     *
     * @param entity CrawledRawJpaEntity
     * @return CrawledRaw 도메인
     */
    public CrawledRaw toDomain(CrawledRawJpaEntity entity) {
        return CrawledRaw.reconstitute(
                CrawledRawId.of(entity.getId()),
                entity.getCrawlSchedulerId(),
                entity.getSellerId(),
                entity.getItemNo(),
                entity.getCrawlType(),
                entity.getRawData(),
                entity.getStatus(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getProcessedAt());
    }
}
