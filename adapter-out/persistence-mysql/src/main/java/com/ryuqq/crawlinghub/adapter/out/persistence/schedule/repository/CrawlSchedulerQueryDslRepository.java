package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlSchedulerQueryDslRepository - CrawlScheduler QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드:</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>existsById(Long id): 존재 여부 확인
 *   <li>findByCriteria(Criteria): 목록 조회 (동적 쿼리)
 *   <li>countByCriteria(Criteria): 개수 조회 (동적 쿼리)
 *   <li>existsBySellerIdAndSchedulerName: 셀러별 스케줄러 이름 중복 확인
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
public class CrawlSchedulerQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QCrawlSchedulerJpaEntity qScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;

    public CrawlSchedulerQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 CrawlScheduler 단건 조회
     *
     * @param id CrawlScheduler ID
     * @return CrawlSchedulerJpaEntity (Optional)
     */
    public Optional<CrawlSchedulerJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qScheduler).where(qScheduler.id.eq(id)).fetchOne());
    }

    /**
     * ID로 CrawlScheduler 존재 여부 확인
     *
     * @param id CrawlScheduler ID
     * @return 존재 여부
     */
    public boolean existsById(Long id) {
        Integer count =
                queryFactory.selectOne().from(qScheduler).where(qScheduler.id.eq(id)).fetchFirst();

        return count != null;
    }

    /**
     * 셀러 ID와 스케줄러 이름으로 존재 여부 확인
     *
     * @param sellerId 셀러 ID
     * @param schedulerName 스케줄러 이름
     * @return 존재 여부
     */
    public boolean existsBySellerIdAndSchedulerName(Long sellerId, String schedulerName) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qScheduler)
                        .where(
                                qScheduler.sellerId.eq(sellerId),
                                qScheduler.schedulerName.eq(schedulerName))
                        .fetchFirst();

        return count != null;
    }

    /**
     * 검색 조건으로 CrawlScheduler 목록 조회
     *
     * <p>Offset 페이징을 지원합니다.
     *
     * @param criteria 검색 조건 (CrawlSchedulerPageCriteria)
     * @return CrawlSchedulerJpaEntity 목록
     */
    public List<CrawlSchedulerJpaEntity> findByCriteria(CrawlSchedulerPageCriteria criteria) {
        var query = queryFactory.selectFrom(qScheduler).where(buildSearchConditions(criteria));

        // Offset 페이징
        query = query.offset(criteria.offset()).limit(criteria.size());

        // 기본 정렬: createdAt 내림차순 (등록 최신순)
        query = query.orderBy(qScheduler.createdAt.desc());

        return query.fetch();
    }

    /**
     * 검색 조건으로 CrawlScheduler 개수 조회
     *
     * @param criteria 검색 조건 (CrawlSchedulerPageCriteria)
     * @return CrawlScheduler 개수
     */
    public long countByCriteria(CrawlSchedulerPageCriteria criteria) {
        Long count =
                queryFactory
                        .select(qScheduler.count())
                        .from(qScheduler)
                        .where(buildSearchConditions(criteria))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 셀러 ID로 활성 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 목록
     */
    public List<CrawlSchedulerJpaEntity> findActiveBySellerIdmethod(Long sellerId) {
        return queryFactory
                .selectFrom(qScheduler)
                .where(
                        qScheduler.sellerId.eq(sellerId),
                        qScheduler.status.eq(SchedulerStatus.ACTIVE))
                .orderBy(qScheduler.createdAt.desc())
                .fetch();
    }

    /**
     * 셀러 ID로 전체 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 전체 스케줄러 개수
     */
    public long countBySellerId(Long sellerId) {
        Long count =
                queryFactory
                        .select(qScheduler.count())
                        .from(qScheduler)
                        .where(qScheduler.sellerId.eq(sellerId))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 셀러 ID로 활성 스케줄러 개수 조회
     *
     * @param sellerId 셀러 ID
     * @return 활성 스케줄러 개수
     */
    public long countActiveSchedulersBySellerId(Long sellerId) {
        Long count =
                queryFactory
                        .select(qScheduler.count())
                        .from(qScheduler)
                        .where(
                                qScheduler.sellerId.eq(sellerId),
                                qScheduler.status.eq(SchedulerStatus.ACTIVE))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 셀러 ID로 전체 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄러 목록 (생성일시 내림차순)
     */
    public List<CrawlSchedulerJpaEntity> findBySellerId(Long sellerId) {
        return queryFactory
                .selectFrom(qScheduler)
                .where(qScheduler.sellerId.eq(sellerId))
                .orderBy(qScheduler.createdAt.desc())
                .fetch();
    }

    /**
     * 검색 조건 구성 (Private 헬퍼 메서드)
     *
     * <p>BooleanExpression을 사용하여 동적 쿼리를 구성합니다.
     */
    private BooleanExpression buildSearchConditions(CrawlSchedulerPageCriteria criteria) {
        BooleanExpression expression = null;

        // 조건 1: 셀러 ID
        if (criteria.hasSellerFilter()) {
            BooleanExpression sellerIdCondition =
                    qScheduler.sellerId.eq(criteria.sellerId().value());
            expression = expression != null ? expression.and(sellerIdCondition) : sellerIdCondition;
        }

        // 조건 2: 상태 (다중 상태 IN 조건)
        BooleanExpression statusesCondition = statusesIn(criteria.statuses());
        if (statusesCondition != null) {
            expression = expression != null ? expression.and(statusesCondition) : statusesCondition;
        }

        // 조건 3-4: 생성일 범위
        if (criteria.hasDateFilter()) {
            BooleanExpression createdFromCondition =
                    createdAtGoe(criteria.dateRange().startInstant());
            if (createdFromCondition != null) {
                expression =
                        expression != null
                                ? expression.and(createdFromCondition)
                                : createdFromCondition;
            }

            BooleanExpression createdToCondition = createdAtLoe(criteria.dateRange().endInstant());
            if (createdToCondition != null) {
                expression =
                        expression != null
                                ? expression.and(createdToCondition)
                                : createdToCondition;
            }
        }

        return expression;
    }

    /**
     * 다중 상태 필터 BooleanExpression
     *
     * @param statuses 상태 목록 (null이거나 빈 리스트면 필터 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression statusesIn(List<SchedulerStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return qScheduler.status.in(statuses);
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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdFrom, ZoneId.systemDefault());
        return qScheduler.createdAt.goe(localDateTime);
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
        LocalDateTime localDateTime = LocalDateTime.ofInstant(createdTo, ZoneId.systemDefault());
        return qScheduler.createdAt.loe(localDateTime);
    }
}
