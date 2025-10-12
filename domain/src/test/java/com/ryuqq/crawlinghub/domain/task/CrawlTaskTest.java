package com.ryuqq.crawlinghub.domain.task;

import com.ryuqq.crawlinghub.domain.common.TaskStatus;
import com.ryuqq.crawlinghub.domain.execution.ExecutionId;
import com.ryuqq.crawlinghub.domain.workflow.StepId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlTaskTest {

    @Test
    @DisplayName("태스크 생성 시 필수 값 검증")
    void shouldCreateTaskWithRequiredFields() {
        // given
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = StepId.of(1L);
        String taskName = "API 호출 태스크";
        int maxRetryCount = 3;

        // when
        CrawlTask task = CrawlTask.create(executionId, stepId, taskName, maxRetryCount);

        // then
        assertThat(task.getExecutionId()).isEqualTo(executionId);
        assertThat(task.getStepId()).isEqualTo(stepId);
        assertThat(task.getTaskName()).isEqualTo(taskName);
        assertThat(task.getMaxRetryCount()).isEqualTo(maxRetryCount);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getRetryCount()).isZero();
        assertThat(task.getParentTaskId()).isNull();
        assertThat(task.getTaskId()).isNull();
    }

    @Test
    @DisplayName("부모 태스크와 함께 태스크 생성 가능")
    void shouldCreateTaskWithParent() {
        // given
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = StepId.of(1L);
        TaskId parentTaskId = TaskId.of(100L);
        String taskName = "자식 태스크";
        int maxRetryCount = 3;

        // when
        CrawlTask task = CrawlTask.createWithParent(executionId, stepId, parentTaskId, taskName, maxRetryCount);

        // then
        assertThat(task.getParentTaskId()).isEqualTo(parentTaskId);
    }

    @Test
    @DisplayName("Execution ID가 null이면 예외 발생")
    void shouldThrowExceptionWhenExecutionIdIsNull() {
        // given
        ExecutionId executionId = null;
        StepId stepId = StepId.of(1L);
        String taskName = "Test Task";
        int maxRetryCount = 3;

        // when & then
        assertThatThrownBy(() -> CrawlTask.create(executionId, stepId, taskName, maxRetryCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Execution ID cannot be null");
    }

    @Test
    @DisplayName("Step ID가 null이면 예외 발생")
    void shouldThrowExceptionWhenStepIdIsNull() {
        // given
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = null;
        String taskName = "Test Task";
        int maxRetryCount = 3;

        // when & then
        assertThatThrownBy(() -> CrawlTask.create(executionId, stepId, taskName, maxRetryCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Step ID cannot be null");
    }

    @Test
    @DisplayName("태스크 이름이 null이면 예외 발생")
    void shouldThrowExceptionWhenTaskNameIsNull() {
        // given
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = StepId.of(1L);
        String taskName = null;
        int maxRetryCount = 3;

        // when & then
        assertThatThrownBy(() -> CrawlTask.create(executionId, stepId, taskName, maxRetryCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task name cannot be null or blank");
    }

    @Test
    @DisplayName("최대 재시도 횟수가 음수면 예외 발생")
    void shouldThrowExceptionWhenMaxRetryCountIsNegative() {
        // given
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = StepId.of(1L);
        String taskName = "Test Task";
        int maxRetryCount = -1;

        // when & then
        assertThatThrownBy(() -> CrawlTask.create(executionId, stepId, taskName, maxRetryCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Max retry count cannot be negative");
    }

    @Test
    @DisplayName("PENDING 상태에서 큐에 추가 가능")
    void shouldEnqueueTaskFromPendingState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );

        // when
        task.enqueue();

        // then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.QUEUED);
        assertThat(task.getQueuedAt()).isNotNull();
    }

    @Test
    @DisplayName("QUEUED 상태에서 태스크 시작 가능")
    void shouldStartTaskFromQueuedState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );
        task.enqueue();

        // when
        task.start();

        // then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.RUNNING);
        assertThat(task.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("RUNNING 상태에서 태스크 완료 가능")
    void shouldCompleteTaskFromRunningState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );
        task.enqueue();
        task.start();

        // when
        task.complete();

        // then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("RUNNING 상태에서 태스크 실패 가능")
    void shouldFailTaskFromRunningState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );
        task.enqueue();
        task.start();
        String errorMessage = "Network error";

        // when
        task.fail(errorMessage);

        // then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(task.getCompletedAt()).isNotNull();
        assertThat(task.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("재시도 가능 여부 확인")
    void shouldCheckIfRetryIsPossible() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );

        // when & then
        assertThat(task.canRetry()).isTrue();
    }

    @Test
    @DisplayName("FAILED 상태에서 재시도 카운트 증가 가능")
    void shouldIncrementRetryCountFromFailedState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );
        task.enqueue();
        task.start();
        task.fail("Error");

        // when
        task.incrementRetry();

        // then
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(TaskStatus.RETRY);
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 시 재시도 불가")
    void shouldThrowExceptionWhenExceedingMaxRetryCount() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                2
        );
        task.enqueue();
        task.start();
        task.fail("Error 1");
        task.incrementRetry();
        task.enqueue();
        task.start();
        task.fail("Error 2");
        task.incrementRetry();
        task.enqueue();
        task.start();
        task.fail("Error 3");

        // when & then
        assertThatThrownBy(() -> task.incrementRetry())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot retry: maximum retry count reached");
    }

    @Test
    @DisplayName("RETRY 상태에서 다시 큐에 추가 가능")
    void shouldEnqueueTaskFromRetryState() {
        // given
        CrawlTask task = CrawlTask.create(
                ExecutionId.of(1L),
                StepId.of(1L),
                "Test Task",
                3
        );
        task.enqueue();
        task.start();
        task.fail("Error");
        task.incrementRetry();

        // when
        task.enqueue();

        // then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.QUEUED);
    }

    @Test
    @DisplayName("reconstitute로 DB에서 태스크 복원 가능")
    void shouldReconstituteTaskFromDatabase() {
        // given
        TaskId taskId = TaskId.of(100L);
        ExecutionId executionId = ExecutionId.of(1L);
        StepId stepId = StepId.of(1L);
        TaskId parentTaskId = TaskId.of(50L);
        String taskName = "Existing Task";
        TaskStatus status = TaskStatus.COMPLETED;
        int retryCount = 2;
        int maxRetryCount = 3;

        TaskReconstituteParams params = new TaskReconstituteParams(
                taskId, executionId, stepId, parentTaskId, taskName,
                status, retryCount, maxRetryCount,
                null, null, null, null
        );

        // when
        CrawlTask task = CrawlTask.reconstitute(params);

        // then
        assertThat(task.getTaskId()).isEqualTo(taskId);
        assertThat(task.getExecutionId()).isEqualTo(executionId);
        assertThat(task.getStepId()).isEqualTo(stepId);
        assertThat(task.getParentTaskId()).isEqualTo(parentTaskId);
        assertThat(task.getTaskName()).isEqualTo(taskName);
        assertThat(task.getStatus()).isEqualTo(status);
        assertThat(task.getRetryCount()).isEqualTo(retryCount);
        assertThat(task.getMaxRetryCount()).isEqualTo(maxRetryCount);
    }

}
