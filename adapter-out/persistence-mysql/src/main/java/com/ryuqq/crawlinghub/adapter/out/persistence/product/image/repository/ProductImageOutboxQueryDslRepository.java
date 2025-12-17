package com.ryuqq.crawlinghub.adapter.out.persistence.product.image.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.QProductImageOutboxJpaEntity.productImageOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * ProductImageOutboxQueryDslRepository - ImageOutbox QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상태별 조회 (findByStatus)
 *   <li>재시도 가능한 Outbox 조회 (findRetryable)
 *   <li>PENDING 상태 조회 (스케줄러용)
 *   <li>중복 체크 (existsByCrawledProductIdAndOriginalUrl)
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
public class ProductImageOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public ProductImageOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id Outbox ID
     * @return Entity (Optional)
     */
    public Optional<ProductImageOutboxJpaEntity> findById(Long id) {
        ProductImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productImageOutboxJpaEntity)
                        .where(productImageOutboxJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 멱등성 키로 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return Entity (Optional)
     */
    public Optional<ProductImageOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        ProductImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productImageOutboxJpaEntity)
                        .where(productImageOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID로 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findByCrawledProductId(Long crawledProductId) {
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(productImageOutboxJpaEntity.crawledProductId.eq(crawledProductId))
                .orderBy(productImageOutboxJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 상태로 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findByStatus(ProductOutboxStatus status, int limit) {
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(productImageOutboxJpaEntity.status.eq(status))
                .orderBy(productImageOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * PENDING 상태 Outbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findPendingOutboxes(int limit) {
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(productImageOutboxJpaEntity.status.eq(ProductOutboxStatus.PENDING))
                .orderBy(productImageOutboxJpaEntity.createdAt.asc())
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
    public List<ProductImageOutboxJpaEntity> findRetryableOutboxes(int maxRetryCount, int limit) {
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(
                        productImageOutboxJpaEntity.status.eq(ProductOutboxStatus.FAILED),
                        productImageOutboxJpaEntity.retryCount.lt(maxRetryCount))
                .orderBy(productImageOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 원본 URL로 이미 존재하는지 확인
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return 존재하면 true
     */
    public boolean existsByCrawledProductIdAndOriginalUrl(
            Long crawledProductId, String originalUrl) {
        Integer result =
                queryFactory
                        .selectOne()
                        .from(productImageOutboxJpaEntity)
                        .where(
                                productImageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                                productImageOutboxJpaEntity.originalUrl.eq(originalUrl))
                        .fetchFirst();
        return result != null;
    }

    /**
     * 이미 존재하는 원본 URL 목록 조회 (IN 절 배치 쿼리)
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 확인할 원본 URL 목록
     * @return 이미 존재하는 원본 URL 목록
     */
    public List<String> findExistingOriginalUrls(Long crawledProductId, List<String> originalUrls) {
        if (originalUrls == null || originalUrls.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .select(productImageOutboxJpaEntity.originalUrl)
                .from(productImageOutboxJpaEntity)
                .where(
                        productImageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                        productImageOutboxJpaEntity.originalUrl.in(originalUrls))
                .fetch();
    }

    /**
     * CrawledProduct ID와 원본 URL로 단건 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return Entity (Optional)
     */
    public Optional<ProductImageOutboxJpaEntity> findByCrawledProductIdAndOriginalUrl(
            Long crawledProductId, String originalUrl) {
        ProductImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productImageOutboxJpaEntity)
                        .where(
                                productImageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                                productImageOutboxJpaEntity.originalUrl.eq(originalUrl))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID와 원본 URL 목록으로 배치 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrls 원본 URL 목록
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findByCrawledProductIdAndOriginalUrls(
            Long crawledProductId, List<String> originalUrls) {
        if (originalUrls == null || originalUrls.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(
                        productImageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                        productImageOutboxJpaEntity.originalUrl.in(originalUrls))
                .fetch();
    }
}
