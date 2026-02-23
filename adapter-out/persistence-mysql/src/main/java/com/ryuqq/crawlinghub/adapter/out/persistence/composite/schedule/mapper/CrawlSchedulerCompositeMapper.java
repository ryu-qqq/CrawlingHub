package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskSummaryDto;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SellerSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.TaskSummary;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * CrawlScheduler Composite Mapper
 *
 * <p>Persistence DTO → Application Result 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlSchedulerCompositeMapper {

    private static final Set<String> COMPLETED_STATUSES = Set.of("SUCCESS", "FAILED", "TIMEOUT");

    /**
     * Persistence DTO들을 CrawlSchedulerDetailResult로 변환
     *
     * @param compositeDto 스케줄러 + 셀러 JOIN 결과
     * @param taskSummaries 최근 태스크 목록
     * @param taskStatistics 태스크 상태별 통계
     * @return Application Layer 결과 DTO
     */
    public CrawlSchedulerDetailResult toResult(
            CrawlSchedulerCompositeDto compositeDto,
            List<CrawlSchedulerTaskSummaryDto> taskSummaries,
            List<CrawlSchedulerTaskStatisticsDto> taskStatistics) {

        SchedulerInfo schedulerInfo = toSchedulerInfo(compositeDto);
        SellerSummary sellerSummary = toSellerSummary(compositeDto);
        ExecutionInfo executionInfo = toExecutionInfo(taskSummaries);
        SchedulerStatistics statistics = toStatistics(taskStatistics);
        List<TaskSummary> tasks = toTaskSummaries(taskSummaries);

        return new CrawlSchedulerDetailResult(
                schedulerInfo, sellerSummary, executionInfo, statistics, tasks);
    }

    private SchedulerInfo toSchedulerInfo(CrawlSchedulerCompositeDto dto) {
        return new SchedulerInfo(
                dto.schedulerId(),
                dto.schedulerName(),
                dto.cronExpression(),
                dto.status(),
                toInstant(dto.schedulerCreatedAt()),
                toInstant(dto.schedulerUpdatedAt()));
    }

    private SellerSummary toSellerSummary(CrawlSchedulerCompositeDto dto) {
        if (dto.sellerName() == null) {
            return null;
        }
        return new SellerSummary(dto.sellerId(), dto.sellerName(), dto.mustItSellerName());
    }

    private ExecutionInfo toExecutionInfo(List<CrawlSchedulerTaskSummaryDto> taskSummaries) {
        if (taskSummaries.isEmpty()) {
            return new ExecutionInfo(null, null);
        }
        CrawlSchedulerTaskSummaryDto latestTask = taskSummaries.get(0);
        return new ExecutionInfo(toInstant(latestTask.createdAt()), latestTask.status());
    }

    private SchedulerStatistics toStatistics(List<CrawlSchedulerTaskStatisticsDto> taskStatistics) {
        long totalTasks =
                taskStatistics.stream().mapToLong(CrawlSchedulerTaskStatisticsDto::count).sum();
        long successTasks =
                taskStatistics.stream()
                        .filter(s -> "SUCCESS".equals(s.status()))
                        .mapToLong(CrawlSchedulerTaskStatisticsDto::count)
                        .sum();
        long failedTasks =
                taskStatistics.stream()
                        .filter(s -> "FAILED".equals(s.status()) || "TIMEOUT".equals(s.status()))
                        .mapToLong(CrawlSchedulerTaskStatisticsDto::count)
                        .sum();
        double successRate = totalTasks > 0 ? (double) successTasks / totalTasks : 0.0;

        return new SchedulerStatistics(totalTasks, successTasks, failedTasks, successRate);
    }

    private List<TaskSummary> toTaskSummaries(List<CrawlSchedulerTaskSummaryDto> taskSummaries) {
        return taskSummaries.stream().map(this::toTaskSummary).toList();
    }

    private TaskSummary toTaskSummary(CrawlSchedulerTaskSummaryDto dto) {
        Instant completedAt =
                COMPLETED_STATUSES.contains(dto.status()) ? toInstant(dto.updatedAt()) : null;

        return new TaskSummary(
                dto.taskId(),
                dto.status(),
                dto.taskType(),
                toInstant(dto.createdAt()),
                completedAt);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
