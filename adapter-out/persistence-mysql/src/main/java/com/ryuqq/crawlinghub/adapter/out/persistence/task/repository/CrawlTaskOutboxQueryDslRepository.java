package com.ryuqq.crawlinghub.adapter.out.persistence.task.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.CrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskOutboxJpaEntity;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlTaskOutboxQueryDslRepository - CrawlTaskOutbox QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>표준 메서드:</strong>
 *
 * <ul>
 *   <li>findByCrawlTaskId(Long): Task ID로 Outbox 조회
 *   <li>findByCriteria(Criteria): 조건으로 목록 조회
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
public class CrawlTaskOutboxQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private static final QCrawlTaskOutboxJpaEntity qOutbox =
            QCrawlTaskOutboxJpaEntity.crawlTaskOutboxJpaEntity;

    public CrawlTaskOutboxQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * CrawlTask ID로 Outbox 단건 조회
     *
     * @param crawlTaskId CrawlTask ID
     * @return CrawlTaskOutboxJpaEntity (Optional)
     */
    public Optional<CrawlTaskOutboxJpaEntity> findByCrawlTaskId(Long crawlTaskId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(qOutbox)
                        .where(qOutbox.crawlTaskId.eq(crawlTaskId))
                        .fetchOne());
    }

    /**
     * 검색 조건으로 Outbox 목록 조회
     *
     * <p>오래된 것부터 처리하기 위해 createdAt 오름차순 정렬합니다.
     *
     * @param criteria 검색 조건 (CrawlTaskOutboxCriteria)
     * @return CrawlTaskOutboxJpaEntity 목록
     */
    public List<CrawlTaskOutboxJpaEntity> findByCriteria(CrawlTaskOutboxCriteria criteria) {
        var query =
                queryFactory
                        .selectFrom(qOutbox)
                        .where(buildSearchConditions(criteria))
                        .orderBy(qOutbox.createdAt.asc())
                        .limit(criteria.limit());

        return query.fetch();
    }

    /**
     * 검색 조건 구성 (Private 헬퍼 메서드)
     *
     * <p>BooleanExpression을 사용하여 동적 쿼리를 구성합니다.
     */
    private BooleanExpression buildSearchConditions(CrawlTaskOutboxCriteria criteria) {
        BooleanExpression expression = null;

        // 조건 1: 단일 상태 필터
        if (criteria.hasSingleStatusFilter()) {
            expression = qOutbox.status.eq(criteria.status());
        }

        // 조건 2: 다중 상태 필터 (OR 조건)
        if (criteria.hasMultipleStatusFilter()) {
            BooleanExpression statusesCondition = qOutbox.status.in(criteria.statuses());
            expression = expression != null ? expression.and(statusesCondition) : statusesCondition;
        }

        return expression;
    }
}
