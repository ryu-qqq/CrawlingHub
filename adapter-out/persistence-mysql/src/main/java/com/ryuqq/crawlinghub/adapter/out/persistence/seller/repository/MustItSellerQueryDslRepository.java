package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QMustItSellerEntity;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

/**
 * SellerQueryDslRepository - Query Repository (QueryDSL)
 *
 * <p><strong>QueryDSL 기반 읽기 전용 Repository ⭐</strong></p>
 * <ul>
 *   <li>✅ JPAQueryFactory 캡슐화</li>
 *   <li>✅ DTO Projection 최적화</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ 타입 안전한 쿼리</li>
 * </ul>
 *
 * <p><strong>사용처:</strong></p>
 * <ul>
 *   <li>SellerQueryAdapter에서 주입받아 사용</li>
 *   <li>Query Adapter는 이 Repository를 통해 조회</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@Repository
public class MustItSellerQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QMustItSellerEntity seller = QMustItSellerEntity.mustItSellerEntity;

    public MustItSellerQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = Objects.requireNonNull(queryFactory, "queryFactory must not be null");
    }

    /**
     * ID로 셀러 조회
     *
     * <p>QueryDSL DTO Projection으로 직접 조회하여 Domain Model을 거치지 않습니다.</p>
     *
     * @param id 셀러 ID (null 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException id가 null인 경우
     */
    public Optional<SellerQueryDto> findById(Long id) {
        Objects.requireNonNull(id, "id must not be null");

        SellerQueryDto result = queryFactory
            .select(Projections.constructor(
                SellerQueryDto.class,
                seller.id,
                seller.sellerCode,
                seller.sellerName,
                seller.status,
                seller.totalProductCount,
                seller.lastCrawledAt,
                seller.createdAt,
                seller.updatedAt
            ))
            .from(seller)
            .where(seller.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 셀러 코드로 셀러 조회
     *
     * <p>인덱스(idx_seller_id)를 활용한 최적화 쿼리로 QueryDSL DTO Projection으로 직접 조회합니다.</p>
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 셀러 Query DTO (없으면 Optional.empty())
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    public Optional<SellerQueryDto> findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }

        SellerQueryDto result = queryFactory
            .select(Projections.constructor(
                SellerQueryDto.class,
                seller.id,
                seller.sellerCode,
                seller.sellerName,
                seller.status,
                seller.totalProductCount,
                seller.lastCrawledAt,
                seller.createdAt,
                seller.updatedAt
            ))
            .from(seller)
            .where(seller.sellerCode.eq(code))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 셀러 코드 존재 여부 확인
     *
     * <p>QueryDSL selectOne() + fetchFirst()로 EXISTS 쿼리를 최적화합니다.</p>
     * <p>COUNT 쿼리 대신 EXISTS를 사용하여 성능을 최적화합니다.</p>
     *
     * @param code 셀러 코드 (null, blank 불가)
     * @return 존재 여부
     * @throws IllegalArgumentException code가 null 또는 blank인 경우
     */
    public boolean existsByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("code must not be null or blank");
        }

        Integer count = queryFactory
            .selectOne()
            .from(seller)
            .where(seller.sellerCode.eq(code))
            .fetchFirst();

        return count != null;
    }
}
