package com.ryuqq.crawlinghub.application.dashboard.service.query;

import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.DailySuccessRate;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.FailedTaskSummary;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.OutboxStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.ScheduleStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.TodayTaskStats;
import com.ryuqq.crawlinghub.application.dashboard.port.in.query.GetDashboardStatsUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Dashboard 통계 조회 Service
 *
 * <p>{@link GetDashboardStatsUseCase} 구현체
 *
 * <p><strong>조회 항목</strong>:
 *
 * <ul>
 *   <li>오늘 태스크 통계 (CrawlTaskQueryPort 사용)
 *   <li>최근 7일 성공률 (CrawlTaskQueryPort 사용)
 *   <li>스케줄 통계 (CrawlScheduleQueryPort 사용)
 *   <li>Outbox 통계 (CrawlTaskOutboxQueryPort 사용)
 *   <li>최근 실패 태스크 (CrawlTaskQueryPort 사용)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetDashboardStatsService implements GetDashboardStatsUseCase {

    private static final int RECENT_DAYS = 7;
    private static final int RECENT_FAILED_LIMIT = 10;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final CrawlTaskQueryPort taskQueryPort;
    private final CrawlScheduleQueryPort scheduleQueryPort;
    private final CrawlTaskOutboxQueryPort outboxQueryPort;

    public GetDashboardStatsService(
            CrawlTaskQueryPort taskQueryPort,
            CrawlScheduleQueryPort scheduleQueryPort,
            CrawlTaskOutboxQueryPort outboxQueryPort) {
        this.taskQueryPort = taskQueryPort;
        this.scheduleQueryPort = scheduleQueryPort;
        this.outboxQueryPort = outboxQueryPort;
    }

    @Override
    public DashboardStatsResponse execute() {
        TodayTaskStats todayTaskStats = getTodayTaskStats();
        List<DailySuccessRate> weeklySuccessRates = getWeeklySuccessRates();
        ScheduleStats scheduleStats = getScheduleStats();
        OutboxStats outboxStats = getOutboxStats();
        List<FailedTaskSummary> recentFailedTasks = getRecentFailedTasks();

        return new DashboardStatsResponse(
                todayTaskStats, weeklySuccessRates, scheduleStats, outboxStats, recentFailedTasks);
    }

    /** 오늘 태스크 통계 조회 */
    private TodayTaskStats getTodayTaskStats() {
        LocalDate today = LocalDate.now(ZONE_ID);
        Instant from = today.atStartOfDay(ZONE_ID).toInstant();
        Instant to = today.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

        CrawlTaskStatisticsCriteria criteria =
                new CrawlTaskStatisticsCriteria(null, null, from, to);
        Map<CrawlTaskStatus, Long> statusCounts = taskQueryPort.countByStatus(criteria);

        long success = statusCounts.getOrDefault(CrawlTaskStatus.SUCCESS, 0L);
        long failed =
                statusCounts.getOrDefault(CrawlTaskStatus.FAILED, 0L)
                        + statusCounts.getOrDefault(CrawlTaskStatus.TIMEOUT, 0L);
        long inProgress =
                statusCounts.getOrDefault(CrawlTaskStatus.RUNNING, 0L)
                        + statusCounts.getOrDefault(CrawlTaskStatus.PUBLISHED, 0L);
        long waiting =
                statusCounts.getOrDefault(CrawlTaskStatus.WAITING, 0L)
                        + statusCounts.getOrDefault(CrawlTaskStatus.RETRY, 0L);
        long total = success + failed + inProgress + waiting;

        return TodayTaskStats.of(total, success, failed, inProgress, waiting);
    }

    /** 최근 7일 성공률 조회 */
    private List<DailySuccessRate> getWeeklySuccessRates() {
        List<DailySuccessRate> result = new ArrayList<>();
        LocalDate today = LocalDate.now(ZONE_ID);

        for (int i = RECENT_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant from = date.atStartOfDay(ZONE_ID).toInstant();
            Instant to = date.plusDays(1).atStartOfDay(ZONE_ID).toInstant();

            CrawlTaskStatisticsCriteria criteria =
                    new CrawlTaskStatisticsCriteria(null, null, from, to);
            Map<CrawlTaskStatus, Long> statusCounts = taskQueryPort.countByStatus(criteria);

            long success = statusCounts.getOrDefault(CrawlTaskStatus.SUCCESS, 0L);
            long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

            result.add(DailySuccessRate.of(date.format(DATE_FORMATTER), total, success));
        }

        return result;
    }

    /** 스케줄 통계 조회 */
    private ScheduleStats getScheduleStats() {
        CrawlSchedulerPageCriteria allCriteria =
                CrawlSchedulerPageCriteria.of(
                        null, null, null, PageRequest.first(PageRequest.MAX_SIZE));
        CrawlSchedulerPageCriteria activeCriteria =
                CrawlSchedulerPageCriteria.of(
                        null,
                        List.of(SchedulerStatus.ACTIVE),
                        null,
                        PageRequest.first(PageRequest.MAX_SIZE));

        long total = scheduleQueryPort.count(allCriteria);
        long active = scheduleQueryPort.count(activeCriteria);
        long inactive = total - active;

        return new ScheduleStats(total, active, inactive);
    }

    /** Outbox 통계 조회 */
    private OutboxStats getOutboxStats() {
        long pending =
                outboxQueryPort.countByCriteria(
                        CrawlTaskOutboxCriteria.byStatus(OutboxStatus.PENDING, Integer.MAX_VALUE));
        long sent =
                outboxQueryPort.countByCriteria(
                        CrawlTaskOutboxCriteria.byStatus(OutboxStatus.SENT, Integer.MAX_VALUE));
        long failed =
                outboxQueryPort.countByCriteria(
                        CrawlTaskOutboxCriteria.byStatus(OutboxStatus.FAILED, Integer.MAX_VALUE));

        return new OutboxStats(pending, sent, failed);
    }

    /** 최근 실패 태스크 조회 */
    private List<FailedTaskSummary> getRecentFailedTasks() {
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        CrawlTaskCriteria criteria =
                new CrawlTaskCriteria(
                        null, // crawlSchedulerId
                        null, // sellerId
                        List.of(CrawlTaskStatus.FAILED), // statuses
                        null, // taskTypes
                        oneDayAgo, // createdFrom
                        null, // createdTo
                        0, // page
                        RECENT_FAILED_LIMIT // size
                        );

        List<CrawlTask> failedTasks = taskQueryPort.findByCriteria(criteria);

        return failedTasks.stream()
                .map(
                        task ->
                                new FailedTaskSummary(
                                        task.getId().value(),
                                        task.getCrawlSchedulerIdValue(),
                                        task.getTaskType().name(),
                                        task.getStatus().name(),
                                        task.getUpdatedAt()))
                .toList();
    }
}
