package com.ryuqq.crawlinghub.domain.execution;

import com.ryuqq.crawlinghub.domain.common.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.schedule.ScheduleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlExecutionTest {

    @Test
    @DisplayName("실행 생성 시 필수 값 검증")
    void shouldCreateExecutionWithRequiredFields() {
        // given
        ScheduleId scheduleId = ScheduleId.of(1L);
        String executionName = "Product Crawl Execution 2025-10-14";

        // when
        CrawlExecution execution = CrawlExecution.create(scheduleId, executionName);

        // then
        assertThat(execution.getScheduleId()).isEqualTo(scheduleId);
        assertThat(execution.getExecutionName()).isEqualTo(executionName);
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(execution.getExecutionId()).isNull();
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
        assertThat(execution.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Schedule ID가 null이면 예외 발생")
    void shouldThrowExceptionWhenScheduleIdIsNull() {
        // given
        ScheduleId scheduleId = null;
        String executionName = "Test Execution";

        // when & then
        assertThatThrownBy(() -> CrawlExecution.create(scheduleId, executionName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule ID cannot be null");
    }

    @Test
    @DisplayName("실행 이름이 null이면 예외 발생")
    void shouldThrowExceptionWhenExecutionNameIsNull() {
        // given
        ScheduleId scheduleId = ScheduleId.of(1L);
        String executionName = null;

        // when & then
        assertThatThrownBy(() -> CrawlExecution.create(scheduleId, executionName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Execution name cannot be null or blank");
    }

    @Test
    @DisplayName("PENDING 상태에서 실행 시작 가능")
    void shouldStartExecutionFromPendingState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );

        // when
        execution.start();

        // then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(execution.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 시작하면 예외 발생")
    void shouldThrowExceptionWhenStartingFromNonPendingState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );
        execution.start();

        // when & then
        assertThatThrownBy(() -> execution.start())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot start execution in RUNNING state");
    }

    @Test
    @DisplayName("RUNNING 상태에서 실행 완료 가능")
    void shouldCompleteExecutionFromRunningState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );
        execution.start();

        // when
        execution.complete();

        // then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("RUNNING이 아닌 상태에서 완료하면 예외 발생")
    void shouldThrowExceptionWhenCompletingFromNonRunningState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );

        // when & then
        assertThatThrownBy(() -> execution.complete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete execution in PENDING state");
    }

    @Test
    @DisplayName("RUNNING 상태에서 실행 실패 가능")
    void shouldFailExecutionFromRunningState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );
        execution.start();
        String errorMessage = "Connection timeout";

        // when
        execution.fail(errorMessage);

        // then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getCompletedAt()).isNotNull();
        assertThat(execution.getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("RUNNING이 아닌 상태에서 실패 처리하면 예외 발생")
    void shouldThrowExceptionWhenFailingFromNonRunningState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );

        // when & then
        assertThatThrownBy(() -> execution.fail("Error"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail execution in PENDING state");
    }

    @Test
    @DisplayName("PENDING 또는 RUNNING 상태에서 취소 가능")
    void shouldCancelExecutionFromPendingOrRunningState() {
        // given - PENDING 상태
        CrawlExecution pendingExecution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution 1"
        );

        // when
        pendingExecution.cancel();

        // then
        assertThat(pendingExecution.getStatus()).isEqualTo(ExecutionStatus.CANCELLED);
        assertThat(pendingExecution.getCompletedAt()).isNotNull();

        // given - RUNNING 상태
        CrawlExecution runningExecution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution 2"
        );
        runningExecution.start();

        // when
        runningExecution.cancel();

        // then
        assertThat(runningExecution.getStatus()).isEqualTo(ExecutionStatus.CANCELLED);
    }

    @Test
    @DisplayName("COMPLETED 상태에서 취소하면 예외 발생")
    void shouldThrowExceptionWhenCancellingFromCompletedState() {
        // given
        CrawlExecution execution = CrawlExecution.create(
                ScheduleId.of(1L),
                "Test Execution"
        );
        execution.start();
        execution.complete();

        // when & then
        assertThatThrownBy(() -> execution.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel execution in COMPLETED state");
    }

    @Test
    @DisplayName("reconstitute로 DB에서 실행 복원 가능")
    void shouldReconstituteExecutionFromDatabase() {
        // given
        ExecutionId executionId = ExecutionId.of(100L);
        ScheduleId scheduleId = ScheduleId.of(1L);
        String executionName = "Existing Execution";
        ExecutionStatus status = ExecutionStatus.COMPLETED;

        ExecutionReconstituteParams params = new ExecutionReconstituteParams(
                executionId, scheduleId, executionName, status,
                null, null, null
        );

        // when
        CrawlExecution execution = CrawlExecution.reconstitute(params);

        // then
        assertThat(execution.getExecutionId()).isEqualTo(executionId);
        assertThat(execution.getScheduleId()).isEqualTo(scheduleId);
        assertThat(execution.getExecutionName()).isEqualTo(executionName);
        assertThat(execution.getStatus()).isEqualTo(status);
    }

}
