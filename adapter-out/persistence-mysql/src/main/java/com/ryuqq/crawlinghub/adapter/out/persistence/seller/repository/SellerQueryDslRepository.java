package com.ryuqq.crawlinghub.adapter.out.persistence.seller.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QSellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.SellerJpaEntity;
import com.ryuqq.crawlinghub.domain.seller.query.SellerQueryCriteria;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * SellerQueryDslRepository - Seller QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드 (4개) + 중복 확인 메서드 (4개):</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>existsById(Long id): 존재 여부 확인
 *   <li>findByCriteria(Criteria): 목록 조회 (동적 쿼리)
 *   <li>countByCriteria(Criteria): 개수 조회 (동적 쿼리)
 *   <li>existsByMustItSellerName: 머스트잇 셀러명 중복 확인
 *   <li>existsBySellerName: 셀러명 중복 확인
 *   <li>existsByMustItSellerNameExcludingId: ID 제외 머스트잇 셀러명 중복 확인
 *   <li>existsBySellerNameExcludingId: ID 제외 셀러명 중복 확인
 * </ul>
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>동적 쿼리 구성 (BooleanExpression)
 *   <li>Offset 페이징
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ Join 절대 금지 (fetch join, left join, inner join)
 *   <li>❌ 비즈니스 로직 금지
 *   <li>❌ Mapper 호출 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class SellerQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QSellerJpaEntity qSeller = QSellerJpaEntity.sellerJpaEntity;

    public SellerQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 Seller 단건 조회
     *
     * @param id Seller ID
     * @return SellerJpaEntity (Optional)
     */
    public Optional<SellerJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qSeller).where(qSeller.id.eq(id)).fetchOne());
    }

    /**
     * ID로 Seller 존재 여부 확인
     *
     * @param id Seller ID
     * @return 존재 여부
     */
    public boolean existsById(Long id) {
        Integer count =
                queryFactory.selectOne().from(qSeller).where(qSeller.id.eq(id)).fetchFirst();

        return count != null;
    }

    /**
     * 검색 조건으로 Seller 목록 조회
     *
     * <p>Offset 페이징을 지원합니다.
     *
     * @param criteria 검색 조건 (SellerQueryCriteria)
     * @return SellerJpaEntity 목록
     */
    public List<SellerJpaEntity> findByCriteria(SellerQueryCriteria criteria) {
        var query = queryFactory.selectFrom(qSeller).where(buildSearchConditions(criteria));

        // Offset 페이징
        if (criteria.page() != null && criteria.size() != null) {
            query = query.offset((long) criteria.page() * criteria.size()).limit(criteria.size());
        }

        // 기본 정렬: ID 내림차순
        query = query.orderBy(qSeller.id.desc());

        return query.fetch();
    }

    /**
     * 검색 조건으로 Seller 개수 조회
     *
     * @param criteria 검색 조건 (SellerQueryCriteria)
     * @return Seller 개수
     */
    public long countByCriteria(SellerQueryCriteria criteria) {
        Long count =
                queryFactory
                        .select(qSeller.count())
                        .from(qSeller)
                        .where(buildSearchConditions(criteria))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * MustItSellerName 존재 여부 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @return 존재 여부
     */
    public boolean existsByMustItSellerName(String mustItSellerName) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qSeller)
                        .where(qSeller.mustItSellerName.eq(mustItSellerName))
                        .fetchFirst();

        return count != null;
    }

    /**
     * SellerName 존재 여부 확인
     *
     * @param sellerName 셀러명
     * @return 존재 여부
     */
    public boolean existsBySellerName(String sellerName) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qSeller)
                        .where(qSeller.sellerName.eq(sellerName))
                        .fetchFirst();

        return count != null;
    }

    /**
     * ID를 제외한 MustItSellerName 존재 여부 확인
     *
     * @param mustItSellerName 머스트잇 셀러명
     * @param excludeId 제외할 ID
     * @return 존재 여부
     */
    public boolean existsByMustItSellerNameExcludingId(String mustItSellerName, Long excludeId) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qSeller)
                        .where(
                                qSeller.mustItSellerName.eq(mustItSellerName),
                                qSeller.id.ne(excludeId))
                        .fetchFirst();

        return count != null;
    }

    /**
     * ID를 제외한 SellerName 존재 여부 확인
     *
     * @param sellerName 셀러명
     * @param excludeId 제외할 ID
     * @return 존재 여부
     */
    public boolean existsBySellerNameExcludingId(String sellerName, Long excludeId) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qSeller)
                        .where(qSeller.sellerName.eq(sellerName), qSeller.id.ne(excludeId))
                        .fetchFirst();

        return count != null;
    }

    /**
     * 검색 조건 구성 (Private 헬퍼 메서드)
     *
     * <p>BooleanExpression을 사용하여 동적 쿼리를 구성합니다.
     */
    private BooleanExpression buildSearchConditions(SellerQueryCriteria criteria) {
        BooleanExpression expression = null;

        // 조건 1: 머스트잇 셀러명 (부분 일치 검색)
        if (criteria.mustItSellerName() != null) {
            BooleanExpression mustItSellerNameCondition =
                    qSeller.mustItSellerName.containsIgnoreCase(
                            criteria.mustItSellerName().value());
            expression =
                    expression != null
                            ? expression.and(mustItSellerNameCondition)
                            : mustItSellerNameCondition;
        }

        // 조건 2: 셀러명 (부분 일치 검색)
        if (criteria.sellerName() != null) {
            BooleanExpression sellerNameCondition =
                    qSeller.sellerName.containsIgnoreCase(criteria.sellerName().value());
            expression =
                    expression != null ? expression.and(sellerNameCondition) : sellerNameCondition;
        }

        // 조건 3: 상태 (다중 상태 IN 조건)
        BooleanExpression statusesCondition = statusesIn(criteria.statuses());
        if (statusesCondition != null) {
            expression = expression != null ? expression.and(statusesCondition) : statusesCondition;
        }

        // 조건 4: 생성일 시작
        BooleanExpression createdFromCondition = createdAtGoe(criteria.createdFrom());
        if (createdFromCondition != null) {
            expression =
                    expression != null
                            ? expression.and(createdFromCondition)
                            : createdFromCondition;
        }

        // 조건 5: 생성일 종료
        BooleanExpression createdToCondition = createdAtLoe(criteria.createdTo());
        if (createdToCondition != null) {
            expression =
                    expression != null ? expression.and(createdToCondition) : createdToCondition;
        }

        return expression;
    }

    /**
     * 다중 상태 필터 BooleanExpression
     *
     * @param statuses 상태 목록 (null이거나 빈 리스트면 필터 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression statusesIn(List<SellerStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return qSeller.status.in(statuses);
    }

    /**
     * 생성일 시작 조건
     *
     * @param createdFrom 생성일 시작 (null이면 조건 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression createdAtGoe(Instant createdFrom) {
        if (createdFrom == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(createdFrom);
        return qSeller.createdAt.goe(localDateTime);
    }

    /**
     * 생성일 종료 조건
     *
     * @param createdTo 생성일 종료 (null이면 조건 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression createdAtLoe(Instant createdTo) {
        if (createdTo == null) {
            return null;
        }
        LocalDateTime localDateTime = toLocalDateTime(createdTo);
        return qSeller.createdAt.loe(localDateTime);
    }

    /**
     * Instant → LocalDateTime 변환
     *
     * <p>Domain Layer의 Instant를 Persistence Layer의 LocalDateTime으로 변환
     *
     * @param instant 변환할 Instant
     * @return 서울 시간대 기준 LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
    }
}
