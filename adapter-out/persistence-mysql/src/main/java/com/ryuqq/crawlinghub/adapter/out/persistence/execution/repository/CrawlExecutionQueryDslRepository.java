package com.ryuqq.crawlinghub.adapter.out.persistence.execution.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.CrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.execution.entity.QCrawlExecutionJpaEntity;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlExecutionQueryDslRepository - CrawlExecution QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드:</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>findByCriteria(Criteria): 목록 조회 (동적 쿼리)
 *   <li>countByCriteria(Criteria): 개수 조회 (동적 쿼리)
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
public class CrawlExecutionQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QCrawlExecutionJpaEntity qExecution =
            QCrawlExecutionJpaEntity.crawlExecutionJpaEntity;

    public CrawlExecutionQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 CrawlExecution 단건 조회
     *
     * @param id CrawlExecution ID
     * @return CrawlExecutionJpaEntity (Optional)
     */
    public Optional<CrawlExecutionJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qExecution).where(qExecution.id.eq(id)).fetchOne());
    }

    /**
     * 검색 조건으로 CrawlExecution 목록 조회
     *
     * <p>Offset 페이징을 지원합니다.
     *
     * @param criteria 검색 조건 (CrawlExecutionCriteria)
     * @return CrawlExecutionJpaEntity 목록
     */
    public List<CrawlExecutionJpaEntity> findByCriteria(CrawlExecutionCriteria criteria) {
        var query = queryFactory.selectFrom(qExecution).where(buildSearchConditions(criteria));

        // Offset 페이징
        query = query.offset(criteria.offset()).limit(criteria.size());

        // 기본 정렬: createdAt 내림차순 (최신순)
        query = query.orderBy(qExecution.createdAt.desc());

        return query.fetch();
    }

    /**
     * 검색 조건으로 CrawlExecution 개수 조회
     *
     * @param criteria 검색 조건 (CrawlExecutionCriteria)
     * @return CrawlExecution 개수
     */
    public long countByCriteria(CrawlExecutionCriteria criteria) {
        Long count =
                queryFactory
                        .select(qExecution.count())
                        .from(qExecution)
                        .where(buildSearchConditions(criteria))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 검색 조건 구성 (Private 헬퍼 메서드)
     *
     * <p>BooleanExpression을 사용하여 동적 쿼리를 구성합니다.
     */
    private BooleanExpression buildSearchConditions(CrawlExecutionCriteria criteria) {
        BooleanExpression expression = null;

        // 조건 1: Task ID 필터
        if (criteria.hasTaskIdFilter()) {
            BooleanExpression taskIdCondition =
                    qExecution.crawlTaskId.eq(criteria.crawlTaskId().value());
            expression = taskIdCondition;
        }

        // 조건 2: Scheduler ID 필터
        if (criteria.hasSchedulerIdFilter()) {
            BooleanExpression schedulerIdCondition =
                    qExecution.crawlSchedulerId.eq(criteria.crawlSchedulerId().value());
            expression =
                    expression != null
                            ? expression.and(schedulerIdCondition)
                            : schedulerIdCondition;
        }

        // 조건 3: Seller ID 필터
        if (criteria.hasSellerIdFilter()) {
            BooleanExpression sellerIdCondition =
                    qExecution.sellerId.eq(criteria.sellerId().value());
            expression = expression != null ? expression.and(sellerIdCondition) : sellerIdCondition;
        }

        // 조건 4: 상태 필터 (다중 상태 IN 조건)
        BooleanExpression statusesCondition = statusesIn(criteria.statuses());
        if (statusesCondition != null) {
            expression = expression != null ? expression.and(statusesCondition) : statusesCondition;
        }

        // 조건 5: 기간 필터
        if (criteria.hasPeriodFilter()) {
            BooleanExpression periodCondition = buildPeriodCondition(criteria);
            if (periodCondition != null) {
                expression = expression != null ? expression.and(periodCondition) : periodCondition;
            }
        }

        return expression;
    }

    /**
     * 기간 조건 구성 (Private 헬퍼 메서드)
     *
     * @param criteria 검색 조건
     * @return BooleanExpression
     */
    private BooleanExpression buildPeriodCondition(CrawlExecutionCriteria criteria) {
        BooleanExpression condition = null;

        if (criteria.from() != null) {
            condition = qExecution.createdAt.goe(toLocalDateTime(criteria.from()));
        }

        if (criteria.to() != null) {
            BooleanExpression toCondition =
                    qExecution.createdAt.loe(toLocalDateTime(criteria.to()));
            condition = condition != null ? condition.and(toCondition) : toCondition;
        }

        return condition;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * 다중 상태 필터 BooleanExpression
     *
     * @param statuses 상태 목록 (null이거나 빈 리스트면 필터 없음)
     * @return BooleanExpression (null이면 조건 없음)
     */
    private BooleanExpression statusesIn(List<CrawlExecutionStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return qExecution.status.in(statuses);
    }

    /**
     * 상위 에러 메시지 조회
     *
     * @param criteria 통계 조회 조건
     * @param limit 조회할 에러 개수
     * @return 에러 메시지별 발생 횟수 목록
     */
    public List<CrawlExecutionQueryPort.ErrorCount> getTopErrors(
            CrawlExecutionStatisticsCriteria criteria, int limit) {
        var query =
                queryFactory
                        .select(qExecution.errorMessage, qExecution.count())
                        .from(qExecution)
                        .where(
                                qExecution.errorMessage.isNotNull(),
                                buildStatisticsConditions(criteria))
                        .groupBy(qExecution.errorMessage)
                        .orderBy(qExecution.count().desc())
                        .limit(limit);

        List<Tuple> results = query.fetch();

        List<CrawlExecutionQueryPort.ErrorCount> errorCounts = new ArrayList<>();
        for (Tuple tuple : results) {
            String errorMessage = tuple.get(qExecution.errorMessage);
            Long count = tuple.get(qExecution.count());
            errorCounts.add(
                    new CrawlExecutionQueryPort.ErrorCount(
                            errorMessage, count != null ? count : 0L));
        }

        return errorCounts;
    }

    private BooleanExpression buildStatisticsConditions(CrawlExecutionStatisticsCriteria criteria) {
        BooleanExpression expression = null;

        if (criteria.crawlSchedulerId() != null) {
            expression = qExecution.crawlSchedulerId.eq(criteria.crawlSchedulerId().value());
        }

        if (criteria.sellerId() != null) {
            BooleanExpression sellerCondition = qExecution.sellerId.eq(criteria.sellerId().value());
            expression = expression != null ? expression.and(sellerCondition) : sellerCondition;
        }

        if (criteria.from() != null) {
            BooleanExpression fromCondition =
                    qExecution.createdAt.goe(toLocalDateTime(criteria.from()));
            expression = expression != null ? expression.and(fromCondition) : fromCondition;
        }

        if (criteria.to() != null) {
            BooleanExpression toCondition =
                    qExecution.createdAt.loe(toLocalDateTime(criteria.to()));
            expression = expression != null ? expression.and(toCondition) : toCondition;
        }

        return expression;
    }
}
