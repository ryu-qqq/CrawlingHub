package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerSchedulerSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.seller.entity.QSellerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Seller Composite QueryDSL Repository
 *
 * <p>셀러 상세 조회를 위한 크로스 도메인 Composite 쿼리
 *
 * <p><strong>쿼리 전략</strong>: Independent query composition
 *
 * <ol>
 *   <li>Seller 단건 조회
 *   <li>CrawlScheduler WHERE sellerId
 *   <li>CrawlTask 최근 N건
 *   <li>CrawlTask GROUP BY status (통계)
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Repository
public class SellerCompositeQueryDslRepository {

    private static final QSellerJpaEntity seller = QSellerJpaEntity.sellerJpaEntity;
    private static final QCrawlSchedulerJpaEntity crawlScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;
    private static final QCrawlTaskJpaEntity crawlTask = QCrawlTaskJpaEntity.crawlTaskJpaEntity;

    private final JPAQueryFactory queryFactory;

    public SellerCompositeQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    /**
     * Seller 단건 조회
     *
     * @param sellerId 셀러 ID
     * @return Composite DTO (Optional)
     */
    public Optional<SellerCompositeDto> fetchSeller(Long sellerId) {
        SellerCompositeDto result =
                queryFactory
                        .select(
                                Projections.constructor(
                                        SellerCompositeDto.class,
                                        seller.id,
                                        seller.mustItSellerName,
                                        seller.sellerName,
                                        seller.status.stringValue(),
                                        seller.productCount,
                                        seller.createdAt,
                                        seller.updatedAt))
                        .from(seller)
                        .where(seller.id.eq(sellerId))
                        .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 셀러 연관 스케줄러 목록 조회
     *
     * @param sellerId 셀러 ID
     * @return 스케줄러 요약 DTO 목록
     */
    public List<SellerSchedulerSummaryDto> fetchSchedulers(Long sellerId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SellerSchedulerSummaryDto.class,
                                crawlScheduler.id,
                                crawlScheduler.schedulerName,
                                crawlScheduler.status.stringValue(),
                                crawlScheduler.cronExpression))
                .from(crawlScheduler)
                .where(crawlScheduler.sellerId.eq(sellerId))
                .fetch();
    }

    /**
     * 최근 태스크 목록 조회
     *
     * @param sellerId 셀러 ID
     * @param limit 조회 건수
     * @return 태스크 요약 DTO 목록 (생성일시 내림차순)
     */
    public List<SellerTaskSummaryDto> fetchRecentTasks(Long sellerId, int limit) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SellerTaskSummaryDto.class,
                                crawlTask.id,
                                crawlTask.status.stringValue(),
                                crawlTask.taskType.stringValue(),
                                crawlTask.createdAt,
                                crawlTask.updatedAt))
                .from(crawlTask)
                .where(crawlTask.sellerId.eq(sellerId))
                .orderBy(crawlTask.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 태스크 상태별 통계 조회
     *
     * @param sellerId 셀러 ID
     * @return 상태별 통계 DTO 목록
     */
    public List<SellerTaskStatisticsDto> fetchTaskStatistics(Long sellerId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                SellerTaskStatisticsDto.class,
                                crawlTask.status.stringValue(),
                                crawlTask.count()))
                .from(crawlTask)
                .where(crawlTask.sellerId.eq(sellerId))
                .groupBy(crawlTask.status)
                .fetch();
    }
}
