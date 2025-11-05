package com.ryuqq.crawlinghub.adapter.out.persistence.seller.adapter;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QMustitSellerEntity;
import com.ryuqq.crawlinghub.application.seller.dto.query.SellerQueryDto;
import com.ryuqq.crawlinghub.application.seller.port.out.LoadSellerPort;
import com.ryuqq.crawlinghub.domain.seller.MustitSellerId;
import com.ryuqq.crawlinghub.domain.seller.SellerStatus;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seller Query Adapter - CQRS Query Adapter (읽기 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Query 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Read 작업 전용 (findById, findByCode)</li>
 *   <li>✅ QueryDSL DTO Projection으로 직접 조회 (Domain Model 거치지 않음)</li>
 *   <li>✅ N+1 문제 방지</li>
 *   <li>✅ 인덱스 활용 최적화</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Write 작업은 SellerCommandAdapter에서 처리</li>
 *   <li>❌ Command 작업은 이 Adapter에서 금지</li>
 *   <li>❌ Domain Model 변환 없이 DTO 직접 반환</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Component
public class SellerQueryAdapter implements LoadSellerPort {

    private final JPAQueryFactory queryFactory;
    private static final QMustitSellerEntity seller = QMustitSellerEntity.mustitSellerEntity;

    /**
     * Adapter 생성자
     *
     * @param queryFactory QueryDSL QueryFactory
     */
    public SellerQueryAdapter(JPAQueryFactory queryFactory) {
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
    @Override
    @Transactional(readOnly = true)
    public Optional<SellerQueryDto> findById(MustitSellerId id) {
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
            .where(seller.id.eq(id.value()))
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
    @Override
    @Transactional(readOnly = true)
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
}

