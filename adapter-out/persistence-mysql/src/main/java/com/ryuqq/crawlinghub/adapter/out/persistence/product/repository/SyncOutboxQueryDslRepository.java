package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QSyncOutboxJpaEntity.syncOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity.OutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * SyncOutboxQueryDslRepository - SyncOutbox QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상태별 조회
 *   <li>재시도 대상 조회
 *   <li>멱등성 키 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class SyncOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public SyncOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id Outbox ID
     * @return Entity (Optional)
     */
    public Optional<SyncOutboxJpaEntity> findById(Long id) {
        SyncOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(syncOutboxJpaEntity)
                        .where(syncOutboxJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 멱등성 키로 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return Entity (Optional)
     */
    public Optional<SyncOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        SyncOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(syncOutboxJpaEntity)
                        .where(syncOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID로 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<SyncOutboxJpaEntity> findByCrawledProductId(Long crawledProductId) {
        return queryFactory
                .selectFrom(syncOutboxJpaEntity)
                .where(syncOutboxJpaEntity.crawledProductId.eq(crawledProductId))
                .orderBy(syncOutboxJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 상태별 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<SyncOutboxJpaEntity> findByStatus(OutboxStatus status, int limit) {
        return queryFactory
                .selectFrom(syncOutboxJpaEntity)
                .where(syncOutboxJpaEntity.status.eq(status))
                .orderBy(syncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * PENDING 상태 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<SyncOutboxJpaEntity> findPendingOutboxes(int limit) {
        return queryFactory
                .selectFrom(syncOutboxJpaEntity)
                .where(syncOutboxJpaEntity.status.eq(OutboxStatus.PENDING))
                .orderBy(syncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * FAILED 상태이고 재시도 가능한 Outbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<SyncOutboxJpaEntity> findRetryableOutboxes(int maxRetryCount, int limit) {
        return queryFactory
                .selectFrom(syncOutboxJpaEntity)
                .where(
                        syncOutboxJpaEntity.status.eq(OutboxStatus.FAILED),
                        syncOutboxJpaEntity.retryCount.lt(maxRetryCount))
                .orderBy(syncOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }
}
