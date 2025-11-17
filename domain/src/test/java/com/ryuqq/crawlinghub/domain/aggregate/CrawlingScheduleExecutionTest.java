package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.execution.CrawlingScheduleExecution;
import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.fixture.CrawlingScheduleExecutionFixture;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

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
        ScheduleId scheduleId = ScheduleId.generate();
        SellerId sellerId = new SellerId("seller_12345");

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
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("RUNNING 상태에서만 완료할 수 있습니다");
    }
}
