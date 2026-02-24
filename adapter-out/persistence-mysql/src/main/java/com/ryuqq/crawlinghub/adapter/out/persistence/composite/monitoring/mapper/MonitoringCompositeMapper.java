package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.mapper;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.DashboardCountsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.OutboxStatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.StatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.SystemFailureCountDto;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult.SystemStatus;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult.SystemHealth;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult.OutboxDetail;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MonitoringCompositeMapper {

    private static final long ERROR_THRESHOLD_CRITICAL = 100;
    private static final long ERROR_THRESHOLD_WARNING = 10;
    private static final long FAILURE_THRESHOLD_CRITICAL = 50;
    private static final long FAILURE_THRESHOLD_WARNING = 5;

    public DashboardSummaryResult toDashboardSummaryResult(DashboardCountsDto dto) {
        SystemStatus status = determineSystemStatus(dto.recentErrors());
        return new DashboardSummaryResult(
                dto.activeSchedulers(),
                dto.runningTasks(),
                dto.pendingOutbox(),
                dto.recentErrors(),
                status);
    }

    public CrawlTaskSummaryResult toCrawlTaskSummaryResult(
            List<StatusCountDto> statusCounts, long stuckTasks) {
        Map<String, Long> countsByStatus = toStatusMap(statusCounts);
        long totalTasks = statusCounts.stream().mapToLong(StatusCountDto::count).sum();
        return new CrawlTaskSummaryResult(countsByStatus, stuckTasks, totalTasks);
    }

    public OutboxSummaryResult toOutboxSummaryResult(List<OutboxStatusCountDto> outboxCounts) {
        OutboxDetail crawlTaskOutbox = buildOutboxDetail(outboxCounts, "CRAWL_TASK");
        OutboxDetail schedulerOutbox = buildOutboxDetail(outboxCounts, "SCHEDULER");
        OutboxDetail productSyncOutbox = buildOutboxDetail(outboxCounts, "PRODUCT_SYNC");
        return new OutboxSummaryResult(crawlTaskOutbox, schedulerOutbox, productSyncOutbox);
    }

    public CrawledRawSummaryResult toCrawledRawSummaryResult(List<StatusCountDto> statusCounts) {
        Map<String, Long> countsByStatus = toStatusMap(statusCounts);
        long totalRaw = statusCounts.stream().mapToLong(StatusCountDto::count).sum();
        return new CrawledRawSummaryResult(countsByStatus, totalRaw);
    }

    public ExternalSystemHealthResult toExternalSystemHealthResult(
            List<SystemFailureCountDto> failureCounts) {
        List<SystemHealth> systems =
                failureCounts.stream()
                        .map(
                                dto ->
                                        new SystemHealth(
                                                dto.systemType(),
                                                dto.failureCount(),
                                                determineHealthStatus(dto.failureCount())))
                        .toList();
        return new ExternalSystemHealthResult(systems);
    }

    private SystemStatus determineSystemStatus(long recentErrors) {
        if (recentErrors >= ERROR_THRESHOLD_CRITICAL) {
            return SystemStatus.CRITICAL;
        }
        if (recentErrors >= ERROR_THRESHOLD_WARNING) {
            return SystemStatus.WARNING;
        }
        return SystemStatus.HEALTHY;
    }

    private String determineHealthStatus(long failureCount) {
        if (failureCount >= FAILURE_THRESHOLD_CRITICAL) {
            return "CRITICAL";
        }
        if (failureCount >= FAILURE_THRESHOLD_WARNING) {
            return "WARNING";
        }
        return "HEALTHY";
    }

    private OutboxDetail buildOutboxDetail(
            List<OutboxStatusCountDto> allCounts, String outboxType) {
        Map<String, Long> countsByStatus =
                allCounts.stream()
                        .filter(dto -> outboxType.equals(dto.outboxType()))
                        .collect(
                                Collectors.toMap(
                                        OutboxStatusCountDto::status,
                                        OutboxStatusCountDto::count,
                                        Long::sum,
                                        LinkedHashMap::new));
        long total = countsByStatus.values().stream().mapToLong(Long::longValue).sum();
        return new OutboxDetail(countsByStatus, total);
    }

    private Map<String, Long> toStatusMap(List<StatusCountDto> statusCounts) {
        return statusCounts.stream()
                .collect(
                        Collectors.toMap(
                                StatusCountDto::status,
                                StatusCountDto::count,
                                Long::sum,
                                LinkedHashMap::new));
    }
}
