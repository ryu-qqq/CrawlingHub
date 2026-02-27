package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QCrawledProductJpaEntity.crawledProductJpaEntity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawledProductQueryDslRepository - CrawledProduct QueryDSL Repository
 *
 * <p>복잡한 조회 쿼리를 QueryDSL로 처리합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>ID, SellerId+ItemNo 기반 단건 조회
 *   <li>SellerId 기반 목록 조회
 *   <li>동기화 필요 상품 조회
 *   <li>존재 여부 확인
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
public class CrawledProductQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public CrawledProductQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 단건 조회
     *
     * @param id CrawledProduct ID
     * @return Entity (Optional)
     */
    public Optional<CrawledProductJpaEntity> findById(Long id) {
        CrawledProductJpaEntity entity =
                queryFactory
                        .selectFrom(crawledProductJpaEntity)
                        .where(crawledProductJpaEntity.id.eq(id))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * SellerId와 ItemNo로 단건 조회
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return Entity (Optional)
     */
    public Optional<CrawledProductJpaEntity> findBySellerIdAndItemNo(long sellerId, long itemNo) {
        CrawledProductJpaEntity entity =
                queryFactory
                        .selectFrom(crawledProductJpaEntity)
                        .where(
                                crawledProductJpaEntity.sellerId.eq(sellerId),
                                crawledProductJpaEntity.itemNo.eq(itemNo),
                                crawledProductJpaEntity.deletedAt.isNull())
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * SellerId와 ItemNo로 단건 조회 (soft-delete 포함)
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return Entity (Optional, soft-deleted 포함)
     */
    public Optional<CrawledProductJpaEntity> findBySellerIdAndItemNoIncludingDeleted(
            long sellerId, long itemNo) {
        CrawledProductJpaEntity entity =
                queryFactory
                        .selectFrom(crawledProductJpaEntity)
                        .where(
                                crawledProductJpaEntity.sellerId.eq(sellerId),
                                crawledProductJpaEntity.itemNo.eq(itemNo))
                        .fetchOne();
        return Optional.ofNullable(entity);
    }

    /**
     * SellerId로 목록 조회
     *
     * @param sellerId 판매자 ID
     * @return Entity 목록
     */
    public List<CrawledProductJpaEntity> findBySellerId(long sellerId) {
        return queryFactory
                .selectFrom(crawledProductJpaEntity)
                .where(
                        crawledProductJpaEntity.sellerId.eq(sellerId),
                        crawledProductJpaEntity.deletedAt.isNull())
                .orderBy(crawledProductJpaEntity.createdAt.desc())
                .fetch();
    }

    /**
     * 동기화 필요 상품 조회 (needsSync=true, 모든 크롤링 완료)
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<CrawledProductJpaEntity> findNeedsSyncProducts(int limit) {
        return queryFactory
                .selectFrom(crawledProductJpaEntity)
                .where(
                        crawledProductJpaEntity.needsSync.isTrue(),
                        crawledProductJpaEntity.miniShopCrawledAt.isNotNull(),
                        crawledProductJpaEntity.detailCrawledAt.isNotNull(),
                        crawledProductJpaEntity.optionCrawledAt.isNotNull(),
                        crawledProductJpaEntity.deletedAt.isNull())
                .orderBy(crawledProductJpaEntity.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * SellerId와 ItemNo로 존재 여부 확인
     *
     * @param sellerId 판매자 ID
     * @param itemNo 상품 번호
     * @return 존재하면 true
     */
    public boolean existsBySellerIdAndItemNo(long sellerId, long itemNo) {
        Integer result =
                queryFactory
                        .selectOne()
                        .from(crawledProductJpaEntity)
                        .where(
                                crawledProductJpaEntity.sellerId.eq(sellerId),
                                crawledProductJpaEntity.itemNo.eq(itemNo),
                                crawledProductJpaEntity.deletedAt.isNull())
                        .fetchFirst();
        return result != null;
    }

    /**
     * 갱신이 오래된 상품 조회 (updatedAt ASC)
     *
     * <p>externalProductId가 존재하고 soft-delete되지 않은 상품 중 updatedAt이 가장 오래된 순으로 조회
     *
     * @param limit 조회 개수 제한
     * @return Entity 목록
     */
    public List<CrawledProductJpaEntity> findStaleProducts(int limit) {
        return queryFactory
                .selectFrom(crawledProductJpaEntity)
                .where(
                        crawledProductJpaEntity.deletedAt.isNull(),
                        crawledProductJpaEntity.externalProductId.isNotNull())
                .orderBy(crawledProductJpaEntity.updatedAt.asc())
                .limit(limit)
                .fetch();
    }

    /**
     * 셀러별 CrawledProduct 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 해당 셀러의 상품 개수
     */
    public long countBySellerId(long sellerId) {
        Long count =
                queryFactory
                        .select(crawledProductJpaEntity.count())
                        .from(crawledProductJpaEntity)
                        .where(
                                crawledProductJpaEntity.sellerId.eq(sellerId),
                                crawledProductJpaEntity.deletedAt.isNull())
                        .fetchOne();

        return count != null ? count : 0L;
    }
}
