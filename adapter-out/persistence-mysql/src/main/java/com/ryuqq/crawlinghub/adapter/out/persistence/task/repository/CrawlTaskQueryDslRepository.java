package com.ryuqq.crawlinghub.adapter.out.persistence.task.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlTaskQueryDslRepository - CrawlTask QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드:</strong>
 *
 * <ul>
 *   <li>findById(Long id): 단건 조회
 *   <li>existsBySchedulerIdAndStatusIn: 상태 목록으로 존재 여부 확인
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
public class CrawlTaskQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QCrawlTaskJpaEntity qTask = QCrawlTaskJpaEntity.crawlTaskJpaEntity;

    public CrawlTaskQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * ID로 CrawlTask 단건 조회
     *
     * @param id CrawlTask ID
     * @return CrawlTaskJpaEntity (Optional)
     */
    public Optional<CrawlTaskJpaEntity> findById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(qTask).where(qTask.id.eq(id)).fetchOne());
    }

    /**
     * 스케줄러 ID와 상태 목록으로 존재 여부 확인
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param statuses 확인할 상태 목록
     * @return 존재 여부
     */
    public boolean existsBySchedulerIdAndStatusIn(
            Long crawlSchedulerId, List<CrawlTaskStatus> statuses) {
        Integer count =
                queryFactory
                        .selectOne()
                        .from(qTask)
                        .where(
                                qTask.crawlSchedulerId.eq(crawlSchedulerId),
                                qTask.status.in(statuses))
                        .fetchFirst();

        return count != null;
    }

    /**
     * 검색 조건으로 CrawlTask 목록 조회
     *
     * <p>Offset 페이징을 지원합니다.
     *
     * @param criteria 검색 조건 (CrawlTaskCriteria)
     * @return CrawlTaskJpaEntity 목록
     */
    public List<CrawlTaskJpaEntity> findByCriteria(CrawlTaskCriteria criteria) {
        var query = queryFactory.selectFrom(qTask).where(buildSearchConditions(criteria));

        // Offset 페이징
        query = query.offset(criteria.offset()).limit(criteria.size());

        // 기본 정렬: createdAt 내림차순 (등록 최신순)
        query = query.orderBy(qTask.createdAt.desc());

        return query.fetch();
    }

    /**
     * 검색 조건으로 CrawlTask 개수 조회
     *
     * @param criteria 검색 조건 (CrawlTaskCriteria)
     * @return CrawlTask 개수
     */
    public long countByCriteria(CrawlTaskCriteria criteria) {
        Long count =
                queryFactory
                        .select(qTask.count())
                        .from(qTask)
                        .where(buildSearchConditions(criteria))
                        .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 검색 조건 구성 (Private 헬퍼 메서드)
     *
     * <p>BooleanExpression을 사용하여 동적 쿼리를 구성합니다.
     */
    private BooleanExpression buildSearchConditions(CrawlTaskCriteria criteria) {
        // 스케줄러 ID는 필수
        BooleanExpression expression =
                qTask.crawlSchedulerId.eq(criteria.crawlSchedulerId().value());

        // 조건: 상태 필터
        if (criteria.hasStatusFilter()) {
            expression = expression.and(qTask.status.eq(criteria.status()));
        }

        // 조건: 태스크 유형 필터
        if (criteria.hasTaskTypeFilter()) {
            expression = expression.and(qTask.taskType.eq(criteria.taskType()));
        }

        return expression;
    }

    /**
     * 상태별 CrawlTask 개수 조회
     *
     * @param criteria 통계 조회 조건
     * @return 상태별 개수 Map
     */
    public Map<CrawlTaskStatus, Long> countByStatus(CrawlTaskStatisticsCriteria criteria) {
        List<Tuple> results =
                queryFactory
                        .select(qTask.status, qTask.count())
                        .from(qTask)
                        .where(buildStatisticsConditions(criteria))
                        .groupBy(qTask.status)
                        .fetch();

        Map<CrawlTaskStatus, Long> statusCounts = new EnumMap<>(CrawlTaskStatus.class);
        for (Tuple tuple : results) {
            CrawlTaskStatus status = tuple.get(qTask.status);
            Long count = tuple.get(qTask.count());
            if (status != null) {
                statusCounts.put(status, count != null ? count : 0L);
            }
        }

        return statusCounts;
    }

    /**
     * 타입별 CrawlTask 통계 조회
     *
     * @param criteria 통계 조회 조건
     * @return 타입별 통계 Map
     */
    public Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> countByTaskType(
            CrawlTaskStatisticsCriteria criteria) {
        List<Tuple> results =
                queryFactory
                        .select(qTask.taskType, qTask.status, qTask.count())
                        .from(qTask)
                        .where(buildStatisticsConditions(criteria))
                        .groupBy(qTask.taskType, qTask.status)
                        .fetch();

        Map<CrawlTaskType, Map<CrawlTaskStatus, Long>> typeStatusCounts =
                new EnumMap<>(CrawlTaskType.class);

        for (Tuple tuple : results) {
            CrawlTaskType taskType = tuple.get(qTask.taskType);
            CrawlTaskStatus status = tuple.get(qTask.status);
            Long count = tuple.get(qTask.count());

            if (taskType != null && status != null) {
                typeStatusCounts
                        .computeIfAbsent(taskType, k -> new EnumMap<>(CrawlTaskStatus.class))
                        .put(status, count != null ? count : 0L);
            }
        }

        Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> result =
                new EnumMap<>(CrawlTaskType.class);
        for (Map.Entry<CrawlTaskType, Map<CrawlTaskStatus, Long>> entry :
                typeStatusCounts.entrySet()) {
            CrawlTaskType taskType = entry.getKey();
            Map<CrawlTaskStatus, Long> statusMap = entry.getValue();

            long total = statusMap.values().stream().mapToLong(Long::longValue).sum();
            long success = statusMap.getOrDefault(CrawlTaskStatus.SUCCESS, 0L);
            long failed = statusMap.getOrDefault(CrawlTaskStatus.FAILED, 0L);

            result.put(taskType, new CrawlTaskQueryPort.TaskTypeCount(total, success, failed));
        }

        return result;
    }

    private BooleanExpression buildStatisticsConditions(CrawlTaskStatisticsCriteria criteria) {
        BooleanExpression expression = null;

        if (criteria.crawlSchedulerId() != null) {
            expression = qTask.crawlSchedulerId.eq(criteria.crawlSchedulerId().value());
        }

        if (criteria.from() != null) {
            BooleanExpression fromCondition = qTask.createdAt.goe(toLocalDateTime(criteria.from()));
            expression = expression != null ? expression.and(fromCondition) : fromCondition;
        }

        if (criteria.to() != null) {
            BooleanExpression toCondition = qTask.createdAt.loe(toLocalDateTime(criteria.to()));
            expression = expression != null ? expression.and(toCondition) : toCondition;
        }

        return expression;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
