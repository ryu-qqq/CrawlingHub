package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskWithExecutionsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskWithExecutionsResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskWithExecutionsResponse.ExecutionHistoryItem;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskWithExecutionsResponse.TaskInfo;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetTaskWithExecutionsUseCase;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Task 상세 + Execution 이력 조회 Service
 *
 * <p>어드민용 Task 상세 조회 서비스입니다. Task 정보와 최근 실행 이력을 함께 반환합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetTaskWithExecutionsService implements GetTaskWithExecutionsUseCase {

    private final CrawlTaskReadManager crawlTaskReadManager;
    private final CrawlExecutionReadManager crawlExecutionReadManager;

    public GetTaskWithExecutionsService(
            CrawlTaskReadManager crawlTaskReadManager,
            CrawlExecutionReadManager crawlExecutionReadManager) {
        this.crawlTaskReadManager = crawlTaskReadManager;
        this.crawlExecutionReadManager = crawlExecutionReadManager;
    }

    @Override
    public TaskWithExecutionsResponse execute(GetTaskWithExecutionsQuery query) {
        // 1. Task 조회
        CrawlTaskId taskId = CrawlTaskId.of(query.crawlTaskId());
        CrawlTask task =
                crawlTaskReadManager
                        .findById(taskId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Task를 찾을 수 없습니다. ID: " + query.crawlTaskId()));

        // 2. Execution 이력 조회
        CrawlExecutionCriteria executionCriteria =
                CrawlExecutionCriteria.byTaskId(taskId, 0, query.executionLimit());
        List<CrawlExecution> executions =
                crawlExecutionReadManager.findByCriteria(executionCriteria);

        // 3. Response 조립
        return assembleResponse(task, executions);
    }

    private TaskWithExecutionsResponse assembleResponse(
            CrawlTask task, List<CrawlExecution> executions) {
        TaskInfo taskInfo = toTaskInfo(task);
        List<ExecutionHistoryItem> executionHistory =
                executions.stream().map(this::toExecutionHistoryItem).toList();

        return new TaskWithExecutionsResponse(taskInfo, executionHistory);
    }

    private TaskInfo toTaskInfo(CrawlTask task) {
        return new TaskInfo(
                task.getIdValue(),
                task.getCrawlSchedulerIdValue(),
                task.getSellerIdValue(),
                task.getStatus().name(),
                task.getTaskType().name(),
                task.getRetryCount().value(),
                task.getEndpoint().baseUrl(),
                task.getEndpoint().path(),
                task.getEndpoint().toFullUrl(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    private ExecutionHistoryItem toExecutionHistoryItem(CrawlExecution execution) {
        return new ExecutionHistoryItem(
                execution.getId().value(),
                execution.getStatus(),
                execution.getResult() != null ? execution.getResult().httpStatusCode() : null,
                execution.getResult() != null ? execution.getResult().errorMessage() : null,
                execution.getDuration() != null ? execution.getDuration().durationMs() : null,
                execution.getDuration() != null ? execution.getDuration().startedAt() : null,
                execution.getDuration() != null ? execution.getDuration().completedAt() : null);
    }
}
