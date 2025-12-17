package com.ryuqq.crawlinghub.adapter.out.persistence.image.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.QCrawledProductImageJpaEntity.crawledProductImageJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.image.entity.CrawledProductImageJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.ImageType;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawledProductImageQueryDslRepository - 이미지 QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class CrawledProductImageQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CrawledProductImageQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id 이미지 ID
     * @return Entity (Optional)
     */
    public Optional<CrawledProductImageJpaEntity> findById(Long id) {
        CrawledProductImageJpaEntity entity =
                queryFactory
                        .selectFrom(crawledProductImageJpaEntity)
                        .where(crawledProductImageJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * CrawledProduct ID로 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<CrawledProductImageJpaEntity> findByCrawledProductId(Long crawledProductId) {
        return queryFactory
                .selectFrom(crawledProductImageJpaEntity)
                .where(crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId))
                .orderBy(
                        crawledProductImageJpaEntity.imageType.asc(),
                        crawledProductImageJpaEntity.displayOrder.asc())
                .fetch();
    }

    /**
     * CrawledProduct ID와 이미지 타입으로 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param imageType 이미지 타입
     * @return Entity 목록
     */
    public List<CrawledProductImageJpaEntity> findByCrawledProductIdAndImageType(
            Long crawledProductId, ImageType imageType) {
        return queryFactory
                .selectFrom(crawledProductImageJpaEntity)
                .where(
                        crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId),
                        crawledProductImageJpaEntity.imageType.eq(imageType))
                .orderBy(crawledProductImageJpaEntity.displayOrder.asc())
                .fetch();
    }

    /**
     * CrawledProduct ID와 원본 URL로 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @param originalUrl 원본 URL
     * @return Entity (Optional)
     */
    public Optional<CrawledProductImageJpaEntity> findByCrawledProductIdAndOriginalUrl(
            Long crawledProductId, String originalUrl) {
        CrawledProductImageJpaEntity entity =
                queryFactory
                        .selectFrom(crawledProductImageJpaEntity)
                        .where(
                                crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId),
                                crawledProductImageJpaEntity.originalUrl.eq(originalUrl))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 업로드되지 않은 이미지 조회 (s3Url이 null)
     *
     * @param crawledProductId CrawledProduct ID
     * @return Entity 목록
     */
    public List<CrawledProductImageJpaEntity> findPendingUpload(Long crawledProductId) {
        return queryFactory
                .selectFrom(crawledProductImageJpaEntity)
                .where(
                        crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId),
                        crawledProductImageJpaEntity.s3Url.isNull())
                .orderBy(crawledProductImageJpaEntity.displayOrder.asc())
                .fetch();
    }

    /**
     * 이미 존재하는 원본 URL 목록 조회 (중복 체크용)
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
                .select(crawledProductImageJpaEntity.originalUrl)
                .from(crawledProductImageJpaEntity)
                .where(
                        crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId),
                        crawledProductImageJpaEntity.originalUrl.in(originalUrls))
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
                        .from(crawledProductImageJpaEntity)
                        .where(
                                crawledProductImageJpaEntity.crawledProductId.eq(crawledProductId),
                                crawledProductImageJpaEntity.originalUrl.eq(originalUrl))
                        .fetchFirst();
        return result != null;
    }
}
