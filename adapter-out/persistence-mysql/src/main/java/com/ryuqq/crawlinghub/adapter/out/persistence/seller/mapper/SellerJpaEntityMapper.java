package com.ryuqq.crawlinghub.adapter.out.persistence.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.seller.vo.MustItSellerName;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerName;
import org.springframework.stereotype.Component;

/**
 * SellerJpaEntityMapper - Entity ↔ Domain 변환 Mapper
 *
 * <p>Persistence Layer의 JPA Entity와 Domain Layer의 Domain 객체 간 변환을 담당합니다.
 *
 * <p><strong>변환 책임:</strong>
 *
 * <ul>
 *   <li>Seller → SellerJpaEntity (저장용)
 *   <li>SellerJpaEntity → Seller (조회용)
 *   <li>Value Object 추출 및 재구성
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
public class SellerJpaEntityMapper {

    private final ClockHolder clockHolder;

    public SellerJpaEntityMapper(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * Domain → Entity 변환
     *
     * <p><strong>사용 시나리오:</strong>
     *
     * <ul>
     *   <li>신규 Seller 저장 (ID가 null)
     *   <li>기존 Seller 수정 (ID가 있음)
     * </ul>
     *
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Domain.getSellerIdValue() → Entity.id
     *   <li>MustItSellerName: Domain.getMustItSellerNameValue() → Entity.mustItSellerName
     *   <li>SellerName: Domain.getSellerNameValue() → Entity.sellerName
     *   <li>Status: Domain.getStatus() → Entity.status
     *   <li>CreatedAt: Domain.getCreatedAt() → Entity.createdAt
     *   <li>UpdatedAt: Domain.getUpdatedAt() → Entity.updatedAt
     * </ul>
     *
     * @param domain Seller 도메인
     * @return SellerJpaEntity
     */
    public SellerJpaEntity toEntity(Seller domain) {
        return SellerJpaEntity.of(
                domain.getSellerIdValue(),
                domain.getMustItSellerNameValue(),
                domain.getSellerNameValue(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt());
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
     * <p><strong>변환 규칙:</strong>
     *
     * <ul>
     *   <li>ID: Entity.id → Domain.SellerId
     *   <li>MustItSellerName: Entity.mustItSellerName → Domain.MustItSellerName
     *   <li>SellerName: Entity.sellerName → Domain.SellerName
     *   <li>Status: Entity.status → Domain.SellerStatus
     *   <li>CreatedAt/UpdatedAt: Entity → Domain
     * </ul>
     *
     * @param entity SellerJpaEntity
     * @return Seller 도메인
     */
    public Seller toDomain(SellerJpaEntity entity) {
        return Seller.reconstitute(
                SellerId.of(entity.getId()),
                MustItSellerName.of(entity.getMustItSellerName()),
                SellerName.of(entity.getSellerName()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                clockHolder.clock());
    }
}
