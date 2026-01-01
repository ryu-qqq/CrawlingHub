package com.ryuqq.crawlinghub.adapter.out.persistence.image.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.QCrawledProductImageJpaEntity.crawledProductImageJpaEntity;
import static com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.QProductImageOutboxJpaEntity.productImageOutboxJpaEntity;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.dto.ProductImageOutboxWithImageDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.ProductImageOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.ProductImageOutboxCriteria;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * ProductImageOutboxQueryDslRepository - Outbox QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
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
     * 이미지 ID로 조회
     *
     * @param crawledProductImageId 이미지 ID
     * @return Entity (Optional)
     */
    public Optional<ProductImageOutboxJpaEntity> findByCrawledProductImageId(
            Long crawledProductImageId) {
        ProductImageOutboxJpaEntity entity =
                queryFactory
                        .selectFrom(productImageOutboxJpaEntity)
                        .where(
                                productImageOutboxJpaEntity.crawledProductImageId.eq(
                                        crawledProductImageId))
                        .fetchOne();
        return Optional.ofNullable(entity);
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
     * Criteria 기반 Outbox 조회 (SQS 스케줄러용)
     *
     * <p>ProductImageOutboxCriteria VO를 사용하여 유연한 조건 조회를 지원합니다.
     *
     * @param criteria 조회 조건 VO
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findByCriteria(ProductImageOutboxCriteria criteria) {
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(buildCriteriaConditions(criteria))
                .orderBy(productImageOutboxJpaEntity.createdAt.asc())
                .offset(criteria.offset())
                .limit(criteria.limit())
                .fetch();
    }

    /**
     * Criteria 기반 Outbox 개수 조회
     *
     * @param criteria 조회 조건 VO
     * @return 총 개수
     */
    public long countByCriteria(ProductImageOutboxCriteria criteria) {
        Long count =
                queryFactory
                        .select(productImageOutboxJpaEntity.count())
                        .from(productImageOutboxJpaEntity)
                        .where(buildCriteriaConditions(criteria))
                        .fetchOne();
        return count != null ? count : 0L;
    }

    /**
     * Criteria에서 BooleanExpression 배열 생성
     *
     * @param criteria 조회 조건 VO
     * @return BooleanExpression 배열
     */
    private BooleanExpression[] buildCriteriaConditions(ProductImageOutboxCriteria criteria) {
        return new BooleanExpression[] {
            criteriaEqStatus(criteria),
            criteriaInStatuses(criteria),
            criteriaGoeCreatedAt(criteria),
            criteriaLoeCreatedAt(criteria),
            criteriaLtRetryCount(criteria)
        };
    }

    private BooleanExpression criteriaEqStatus(ProductImageOutboxCriteria criteria) {
        return criteria.hasSingleStatusFilter()
                ? productImageOutboxJpaEntity.status.eq(criteria.status())
                : null;
    }

    private BooleanExpression criteriaInStatuses(ProductImageOutboxCriteria criteria) {
        return criteria.hasMultipleStatusFilter()
                ? productImageOutboxJpaEntity.status.in(criteria.statuses())
                : null;
    }

    private BooleanExpression criteriaGoeCreatedAt(ProductImageOutboxCriteria criteria) {
        if (!criteria.hasCreatedFromFilter()) {
            return null;
        }
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(criteria.createdFrom(), ZoneId.systemDefault());
        return productImageOutboxJpaEntity.createdAt.goe(localDateTime);
    }

    private BooleanExpression criteriaLoeCreatedAt(ProductImageOutboxCriteria criteria) {
        if (!criteria.hasCreatedToFilter()) {
            return null;
        }
        LocalDateTime localDateTime =
                LocalDateTime.ofInstant(criteria.createdTo(), ZoneId.systemDefault());
        return productImageOutboxJpaEntity.createdAt.loe(localDateTime);
    }

    private BooleanExpression criteriaLtRetryCount(ProductImageOutboxCriteria criteria) {
        return criteria.hasMaxRetryCountFilter()
                ? productImageOutboxJpaEntity.retryCount.lt(criteria.maxRetryCount())
                : null;
    }

    /**
     * 이미지 ID 목록으로 Outbox 조회
     *
     * @param imageIds 이미지 ID 목록
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findByImageIds(List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return List.of();
        }
        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(productImageOutboxJpaEntity.crawledProductImageId.in(imageIds))
                .fetch();
    }

    /**
     * 이미지 ID로 이미 존재하는지 확인
     *
     * @param crawledProductImageId 이미지 ID
     * @return 존재하면 true
     */
    public boolean existsByCrawledProductImageId(Long crawledProductImageId) {
        Integer result =
                queryFactory
                        .selectOne()
                        .from(productImageOutboxJpaEntity)
                        .where(
                                productImageOutboxJpaEntity.crawledProductImageId.eq(
                                        crawledProductImageId))
                        .fetchFirst();
        return result != null;
    }

    /**
     * 조건으로 ImageOutbox 목록 검색 (페이징)
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> search(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        var query = queryFactory.selectFrom(productImageOutboxJpaEntity);

        // crawledProductId 필터 시 JOIN 필요
        if (crawledProductId != null) {
            query =
                    query.innerJoin(crawledProductImageJpaEntity)
                            .on(
                                    productImageOutboxJpaEntity.crawledProductImageId.eq(
                                            crawledProductImageJpaEntity.id));
        }

        return query.where(
                        eqCrawledProductImageId(crawledProductImageId),
                        eqCrawledProductId(crawledProductId),
                        inStatuses(statuses),
                        goeCreatedAt(createdFrom),
                        loeCreatedAt(createdTo))
                .orderBy(productImageOutboxJpaEntity.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    /**
     * 조건으로 ImageOutbox 개수 조회
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @return 총 개수
     */
    public long count(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo) {
        var query =
                queryFactory
                        .select(productImageOutboxJpaEntity.count())
                        .from(productImageOutboxJpaEntity);

        // crawledProductId 필터 시 JOIN 필요
        if (crawledProductId != null) {
            query =
                    query.innerJoin(crawledProductImageJpaEntity)
                            .on(
                                    productImageOutboxJpaEntity.crawledProductImageId.eq(
                                            crawledProductImageJpaEntity.id));
        }

        Long result =
                query.where(
                                eqCrawledProductImageId(crawledProductImageId),
                                eqCrawledProductId(crawledProductId),
                                inStatuses(statuses),
                                goeCreatedAt(createdFrom),
                                loeCreatedAt(createdTo))
                        .fetchOne();
        return result != null ? result : 0L;
    }

    private BooleanExpression eqCrawledProductImageId(Long crawledProductImageId) {
        return crawledProductImageId != null
                ? productImageOutboxJpaEntity.crawledProductImageId.eq(crawledProductImageId)
                : null;
    }

    private BooleanExpression eqCrawledProductId(Long crawledProductId) {
        return crawledProductId != null
                ? crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId)
                : null;
    }

    private BooleanExpression inStatuses(List<ProductOutboxStatus> statuses) {
        return statuses != null && !statuses.isEmpty()
                ? productImageOutboxJpaEntity.status.in(statuses)
                : null;
    }

    private BooleanExpression goeCreatedAt(Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdFrom, ZoneId.systemDefault());
        return productImageOutboxJpaEntity.createdAt.goe(localDateTime);
    }

    private BooleanExpression loeCreatedAt(Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdTo, ZoneId.systemDefault());
        return productImageOutboxJpaEntity.createdAt.loe(localDateTime);
    }

    /**
     * PROCESSING 상태이고 타임아웃된 Outbox 조회
     *
     * <p>processedAt 기준으로 지정된 시간(초)이 지난 PROCESSING 상태의 Outbox를 조회합니다.
     *
     * @param timeoutSeconds 타임아웃 기준 시간(초)
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<ProductImageOutboxJpaEntity> findTimedOutProcessingOutboxes(
            int timeoutSeconds, int limit) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(timeoutSeconds);

        return queryFactory
                .selectFrom(productImageOutboxJpaEntity)
                .where(
                        productImageOutboxJpaEntity.status.eq(ProductOutboxStatus.PROCESSING),
                        productImageOutboxJpaEntity.processedAt.lt(cutoffTime))
                .orderBy(productImageOutboxJpaEntity.processedAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 조건으로 ImageOutbox 목록 검색 (이미지 정보 포함, 페이징)
     *
     * <p>CrawledProductImage와 LEFT JOIN하여 이미지 정보를 함께 반환합니다.
     *
     * @param crawledProductImageId CrawledProductImage ID (nullable)
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return Outbox + 이미지 정보 DTO 목록
     */
    public List<ProductImageOutboxWithImageDto> searchWithImageInfo(
            Long crawledProductImageId,
            Long crawledProductId,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size) {
        return queryFactory
                .select(
                        Projections.constructor(
                                ProductImageOutboxWithImageDto.class,
                                productImageOutboxJpaEntity.id,
                                productImageOutboxJpaEntity.crawledProductImageId,
                                productImageOutboxJpaEntity.idempotencyKey,
                                productImageOutboxJpaEntity.status,
                                productImageOutboxJpaEntity.retryCount,
                                productImageOutboxJpaEntity.errorMessage,
                                productImageOutboxJpaEntity.createdAt,
                                productImageOutboxJpaEntity.processedAt,
                                crawledProductImageJpaEntity.crawledProductId,
                                crawledProductImageJpaEntity.originalUrl,
                                crawledProductImageJpaEntity.s3Url,
                                crawledProductImageJpaEntity.imageType))
                .from(productImageOutboxJpaEntity)
                .leftJoin(crawledProductImageJpaEntity)
                .on(
                        productImageOutboxJpaEntity.crawledProductImageId.eq(
                                crawledProductImageJpaEntity.id))
                .where(
                        eqCrawledProductImageId(crawledProductImageId),
                        eqCrawledProductId(crawledProductId),
                        inStatuses(statuses),
                        goeCreatedAt(createdFrom),
                        loeCreatedAt(createdTo))
                .orderBy(productImageOutboxJpaEntity.createdAt.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }
}
