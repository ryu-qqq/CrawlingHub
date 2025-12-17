package com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.entity.QProductSyncOutboxJpaEntity.productSyncOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * ProductSyncOutboxQueryDslRepository - SyncOutbox QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상태별 조회 (findByStatus)
 *   <li>재시도 가능한 Outbox 조회 (findRetryable)
 *   <li>PENDING 상태 조회 (스케줄러용)
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>비즈니스 로직 포함 금지
 *   <li>저장/수정 로직 금지 (JpaRepository에서 처리)
 *   <li>Mapper 호출 금지 (Adapter에서 처리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class ProductSyncOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public ProductSyncOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id Outbox ID
     * @return Entity (Optional)
     */
    public Optional<ProductSyncOutboxJpaEntity> findById(Long id) {
        ProductSyncOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productSyncOutboxJpaEntity)
                        .where(productSyncOutboxJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 멱등성 키로 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return Entity (Optional)
     */
    public Optional<ProductSyncOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        ProductSyncOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productSyncOutboxJpaEntity)
                        .where(productSyncOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID로 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<ProductSyncOutboxJpaEntity> findByCrawledProductId(Long crawledProductId) {
        return queryFactory
                .selectFrom(productSyncOutboxJpaEntity)
                .where(productSyncOutboxJpaEntity.crawledProductId.eq(crawledProductId))
                .orderBy(productSyncOutboxJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 상태로 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductSyncOutboxJpaEntity> findByStatus(ProductOutboxStatus status, int limit) {
        return queryFactory
                .selectFrom(productSyncOutboxJpaEntity)
                .where(productSyncOutboxJpaEntity.status.eq(status))
                .orderBy(productSyncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * PENDING 상태 Outbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductSyncOutboxJpaEntity> findPendingOutboxes(int limit) {
        return queryFactory
                .selectFrom(productSyncOutboxJpaEntity)
                .where(productSyncOutboxJpaEntity.status.eq(ProductOutboxStatus.PENDING))
                .orderBy(productSyncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 재시도 가능한 Outbox 조회 (FAILED 상태 + 재시도 횟수 미달)
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductSyncOutboxJpaEntity> findRetryableOutboxes(int maxRetryCount, int limit) {
        return queryFactory
                .selectFrom(productSyncOutboxJpaEntity)
                .where(
                        productSyncOutboxJpaEntity.status.eq(ProductOutboxStatus.FAILED),
                        productSyncOutboxJpaEntity.retryCount.lt(maxRetryCount))
                .orderBy(productSyncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
