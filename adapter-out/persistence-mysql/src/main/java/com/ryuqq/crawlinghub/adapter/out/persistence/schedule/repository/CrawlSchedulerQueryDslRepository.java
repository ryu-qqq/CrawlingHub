package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.condition.CrawlSchedulerConditionBuilder;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlSchedulerQueryDslRepository - CrawlScheduler QueryDSL Repository
 *
 * <p>QueryDSL 기반 조회 쿼리를 처리하는 전용 Repository입니다.
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>Join 절대 금지
 *   <li>비즈니스 로직 금지
 *   <li>Mapper 호출 금지
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class CrawlSchedulerQueryDslRepository {

    private final JPAQueryFactory queryFactory;
    private final CrawlSchedulerConditionBuilder conditionBuilder;
    private static final QCrawlSchedulerJpaEntity qScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;

    public CrawlSchedulerQueryDslRepository(
            JPAQueryFactory queryFactory, CrawlSchedulerConditionBuilder conditionBuilder) {
        this.queryFactory = queryFactory;
        this.conditionBuilder = conditionBuilder;
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
     * @param criteria 검색 조건 (CrawlSchedulerSearchCriteria)
     * @return CrawlSchedulerJpaEntity 목록
     */
    public List<CrawlSchedulerJpaEntity> findByCriteria(CrawlSchedulerSearchCriteria criteria) {
        return queryFactory
                .selectFrom(qScheduler)
                .where(
                        conditionBuilder.sellerIdEq(criteria),
                        conditionBuilder.statusIn(criteria),
                        conditionBuilder.searchCondition(criteria))
                .offset(criteria.offset())
                .limit(criteria.size())
                .orderBy(resolveOrderSpecifier(criteria))
                .fetch();
    }

    /**
     * 검색 조건으로 CrawlScheduler 개수 조회
     *
     * @param criteria 검색 조건 (CrawlSchedulerSearchCriteria)
     * @return CrawlScheduler 개수
     */
    public long countByCriteria(CrawlSchedulerSearchCriteria criteria) {
        Long count =
                queryFactory
                        .select(qScheduler.count())
                        .from(qScheduler)
                        .where(
                                conditionBuilder.sellerIdEq(criteria),
                                conditionBuilder.statusIn(criteria),
                                conditionBuilder.searchCondition(criteria))
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
     * SortKey + SortDirection 기반 동적 정렬
     *
     * @param criteria 검색 조건
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> resolveOrderSpecifier(CrawlSchedulerSearchCriteria criteria) {
        CrawlSchedulerSortKey sortKey = criteria.queryContext().sortKey();
        boolean ascending = criteria.queryContext().isAscending();

        ComparableExpressionBase<?> path =
                switch (sortKey) {
                    case CREATED_AT -> qScheduler.createdAt;
                    case UPDATED_AT -> qScheduler.updatedAt;
                    case SCHEDULER_NAME -> qScheduler.schedulerName;
                };

        return ascending ? path.asc() : path.desc();
    }
}
