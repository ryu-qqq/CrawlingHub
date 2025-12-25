package com.ryuqq.crawlinghub.application.task.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskWithExecutionsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskWithExecutionsResponse;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionResult;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import com.ryuqq.crawlinghub.domain.execution.vo.ExecutionDuration;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetTaskWithExecutionsService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetTaskWithExecutionsService 테스트")
class GetTaskWithExecutionsServiceTest {

    @Mock private CrawlTaskReadManager crawlTaskReadManager;
    @Mock private CrawlExecutionReadManager crawlExecutionReadManager;
    @Mock private CrawlTask task;
    @Mock private CrawlExecution execution;
    @Mock private CrawlEndpoint endpoint;
    @Mock private RetryCount retryCount;
    @Mock private CrawlExecutionResult executionResult;
    @Mock private ExecutionDuration executionDuration;

    private GetTaskWithExecutionsService service;

    @BeforeEach
    void setUp() {
        service = new GetTaskWithExecutionsService(crawlTaskReadManager, crawlExecutionReadManager);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] Task와 Execution 이력 조회")
        void shouldReturnTaskWithExecutions() {
            // Given
            Long taskId = 1L;
            GetTaskWithExecutionsQuery query = new GetTaskWithExecutionsQuery(taskId, 10);

            given(crawlTaskReadManager.findById(CrawlTaskId.of(taskId)))
                    .willReturn(Optional.of(task));
            given(crawlExecutionReadManager.findByCriteria(any(CrawlExecutionCriteria.class)))
                    .willReturn(List.of(execution));

            // Mock task
            given(task.getIdValue()).willReturn(taskId);
            given(task.getCrawlSchedulerIdValue()).willReturn(100L);
            given(task.getSellerIdValue()).willReturn(200L);
            given(task.getStatus()).willReturn(CrawlTaskStatus.SUCCESS);
            given(task.getTaskType()).willReturn(CrawlTaskType.META);
            given(task.getRetryCount()).willReturn(retryCount);
            given(retryCount.value()).willReturn(0);
            given(task.getEndpoint()).willReturn(endpoint);
            given(endpoint.baseUrl()).willReturn("https://example.com");
            given(endpoint.path()).willReturn("/api/products");
            given(endpoint.toFullUrl()).willReturn("https://example.com/api/products");
            given(task.getCreatedAt()).willReturn(Instant.now());
            given(task.getUpdatedAt()).willReturn(Instant.now());

            // Mock execution
            given(execution.getId())
                    .willReturn(
                            new com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId(
                                    1L));
            given(execution.getStatus()).willReturn(CrawlExecutionStatus.SUCCESS);
            given(execution.getResult()).willReturn(executionResult);
            given(executionResult.httpStatusCode()).willReturn(200);
            given(executionResult.errorMessage()).willReturn(null);
            given(execution.getDuration()).willReturn(executionDuration);
            given(executionDuration.durationMs()).willReturn(150L);
            given(executionDuration.startedAt()).willReturn(Instant.now().minusMillis(150));
            given(executionDuration.completedAt()).willReturn(Instant.now());

            // When
            TaskWithExecutionsResponse result = service.execute(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.task().crawlTaskId()).isEqualTo(taskId);
            assertThat(result.executionHistory()).hasSize(1);
        }

        @Test
        @DisplayName("[실패] Task가 존재하지 않으면 예외 발생")
        void shouldThrowExceptionWhenTaskNotFound() {
            // Given
            Long taskId = 999L;
            GetTaskWithExecutionsQuery query = new GetTaskWithExecutionsQuery(taskId, 10);

            given(crawlTaskReadManager.findById(CrawlTaskId.of(taskId)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(query))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Task를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("[성공] Execution이 없어도 정상 반환")
        void shouldReturnTaskWithEmptyExecutions() {
            // Given
            Long taskId = 1L;
            GetTaskWithExecutionsQuery query = new GetTaskWithExecutionsQuery(taskId, 10);

            given(crawlTaskReadManager.findById(CrawlTaskId.of(taskId)))
                    .willReturn(Optional.of(task));
            given(crawlExecutionReadManager.findByCriteria(any(CrawlExecutionCriteria.class)))
                    .willReturn(List.of());

            // Mock task
            given(task.getIdValue()).willReturn(taskId);
            given(task.getCrawlSchedulerIdValue()).willReturn(100L);
            given(task.getSellerIdValue()).willReturn(200L);
            given(task.getStatus()).willReturn(CrawlTaskStatus.WAITING);
            given(task.getTaskType()).willReturn(CrawlTaskType.META);
            given(task.getRetryCount()).willReturn(retryCount);
            given(retryCount.value()).willReturn(0);
            given(task.getEndpoint()).willReturn(endpoint);
            given(endpoint.baseUrl()).willReturn("https://example.com");
            given(endpoint.path()).willReturn("/api");
            given(endpoint.toFullUrl()).willReturn("https://example.com/api");
            given(task.getCreatedAt()).willReturn(Instant.now());
            given(task.getUpdatedAt()).willReturn(Instant.now());

            // When
            TaskWithExecutionsResponse result = service.execute(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.executionHistory()).isEmpty();
        }

        @Test
        @DisplayName("[성공] null Result/Duration 처리")
        void shouldHandleNullResultAndDuration() {
            // Given
            Long taskId = 1L;
            GetTaskWithExecutionsQuery query = new GetTaskWithExecutionsQuery(taskId, 10);

            given(crawlTaskReadManager.findById(CrawlTaskId.of(taskId)))
                    .willReturn(Optional.of(task));
            given(crawlExecutionReadManager.findByCriteria(any(CrawlExecutionCriteria.class)))
                    .willReturn(List.of(execution));

            // Mock task
            given(task.getIdValue()).willReturn(taskId);
            given(task.getCrawlSchedulerIdValue()).willReturn(100L);
            given(task.getSellerIdValue()).willReturn(200L);
            given(task.getStatus()).willReturn(CrawlTaskStatus.RUNNING);
            given(task.getTaskType()).willReturn(CrawlTaskType.DETAIL);
            given(task.getRetryCount()).willReturn(retryCount);
            given(retryCount.value()).willReturn(1);
            given(task.getEndpoint()).willReturn(endpoint);
            given(endpoint.baseUrl()).willReturn("https://example.com");
            given(endpoint.path()).willReturn("/detail");
            given(endpoint.toFullUrl()).willReturn("https://example.com/detail");
            given(task.getCreatedAt()).willReturn(Instant.now());
            given(task.getUpdatedAt()).willReturn(Instant.now());

            // Mock execution with null result and duration
            given(execution.getId())
                    .willReturn(
                            new com.ryuqq.crawlinghub.domain.execution.identifier.CrawlExecutionId(
                                    2L));
            given(execution.getStatus()).willReturn(CrawlExecutionStatus.RUNNING);
            given(execution.getResult()).willReturn(null);
            given(execution.getDuration()).willReturn(null);

            // When
            TaskWithExecutionsResponse result = service.execute(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.executionHistory()).hasSize(1);
            assertThat(result.executionHistory().get(0).httpStatusCode()).isNull();
            assertThat(result.executionHistory().get(0).errorMessage()).isNull();
            assertThat(result.executionHistory().get(0).durationMs()).isNull();
        }
    }
}
