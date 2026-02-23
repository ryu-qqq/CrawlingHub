package com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerSchedulerSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.seller.dto.SellerTaskSummaryDto;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult.SchedulerSummary;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult.SellerInfo;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult.SellerStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.composite.SellerDetailResult.TaskSummary;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Seller Composite Mapper
 *
 * <p>Persistence DTO → Application Result 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerCompositeMapper {

    private static final Set<String> COMPLETED_STATUSES = Set.of("SUCCESS", "FAILED", "TIMEOUT");

    /**
     * Persistence DTO들을 SellerDetailResult로 변환
     *
     * @param sellerDto 셀러 기본 정보
     * @param schedulerDtos 연관 스케줄러 목록
     * @param taskSummaryDtos 최근 태스크 목록
     * @param taskStatisticsDtos 태스크 상태별 통계
     * @return Application Layer 결과 DTO
     */
    public SellerDetailResult toResult(
            SellerCompositeDto sellerDto,
            List<SellerSchedulerSummaryDto> schedulerDtos,
            List<SellerTaskSummaryDto> taskSummaryDtos,
            List<SellerTaskStatisticsDto> taskStatisticsDtos) {

        SellerInfo sellerInfo = toSellerInfo(sellerDto);
        List<SchedulerSummary> schedulers = toSchedulerSummaries(schedulerDtos);
        List<TaskSummary> tasks = toTaskSummaries(taskSummaryDtos);
        SellerStatistics statistics = toStatistics(taskStatisticsDtos);

        return new SellerDetailResult(sellerInfo, schedulers, tasks, statistics);
    }

    private SellerInfo toSellerInfo(SellerCompositeDto dto) {
        return new SellerInfo(
                dto.sellerId(),
                dto.mustItSellerName(),
                dto.sellerName(),
                dto.status(),
                dto.productCount(),
                toInstant(dto.createdAt()),
                toInstant(dto.updatedAt()));
    }

    private List<SchedulerSummary> toSchedulerSummaries(List<SellerSchedulerSummaryDto> dtos) {
        return dtos.stream().map(this::toSchedulerSummary).toList();
    }

    private SchedulerSummary toSchedulerSummary(SellerSchedulerSummaryDto dto) {
        return new SchedulerSummary(
                dto.schedulerId(), dto.schedulerName(), dto.status(), dto.cronExpression());
    }

    private List<TaskSummary> toTaskSummaries(List<SellerTaskSummaryDto> dtos) {
        return dtos.stream().map(this::toTaskSummary).toList();
    }

    private TaskSummary toTaskSummary(SellerTaskSummaryDto dto) {
        Instant completedAt =
                COMPLETED_STATUSES.contains(dto.status()) ? toInstant(dto.updatedAt()) : null;

        return new TaskSummary(
                dto.taskId(),
                dto.status(),
                dto.taskType(),
                toInstant(dto.createdAt()),
                completedAt);
    }

    private SellerStatistics toStatistics(List<SellerTaskStatisticsDto> taskStatistics) {
        long totalTasks = taskStatistics.stream().mapToLong(SellerTaskStatisticsDto::count).sum();
        long successTasks =
                taskStatistics.stream()
                        .filter(s -> "SUCCESS".equals(s.status()))
                        .mapToLong(SellerTaskStatisticsDto::count)
                        .sum();
        long failedTasks =
                taskStatistics.stream()
                        .filter(s -> "FAILED".equals(s.status()) || "TIMEOUT".equals(s.status()))
                        .mapToLong(SellerTaskStatisticsDto::count)
                        .sum();
        long pendingTasks = totalTasks - successTasks - failedTasks;
        double successRate = totalTasks > 0 ? (double) successTasks / totalTasks : 0.0;

        return new SellerStatistics(totalTasks, successTasks, pendingTasks, successRate);
    }

    private Instant toInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
