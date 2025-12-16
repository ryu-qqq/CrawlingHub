package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QImageOutboxJpaEntity.imageOutboxJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.ImageOutboxJpaEntity.OutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * ImageOutboxQueryDslRepository - ImageOutbox QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>상태별 조회
 *   <li>재시도 대상 조회
 *   <li>멱등성 키 조회
 *   <li>원본 URL 기반 조회
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class ImageOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public ImageOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id Outbox ID
     * @return Entity (Optional)
     */
    public Optional<ImageOutboxJpaEntity> findById(Long id) {
        ImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(imageOutboxJpaEntity)
                        .where(imageOutboxJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 멱등성 키로 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return Entity (Optional)
     */
    public Optional<ImageOutboxJpaEntity> findByIdempotencyKey(String idempotencyKey) {
        ImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(imageOutboxJpaEntity)
                        .where(imageOutboxJpaEntity.idempotencyKey.eq(idempotencyKey))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID로 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<ImageOutboxJpaEntity> findByCrawledProductId(Long crawledProductId) {
        return queryFactory
                .selectFrom(imageOutboxJpaEntity)
                .where(imageOutboxJpaEntity.crawledProductId.eq(crawledProductId))
                .orderBy(imageOutboxJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 상태별 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ImageOutboxJpaEntity> findByStatus(OutboxStatus status, int limit) {
        return queryFactory
                .selectFrom(imageOutboxJpaEntity)
                .where(imageOutboxJpaEntity.status.eq(status))
                .orderBy(imageOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * PENDING 상태 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ImageOutboxJpaEntity> findPendingOutboxes(int limit) {
        return queryFactory
                .selectFrom(imageOutboxJpaEntity)
                .where(imageOutboxJpaEntity.status.eq(OutboxStatus.PENDING))
                .orderBy(imageOutboxJpaEntity.createdAt.asc())
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
    public List<ImageOutboxJpaEntity> findRetryableOutboxes(int maxRetryCount, int limit) {
        return queryFactory
                .selectFrom(imageOutboxJpaEntity)
                .where(
                        imageOutboxJpaEntity.status.eq(OutboxStatus.FAILED),
                        imageOutboxJpaEntity.retryCount.lt(maxRetryCount))
                .orderBy(imageOutboxJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * CrawledProduct ID와 원본 URL로 존재 여부 확인
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
                        .from(imageOutboxJpaEntity)
                        .where(
                                imageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                                imageOutboxJpaEntity.originalUrl.eq(originalUrl))
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
        return queryFactory
                .select(imageOutboxJpaEntity.originalUrl)
                .from(imageOutboxJpaEntity)
                .where(
                        imageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                        imageOutboxJpaEntity.originalUrl.in(originalUrls))
                .fetch();
    }

    /**
     * CrawledProduct ID와 원본 URL로 단건 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return Entity (Optional)
     */
    public Optional<ImageOutboxJpaEntity> findByCrawledProductIdAndOriginalUrl(
            Long crawledProductId, String originalUrl) {
        ImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(imageOutboxJpaEntity)
                        .where(
                                imageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                                imageOutboxJpaEntity.originalUrl.eq(originalUrl))
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
    public List<ImageOutboxJpaEntity> findByCrawledProductIdAndOriginalUrls(
            Long crawledProductId, List<String> originalUrls) {
        return queryFactory
                .selectFrom(imageOutboxJpaEntity)
                .where(
                        imageOutboxJpaEntity.crawledProductId.eq(crawledProductId),
                        imageOutboxJpaEntity.originalUrl.in(originalUrls))
                .fetch();
    }
}
