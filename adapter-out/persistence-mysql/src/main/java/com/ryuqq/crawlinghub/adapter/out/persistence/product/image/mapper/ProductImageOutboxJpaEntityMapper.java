package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/**
 * ProductImageOutboxJpaEntityMapper - Entity <-> Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 CrawledProductImageOutbox 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>CrawledProductImageOutbox -> ProductImageOutboxJpaEntity (저장용)
 *   <li>ProductImageOutboxJpaEntity -> CrawledProductImageOutbox (조회용)
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
public class ProductImageOutboxJpaEntityMapper {

    /**
     * Domain -> Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 Outbox 저장
     *   <li>기존 Outbox 상태 업데이트
     * </ul>
     *
     * @param domain CrawledProductImageOutbox 도메인
     * @return ProductImageOutboxJpaEntity
     */
    public ProductImageOutboxJpaEntity toEntity(CrawledProductImageOutbox domain) {
        return ProductImageOutboxJpaEntity.of(
                domain.getId(),
                domain.getCrawledProductIdValue(),
                domain.getImageType(),
                domain.getOriginalUrl(),
                domain.getIdempotencyKey(),
                domain.getS3Url(),
                domain.getStatus(),
                domain.getRetryCount(),
                domain.getErrorMessage(),
                toLocalDateTime(domain.getCreatedAt()),
                toLocalDateTime(domain.getProcessedAt()));
    }

    /**
     * Entity -> Domain 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>데이터베이스에서 조회한 Entity를 Domain으로 변환
     *   <li>Application Layer로 전달
     * </ul>
     *
     * @param entity ProductImageOutboxJpaEntity
     * @return CrawledProductImageOutbox 도메인
     */
    public CrawledProductImageOutbox toDomain(ProductImageOutboxJpaEntity entity) {
        return CrawledProductImageOutbox.reconstitute(
                entity.getId(),
                CrawledProductId.of(entity.getCrawledProductId()),
                entity.getOriginalUrl(),
                entity.getImageType(),
                entity.getIdempotencyKey(),
                entity.getS3Url(),
                entity.getStatus(),
                entity.getRetryCount(),
                entity.getErrorMessage(),
                toInstant(entity.getCreatedAt()),
                toInstant(entity.getProcessedAt()));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
    }
}
