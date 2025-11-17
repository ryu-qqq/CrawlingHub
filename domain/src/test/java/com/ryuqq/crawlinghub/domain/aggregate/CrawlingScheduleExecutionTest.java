package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.execution.CrawlingScheduleExecution;
import com.ryuqq.crawlinghub.domain.crawler.exception.CrawlingScheduleExecutionInvalidStateException;
import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionId;
import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.fixture.CrawlingScheduleExecutionFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CrawlingScheduleExecution Aggregate Root 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ CrawlingScheduleExecution 생성 (PENDING 상태)</li>
 *   <li>✅ 초기 작업 카운터 (totalTasksCreated, completedTasks, failedTasks)</li>
 *   <li>✅ 진행률 계산 (getProgressRate - Tell Don't Ask)</li>
 *   <li>✅ 성공률 계산 (getSuccessRate - Tell Don't Ask)</li>
 *   <li>✅ 실행 완료 (RUNNING → COMPLETED)</li>
 *   <li>✅ 실행 실패 (RUNNING → FAILED)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-17
 */
class CrawlingScheduleExecutionTest {

    @Test
    void shouldCreateExecutionWithPendingStatus() {
        // Given
        ScheduleId scheduleId = ScheduleId.forNew();
        SellerId sellerId = new SellerId(1L);

        // When
        CrawlingScheduleExecution execution = CrawlingScheduleExecution.create(scheduleId, sellerId);

        // Then
        assertThat(execution.getExecutionId()).isNotNull();
        assertThat(execution.getScheduleId()).isEqualTo(scheduleId);
        assertThat(execution.getSellerId()).isEqualTo(sellerId);
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(execution.getTotalTasksCreated()).isEqualTo(0);
        assertThat(execution.getCompletedTasks()).isEqualTo(0);
        assertThat(execution.getFailedTasks()).isEqualTo(0);
    }

    @Test
    void shouldCalculateProgressRate() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.pendingExecution();
        execution.start(100);  // 총 100개 작업 생성
        execution.completeTask();
        execution.completeTask();
        execution.completeTask();  // 3개 완료

        // When
        double progressRate = execution.getProgressRate();

        // Then
        assertThat(progressRate).isEqualTo(3.0);  // 3/100 * 100 = 3%
    }

    @Test
    void shouldCalculateSuccessRate() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.pendingExecution();
        execution.start(100);
        execution.completeTask();
        execution.completeTask();
        execution.failTask();
        execution.failTask();  // 2 성공, 2 실패

        // When
        double successRate = execution.getSuccessRate();

        // Then
        assertThat(successRate).isEqualTo(50.0);  // 2/(2+2) * 100 = 50%
    }

    @Test
    void shouldReturnZeroWhenNoTasksCompleted() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.pendingExecution();
        execution.start(100);

        // When & Then
        assertThat(execution.getProgressRate()).isEqualTo(0.0);
        assertThat(execution.getSuccessRate()).isEqualTo(0.0);
    }

    @Test
    void shouldCompleteExecution() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.runningExecution();

        // When
        execution.complete();

        // Then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldFailExecution() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.runningExecution();

        // When
        execution.fail();

        // Then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenCompletingNonRunningExecution() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.pendingExecution();

        // When & Then
        assertThatThrownBy(() -> execution.complete())
            .isInstanceOf(CrawlingScheduleExecutionInvalidStateException.class)
            .hasMessageContaining("Cannot complete execution")
            .hasMessageContaining("PENDING");
    }

    // ===== reconstitute() 메서드 테스트 =====

    @Test
    void shouldReconstitutePendingExecution() {
        // Given
        ExecutionId executionId = ExecutionId.forNew();
        ScheduleId scheduleId = ScheduleId.forNew();
        SellerId sellerId = new SellerId(1L);
        LocalDateTime now = LocalDateTime.now();

        // When
        CrawlingScheduleExecution execution = CrawlingScheduleExecution.reconstitute(
            executionId,
            scheduleId,
            sellerId,
            ExecutionStatus.PENDING,
            0,
            0,
            0,
            null,
            null,
            now
        );

        // Then
        assertThat(execution.getScheduleId()).isEqualTo(scheduleId);
        assertThat(execution.getSellerId()).isEqualTo(sellerId);
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.PENDING);
        assertThat(execution.getTotalTasksCreated()).isEqualTo(0);
        assertThat(execution.getCompletedTasks()).isEqualTo(0);
        assertThat(execution.getFailedTasks()).isEqualTo(0);
        assertThat(execution.getStartedAt()).isNull();
        assertThat(execution.getCompletedAt()).isNull();
    }

    @Test
    void shouldReconstituteRunningExecution() {
        // Given
        ExecutionId executionId = ExecutionId.forNew();
        ScheduleId scheduleId = ScheduleId.forNew();
        SellerId sellerId = new SellerId(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusMinutes(10);

        // When
        CrawlingScheduleExecution execution = CrawlingScheduleExecution.reconstitute(
            executionId,
            scheduleId,
            sellerId,
            ExecutionStatus.RUNNING,
            100,
            30,
            5,
            startedAt,
            null,
            now
        );

        // Then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.RUNNING);
        assertThat(execution.getTotalTasksCreated()).isEqualTo(100);
        assertThat(execution.getCompletedTasks()).isEqualTo(30);
        assertThat(execution.getFailedTasks()).isEqualTo(5);
        assertThat(execution.getProgressRate()).isEqualTo(35.0);  // (30 + 5) / 100 * 100
        assertThat(execution.getSuccessRate()).isEqualTo((30.0 / 35.0) * 100);  // 30 / 35 * 100
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNull();
    }

    @Test
    void shouldReconstituteCompletedExecution() {
        // Given
        ExecutionId executionId = ExecutionId.forNew();
        ScheduleId scheduleId = ScheduleId.forNew();
        SellerId sellerId = new SellerId(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusMinutes(30);
        LocalDateTime completedAt = now.minusMinutes(5);

        // When
        CrawlingScheduleExecution execution = CrawlingScheduleExecution.reconstitute(
            executionId,
            scheduleId,
            sellerId,
            ExecutionStatus.COMPLETED,
            200,
            180,
            20,
            startedAt,
            completedAt,
            now
        );

        // Then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
        assertThat(execution.getTotalTasksCreated()).isEqualTo(200);
        assertThat(execution.getCompletedTasks()).isEqualTo(180);
        assertThat(execution.getFailedTasks()).isEqualTo(20);
        assertThat(execution.getProgressRate()).isEqualTo(100.0);  // (180 + 20) / 200 * 100
        assertThat(execution.getSuccessRate()).isEqualTo(90.0);  // 180 / 200 * 100
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    @Test
    void shouldReconstituteFailedExecution() {
        // Given
        ExecutionId executionId = ExecutionId.forNew();
        ScheduleId scheduleId = ScheduleId.forNew();
        SellerId sellerId = new SellerId(1L);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = now.minusMinutes(30);
        LocalDateTime completedAt = now.minusMinutes(5);

        // When
        CrawlingScheduleExecution execution = CrawlingScheduleExecution.reconstitute(
            executionId,
            scheduleId,
            sellerId,
            ExecutionStatus.FAILED,
            50,
            10,
            40,
            startedAt,
            completedAt,
            now
        );

        // Then
        assertThat(execution.getStatus()).isEqualTo(ExecutionStatus.FAILED);
        assertThat(execution.getTotalTasksCreated()).isEqualTo(50);
        assertThat(execution.getCompletedTasks()).isEqualTo(10);
        assertThat(execution.getFailedTasks()).isEqualTo(40);
        assertThat(execution.getProgressRate()).isEqualTo(100.0);  // (10 + 40) / 50 * 100
        assertThat(execution.getSuccessRate()).isEqualTo(20.0);  // 10 / 50 * 100
        assertThat(execution.getStartedAt()).isNotNull();
        assertThat(execution.getCompletedAt()).isNotNull();
    }

    // ===== 예외 케이스 테스트 =====

    @Test
    void shouldThrowExceptionWhenStartingNonPendingExecution() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.runningExecution();

        // When & Then
        assertThatThrownBy(() -> execution.start(100))
            .isInstanceOf(CrawlingScheduleExecutionInvalidStateException.class)
            .hasMessageContaining("Cannot start execution")
            .hasMessageContaining("RUNNING");
    }

    @Test
    void shouldThrowExceptionWhenFailingNonRunningExecution() {
        // Given
        CrawlingScheduleExecution execution = CrawlingScheduleExecutionFixture.pendingExecution();

        // When & Then
        assertThatThrownBy(() -> execution.fail())
            .isInstanceOf(CrawlingScheduleExecutionInvalidStateException.class)
            .hasMessageContaining("Cannot fail execution")
            .hasMessageContaining("PENDING");
    }
}
