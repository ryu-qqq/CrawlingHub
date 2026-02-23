package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QSellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * CrawlScheduler Composite QueryDSL Repository
 *
 * <p>스케줄러 상세 조회를 위한 크로스 도메인 Composite 쿼리
 *
 * <p><strong>쿼리 전략</strong>: Independent query composition
 *
 * <ol>
 *   <li>Scheduler LEFT JOIN Seller
 *   <li>CrawlTask 최근 N건
 *   <li>CrawlTask GROUP BY status (통계)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class CrawlSchedulerCompositeQueryDslRepository {

    private static final QCrawlSchedulerJpaEntity crawlScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;
    private static final QSellerJpaEntity seller = QSellerJpaEntity.sellerJpaEntity;
    private static final QCrawlTaskJpaEntity crawlTask = QCrawlTaskJpaEntity.crawlTaskJpaEntity;

    private final JPAQueryFactory queryFactory;

    public CrawlSchedulerCompositeQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Scheduler + Seller LEFT JOIN 조회
     *
     * @param schedulerId 스케줄러 ID
     * @return Composite DTO (Optional)
     */
    public Optional<CrawlSchedulerCompositeDto> fetchSchedulerWithSeller(Long schedulerId) {
        CrawlSchedulerCompositeDto result =
                queryFactory
                        .select(
                                Projections.constructor(
                                        CrawlSchedulerCompositeDto.class,
                                        crawlScheduler.id,
                                        crawlScheduler.sellerId,
                                        crawlScheduler.schedulerName,
                                        crawlScheduler.cronExpression,
                                        crawlScheduler.status.stringValue(),
                                        crawlScheduler.createdAt,
                                        crawlScheduler.updatedAt,
                                        seller.sellerName,
                                        seller.mustItSellerName))
                        .from(crawlScheduler)
                        .leftJoin(seller)
                        .on(crawlScheduler.sellerId.eq(seller.id))
                        .where(crawlScheduler.id.eq(schedulerId))
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 최근 태스크 목록 조회
     *
     * @param schedulerId 스케줄러 ID
     * @param limit 조회 건수
     * @return 태스크 요약 DTO 목록 (생성일시 내림차순)
     */
    public List<CrawlSchedulerTaskSummaryDto> fetchRecentTasks(Long schedulerId, int limit) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CrawlSchedulerTaskSummaryDto.class,
                                crawlTask.id,
                                crawlTask.status.stringValue(),
                                crawlTask.taskType.stringValue(),
                                crawlTask.createdAt,
                                crawlTask.updatedAt))
                .from(crawlTask)
                .where(crawlTask.crawlSchedulerId.eq(schedulerId))
                .orderBy(crawlTask.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 태스크 상태별 통계 조회
     *
     * @param schedulerId 스케줄러 ID
     * @return 상태별 통계 DTO 목록
     */
    public List<CrawlSchedulerTaskStatisticsDto> fetchTaskStatistics(Long schedulerId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                CrawlSchedulerTaskStatisticsDto.class,
                                crawlTask.status.stringValue(),
                                crawlTask.count()))
                .from(crawlTask)
                .where(crawlTask.crawlSchedulerId.eq(schedulerId))
                .groupBy(crawlTask.status)
                .fetch();
    }
}
