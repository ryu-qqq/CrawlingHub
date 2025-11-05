package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.ProductCountHistoryEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QProductCountHistoryEntity;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductCountHistoryQueryRepository - Query Repository (QueryDSL)
 *
 * <p><strong>QueryDSL 기반 읽기 전용 Repository ⭐</strong></p>
 * <ul>
 *   <li>N+1 문제 방지</li>
 *   <li>DTO Projection 최적화</li>
 *   <li>타입 안전한 쿼리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Repository
public class ProductCountHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProductCountHistoryQueryRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * 셀러별 상품 수 이력 조회 (페이징)
     *
     * <p>QueryDSL 기반 최적화 쿼리 ⭐</p>
     *
     * @param sellerId 셀러 ID
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return ProductCountHistoryEntity 리스트
     */
    public List<ProductCountHistoryEntity> findHistoriesBySellerId(Long sellerId, int offset, int limit) {
        QProductCountHistoryEntity history = QProductCountHistoryEntity.productCountHistoryEntity;

        return queryFactory
            .selectFrom(history)
            .where(history.sellerId.eq(sellerId))
            .orderBy(history.executedDate.desc())
            .offset(offset)
            .limit(limit)
            .fetch();
    }

    /**
     * 전체 이력 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 개수
     */
    public long countHistoriesBySellerId(Long sellerId) {
        QProductCountHistoryEntity history = QProductCountHistoryEntity.productCountHistoryEntity;

        return queryFactory
            .selectFrom(history)
            .where(history.sellerId.eq(sellerId))
            .fetchCount();
    }
}

