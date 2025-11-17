package com.ryuqq.crawlinghub.domain.aggregate;

import com.ryuqq.crawlinghub.domain.crawler.aggregate.execution.CrawlingScheduleExecution;
import com.ryuqq.crawlinghub.domain.crawler.vo.ExecutionStatus;
import com.ryuqq.crawlinghub.domain.crawler.vo.ScheduleId;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CrawlingScheduleExecution Aggregate Root 테스트
 *
 * <p><strong>테스트 범위:</strong></p>
 * <ul>
 *   <li>✅ CrawlingScheduleExecution 생성 (PENDING 상태)</li>
 *   <li>✅ 초기 작업 카운터 (totalTasksCreated, completedTasks, failedTasks)</li>
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
}
