package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.DashboardCountsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.OutboxStatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.StatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.SystemFailureCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QCrawledRawJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.QProductSyncOutboxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.QCrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskJpaEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.task.entity.QCrawlTaskOutboxJpaEntity;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MonitoringCompositeQueryDslRepository {

    private static final QCrawlTaskJpaEntity crawlTask = QCrawlTaskJpaEntity.crawlTaskJpaEntity;
    private static final QCrawlTaskOutboxJpaEntity crawlTaskOutbox =
            QCrawlTaskOutboxJpaEntity.crawlTaskOutboxJpaEntity;
    private static final QCrawlSchedulerOutBoxJpaEntity schedulerOutbox =
            QCrawlSchedulerOutBoxJpaEntity.crawlSchedulerOutBoxJpaEntity;
    private static final QProductSyncOutboxJpaEntity productSyncOutbox =
            QProductSyncOutboxJpaEntity.productSyncOutboxJpaEntity;
    private static final QCrawledRawJpaEntity crawledRaw = QCrawledRawJpaEntity.crawledRawJpaEntity;
    private static final QCrawlSchedulerJpaEntity crawlScheduler =
            QCrawlSchedulerJpaEntity.crawlSchedulerJpaEntity;

    private final JPAQueryFactory queryFactory;

    public MonitoringCompositeQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public DashboardCountsDto fetchDashboardCounts(Instant threshold) {
        LocalDateTime thresholdLdt = LocalDateTime.ofInstant(threshold, ZoneOffset.UTC);

        Long activeSchedulers =
                queryFactory
                        .select(crawlScheduler.count())
                        .from(crawlScheduler)
                        .where(crawlScheduler.status.stringValue().eq("ACTIVE"))
                        .fetchOne();

        Long runningTasks =
                queryFactory
                        .select(crawlTask.count())
                        .from(crawlTask)
                        .where(crawlTask.status.stringValue().eq("RUNNING"))
                        .fetchOne();

        Long pendingOutbox =
                queryFactory
                        .select(crawlTaskOutbox.count())
                        .from(crawlTaskOutbox)
                        .where(crawlTaskOutbox.status.stringValue().eq("PENDING"))
                        .fetchOne();

        Long recentErrors =
                queryFactory
                        .select(crawlTask.count())
                        .from(crawlTask)
                        .where(
                                crawlTask
                                        .status
                                        .stringValue()
                                        .eq("FAILED")
                                        .and(crawlTask.updatedAt.after(thresholdLdt)))
                        .fetchOne();

        return new DashboardCountsDto(
                nullToZero(activeSchedulers),
                nullToZero(runningTasks),
                nullToZero(pendingOutbox),
                nullToZero(recentErrors));
    }

    public List<StatusCountDto> fetchCrawlTaskCountsByStatus() {
        return queryFactory
                .select(
                        Projections.constructor(
                                StatusCountDto.class,
                                crawlTask.status.stringValue(),
                                crawlTask.count()))
                .from(crawlTask)
                .groupBy(crawlTask.status)
                .fetch();
    }

    public long fetchStuckCrawlTasks(Instant threshold) {
        LocalDateTime thresholdLdt = LocalDateTime.ofInstant(threshold, ZoneOffset.UTC);

        Long count =
                queryFactory
                        .select(crawlTask.count())
                        .from(crawlTask)
                        .where(
                                crawlTask
                                        .status
                                        .stringValue()
                                        .eq("RUNNING")
                                        .and(crawlTask.updatedAt.before(thresholdLdt)))
                        .fetchOne();

        return nullToZero(count);
    }

    public List<OutboxStatusCountDto> fetchOutboxCountsByType() {
        List<OutboxStatusCountDto> results = new ArrayList<>();

        // CrawlTask Outbox
        List<OutboxStatusCountDto> taskOutboxCounts =
                queryFactory
                        .select(
                                Projections.constructor(
                                        OutboxStatusCountDto.class,
                                        Expressions.constant("CRAWL_TASK"),
                                        crawlTaskOutbox.status.stringValue(),
                                        crawlTaskOutbox.count()))
                        .from(crawlTaskOutbox)
                        .groupBy(crawlTaskOutbox.status)
                        .fetch();
        results.addAll(taskOutboxCounts);

        // Scheduler Outbox
        List<OutboxStatusCountDto> schedulerOutboxCounts =
                queryFactory
                        .select(
                                Projections.constructor(
                                        OutboxStatusCountDto.class,
                                        Expressions.constant("SCHEDULER"),
                                        schedulerOutbox.status.stringValue(),
                                        schedulerOutbox.count()))
                        .from(schedulerOutbox)
                        .groupBy(schedulerOutbox.status)
                        .fetch();
        results.addAll(schedulerOutboxCounts);

        // ProductSync Outbox
        List<OutboxStatusCountDto> productSyncOutboxCounts =
                queryFactory
                        .select(
                                Projections.constructor(
                                        OutboxStatusCountDto.class,
                                        Expressions.constant("PRODUCT_SYNC"),
                                        productSyncOutbox.status.stringValue(),
                                        productSyncOutbox.count()))
                        .from(productSyncOutbox)
                        .groupBy(productSyncOutbox.status)
                        .fetch();
        results.addAll(productSyncOutboxCounts);

        return results;
    }

    public List<StatusCountDto> fetchCrawledRawCountsByStatus() {
        return queryFactory
                .select(
                        Projections.constructor(
                                StatusCountDto.class,
                                crawledRaw.status.stringValue(),
                                crawledRaw.count()))
                .from(crawledRaw)
                .groupBy(crawledRaw.status)
                .fetch();
    }

    public List<SystemFailureCountDto> fetchRecentFailureCounts(Instant threshold) {
        LocalDateTime thresholdLdt = LocalDateTime.ofInstant(threshold, ZoneOffset.UTC);
        List<SystemFailureCountDto> results = new ArrayList<>();

        // CrawlTask failures
        Long taskFailures =
                queryFactory
                        .select(crawlTask.count())
                        .from(crawlTask)
                        .where(
                                crawlTask
                                        .status
                                        .stringValue()
                                        .in("FAILED", "TIMEOUT")
                                        .and(crawlTask.updatedAt.after(thresholdLdt)))
                        .fetchOne();
        results.add(new SystemFailureCountDto("CRAWL_TASK", nullToZero(taskFailures)));

        // CrawlTaskOutbox failures
        Long taskOutboxFailures =
                queryFactory
                        .select(crawlTaskOutbox.count())
                        .from(crawlTaskOutbox)
                        .where(
                                crawlTaskOutbox
                                        .status
                                        .stringValue()
                                        .eq("FAILED")
                                        .and(crawlTaskOutbox.createdAt.after(thresholdLdt)))
                        .fetchOne();
        results.add(new SystemFailureCountDto("CRAWL_TASK_OUTBOX", nullToZero(taskOutboxFailures)));

        // SchedulerOutbox failures
        Long schedulerFailures =
                queryFactory
                        .select(schedulerOutbox.count())
                        .from(schedulerOutbox)
                        .where(
                                schedulerOutbox
                                        .status
                                        .stringValue()
                                        .eq("FAILED")
                                        .and(schedulerOutbox.createdAt.after(thresholdLdt)))
                        .fetchOne();
        results.add(new SystemFailureCountDto("SCHEDULER_OUTBOX", nullToZero(schedulerFailures)));

        // ProductSyncOutbox failures
        Long productSyncFailures =
                queryFactory
                        .select(productSyncOutbox.count())
                        .from(productSyncOutbox)
                        .where(
                                productSyncOutbox
                                        .status
                                        .stringValue()
                                        .eq("FAILED")
                                        .and(productSyncOutbox.createdAt.after(thresholdLdt)))
                        .fetchOne();
        results.add(
                new SystemFailureCountDto("PRODUCT_SYNC_OUTBOX", nullToZero(productSyncFailures)));

        return results;
    }

    private long nullToZero(Long value) {
        return value != null ? value : 0L;
    }
}
