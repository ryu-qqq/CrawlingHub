package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskStatisticsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskStatisticsResponse;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskQueryFactory;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetTaskStatisticsUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Task 통계 조회 Service
 *
 * <p>GetTaskStatisticsUseCase 구현체
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetTaskStatisticsService implements GetTaskStatisticsUseCase {

    private static final int TOP_ERRORS_LIMIT = 5;

    private final CrawlTaskReadManager taskReadManager;
    private final CrawlExecutionReadManager executionReadManager;
    private final CrawlTaskQueryFactory queryFactory;

    public GetTaskStatisticsService(
            CrawlTaskReadManager taskReadManager,
            CrawlExecutionReadManager executionReadManager,
            CrawlTaskQueryFactory queryFactory) {
        this.taskReadManager = taskReadManager;
        this.executionReadManager = executionReadManager;
        this.queryFactory = queryFactory;
    }

    @Override
    public TaskStatisticsResponse execute(GetTaskStatisticsQuery query) {
        // 1. Query → Criteria 변환
        CrawlTaskStatisticsCriteria taskCriteria = queryFactory.createStatisticsCriteria(query);
        CrawlExecutionStatisticsCriteria executionCriteria =
                queryFactory.createExecutionStatisticsCriteria(query);

        // 2. 상태별 카운트 조회
        Map<CrawlTaskStatus, Long> statusCounts = taskReadManager.countByStatus(taskCriteria);

        // 3. 태스크 유형별 통계 조회
        Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> taskTypeCounts =
                taskReadManager.countByTaskType(taskCriteria);

        // 4. 상위 에러 조회
        List<CrawlExecutionQueryPort.ErrorCount> topErrors =
                executionReadManager.getTopErrors(executionCriteria, TOP_ERRORS_LIMIT);

        // 5. Response 생성
        return buildResponse(query, statusCounts, taskTypeCounts, topErrors);
    }

    private TaskStatisticsResponse buildResponse(
            GetTaskStatisticsQuery query,
            Map<CrawlTaskStatus, Long> statusCounts,
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> taskTypeCounts,
            List<CrawlExecutionQueryPort.ErrorCount> topErrors) {

        // 기간 정보
        TaskStatisticsResponse.PeriodInfo periodInfo = buildPeriodInfo(query);

        // 요약 정보
        TaskStatisticsResponse.SummaryInfo summaryInfo = buildSummaryInfo(statusCounts);

        // 실패 분석 정보
        TaskStatisticsResponse.FailureAnalysis failureAnalysis =
                buildFailureAnalysis(topErrors, statusCounts);

        // 태스크 유형별 통계
        Map<String, TaskStatisticsResponse.TaskTypeStats> byTaskType =
                buildByTaskType(taskTypeCounts);

        return new TaskStatisticsResponse(periodInfo, summaryInfo, failureAnalysis, byTaskType);
    }

    private TaskStatisticsResponse.PeriodInfo buildPeriodInfo(GetTaskStatisticsQuery query) {
        Instant from =
                query.from() != null
                        ? query.from().atZone(java.time.ZoneId.systemDefault()).toInstant()
                        : null;
        Instant to =
                query.to() != null
                        ? query.to().atZone(java.time.ZoneId.systemDefault()).toInstant()
                        : null;
        return new TaskStatisticsResponse.PeriodInfo(from, to);
    }

    private TaskStatisticsResponse.SummaryInfo buildSummaryInfo(
            Map<CrawlTaskStatus, Long> statusCounts) {
        // 전체 개수
        long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

        // 상태별 개수 (String 키로 변환)
        Map<String, Long> byStatus = new HashMap<>();
        for (Map.Entry<CrawlTaskStatus, Long> entry : statusCounts.entrySet()) {
            byStatus.put(entry.getKey().name(), entry.getValue());
        }

        // 성공률 계산
        long successCount = statusCounts.getOrDefault(CrawlTaskStatus.SUCCESS, 0L);
        double successRate = total > 0 ? ((double) successCount / total) * 100 : 0.0;

        return new TaskStatisticsResponse.SummaryInfo(total, byStatus, successRate);
    }

    private TaskStatisticsResponse.FailureAnalysis buildFailureAnalysis(
            List<CrawlExecutionQueryPort.ErrorCount> topErrors,
            Map<CrawlTaskStatus, Long> statusCounts) {
        // 전체 실패 개수
        long totalFailed = statusCounts.getOrDefault(CrawlTaskStatus.FAILED, 0L);

        // 에러 요약 목록 생성
        List<TaskStatisticsResponse.ErrorSummary> errorSummaries =
                topErrors.stream()
                        .map(
                                error -> {
                                    double percentage =
                                            totalFailed > 0
                                                    ? ((double) error.count() / totalFailed) * 100
                                                    : 0.0;
                                    return new TaskStatisticsResponse.ErrorSummary(
                                            error.errorMessage(), error.count(), percentage);
                                })
                        .collect(Collectors.toList());

        return new TaskStatisticsResponse.FailureAnalysis(errorSummaries);
    }

    private Map<String, TaskStatisticsResponse.TaskTypeStats> buildByTaskType(
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> taskTypeCounts) {
        Map<String, TaskStatisticsResponse.TaskTypeStats> result = new HashMap<>();
        for (Map.Entry<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> entry :
                taskTypeCounts.entrySet()) {
            CrawlTaskQueryPort.TaskTypeCount count = entry.getValue();
            result.put(
                    entry.getKey().name(),
                    new TaskStatisticsResponse.TaskTypeStats(
                            count.total(), count.success(), count.failed()));
        }
        return result;
    }
}
