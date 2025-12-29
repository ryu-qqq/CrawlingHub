package com.ryuqq.crawlinghub.adapter.out.persistence.sync.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.QProductSyncOutboxJpaEntity.productSyncOutboxJpaEntity;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.sync.entity.ProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    /**
     * 조건으로 SyncOutbox 목록 검색 (페이징)
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return Entity 목록
     */
    public List<ProductSyncOutboxJpaEntity> search(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        return queryFactory
                .selectFrom(productSyncOutboxJpaEntity)
                .where(
                        eqCrawledProductId(crawledProductId),
                        eqSellerId(sellerId),
                        inItemNos(itemNos),
                        inStatuses(statuses),
                        goeCreatedAt(createdFrom),
                        loeCreatedAt(createdTo))
                .orderBy(productSyncOutboxJpaEntity.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    /**
     * 조건으로 SyncOutbox 개수 조회
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @return 총 개수
     */
    public long count(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo) {
        Long result =
                queryFactory
                        .select(productSyncOutboxJpaEntity.count())
                        .from(productSyncOutboxJpaEntity)
                        .where(
                                eqCrawledProductId(crawledProductId),
                                eqSellerId(sellerId),
                                inItemNos(itemNos),
                                inStatuses(statuses),
                                goeCreatedAt(createdFrom),
                                loeCreatedAt(createdTo))
                        .fetchOne();
        return result != null ? result : 0L;
    }

    private BooleanExpression eqCrawledProductId(Long crawledProductId) {
        return crawledProductId != null
                ? productSyncOutboxJpaEntity.crawledProductId.eq(crawledProductId)
                : null;
    }

    private BooleanExpression eqSellerId(Long sellerId) {
        return sellerId != null ? productSyncOutboxJpaEntity.sellerId.eq(sellerId) : null;
    }

    private BooleanExpression inItemNos(List<Long> itemNos) {
        return itemNos != null && !itemNos.isEmpty()
                ? productSyncOutboxJpaEntity.itemNo.in(itemNos)
                : null;
    }

    private BooleanExpression inStatuses(List<ProductOutboxStatus> statuses) {
        return statuses != null && !statuses.isEmpty()
                ? productSyncOutboxJpaEntity.status.in(statuses)
                : null;
    }

    private BooleanExpression goeCreatedAt(Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdFrom, ZoneId.systemDefault());
        return productSyncOutboxJpaEntity.createdAt.goe(localDateTime);
    }

    private BooleanExpression loeCreatedAt(Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdTo, ZoneId.systemDefault());
        return productSyncOutboxJpaEntity.createdAt.loe(localDateTime);
    }
}
