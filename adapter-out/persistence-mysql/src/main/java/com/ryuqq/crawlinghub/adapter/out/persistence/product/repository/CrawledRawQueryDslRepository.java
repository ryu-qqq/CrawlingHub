package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QCrawledRawJpaEntity.crawledRawJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import com.ryuqq.crawlinghub.domain.product.vo.CrawlType;
import com.ryuqq.crawlinghub.domain.product.vo.RawDataStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawledRawQueryDslRepository - CrawledRaw QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class CrawledRawQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CrawledRawQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id CrawledRaw ID
     * @return Entity (Optional)
     */
    public Optional<CrawledRawJpaEntity> findById(long id) {
        CrawledRawJpaEntity entity =
                queryFactory
                        .selectFrom(crawledRawJpaEntity)
                        .where(crawledRawJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * 상태와 타입으로 조회 (가공 스케줄러용)
     *
     * @param status 상태
     * @param crawlType 크롤링 타입
     * @param limit 최대 조회 건수
     * @return Entity 목록
     */
    public List<CrawledRawJpaEntity> findByStatusAndType(
            RawDataStatus status, CrawlType crawlType, int limit) {
        return queryFactory
                .selectFrom(crawledRawJpaEntity)
                .where(
                        crawledRawJpaEntity.status.eq(status),
                        crawledRawJpaEntity.crawlType.eq(crawlType))
                .orderBy(crawledRawJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * seller_id와 item_no로 특정 타입 조회
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @param crawlType 크롤링 타입
     * @return Entity (Optional)
     */
    public Optional<CrawledRawJpaEntity> findBySellerIdAndItemNoAndType(
            long sellerId, long itemNo, CrawlType crawlType) {
        CrawledRawJpaEntity entity =
                queryFactory
                        .selectFrom(crawledRawJpaEntity)
                        .where(
                                crawledRawJpaEntity.sellerId.eq(sellerId),
                                crawledRawJpaEntity.itemNo.eq(itemNo),
                                crawledRawJpaEntity.crawlType.eq(crawlType))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }
}
