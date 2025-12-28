package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import static com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QCrawledProductJpaEntity.crawledProductJpaEntity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledProductJpaEntity;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
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
                .where(crawledProductJpaEntity.sellerId.eq(sellerId))
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
                        crawledProductJpaEntity.optionCrawledAt.isNotNull())
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
                                crawledProductJpaEntity.itemNo.eq(itemNo))
                        .fetchFirst();
        return result != null;
    }

    /**
     * 검색 조건에 따른 CrawledProduct 목록 조회 (페이징)
     *
     * @param query 검색 조건
     * @return Entity 목록
     */
    public List<CrawledProductJpaEntity> search(SearchCrawledProductsQuery query) {
        BooleanBuilder builder = buildSearchCondition(query);

        return queryFactory
                .selectFrom(crawledProductJpaEntity)
                .where(builder)
                .orderBy(crawledProductJpaEntity.createdAt.desc())
                .offset(query.getOffset())
                .limit(query.size())
                .fetch();
    }

    /**
     * 검색 조건에 따른 CrawledProduct 개수 조회
     *
     * @param query 검색 조건
     * @return 총 개수
     */
    public long count(SearchCrawledProductsQuery query) {
        BooleanBuilder builder = buildSearchCondition(query);

        Long count =
                queryFactory
                        .select(crawledProductJpaEntity.count())
                        .from(crawledProductJpaEntity)
                        .where(builder)
                        .fetchOne();

        return count != null ? count : 0L;
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
                        .where(crawledProductJpaEntity.sellerId.eq(sellerId))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 검색 조건 BooleanBuilder 생성
     *
     * @param query 검색 조건
     * @return BooleanBuilder
     */
    private BooleanBuilder buildSearchCondition(SearchCrawledProductsQuery query) {
        BooleanBuilder builder = new BooleanBuilder();

        // sellerId 정확히 일치
        if (query.sellerId() != null) {
            builder.and(crawledProductJpaEntity.sellerId.eq(query.sellerId()));
        }

        // itemNo 정확히 일치
        if (query.itemNo() != null) {
            builder.and(crawledProductJpaEntity.itemNo.eq(query.itemNo()));
        }

        // itemName 부분 일치 (LIKE)
        if (query.itemName() != null && !query.itemName().isBlank()) {
            builder.and(crawledProductJpaEntity.itemName.containsIgnoreCase(query.itemName()));
        }

        // brandName 부분 일치 (LIKE)
        if (query.brandName() != null && !query.brandName().isBlank()) {
            builder.and(crawledProductJpaEntity.brandName.containsIgnoreCase(query.brandName()));
        }

        // needsSync 여부
        if (query.needsSync() != null) {
            builder.and(crawledProductJpaEntity.needsSync.eq(query.needsSync()));
        }

        // allCrawled 여부 (MINI_SHOP, DETAIL, OPTION 모두 완료)
        if (query.allCrawled() != null) {
            if (query.allCrawled()) {
                builder.and(crawledProductJpaEntity.miniShopCrawledAt.isNotNull());
                builder.and(crawledProductJpaEntity.detailCrawledAt.isNotNull());
                builder.and(crawledProductJpaEntity.optionCrawledAt.isNotNull());
            } else {
                builder.and(
                        crawledProductJpaEntity
                                .miniShopCrawledAt
                                .isNull()
                                .or(crawledProductJpaEntity.detailCrawledAt.isNull())
                                .or(crawledProductJpaEntity.optionCrawledAt.isNull()));
            }
        }

        // hasExternalId 여부 (외부 상품 ID 존재)
        if (query.hasExternalId() != null) {
            if (query.hasExternalId()) {
                builder.and(crawledProductJpaEntity.externalProductId.isNotNull());
            } else {
                builder.and(crawledProductJpaEntity.externalProductId.isNull());
            }
        }

        return builder;
    }
}
