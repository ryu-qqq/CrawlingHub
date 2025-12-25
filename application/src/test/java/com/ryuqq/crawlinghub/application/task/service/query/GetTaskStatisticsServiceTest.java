package com.ryuqq.crawlinghub.application.task.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.application.execution.port.out.query.CrawlExecutionQueryPort;
import com.ryuqq.crawlinghub.application.task.dto.query.GetTaskStatisticsQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.TaskStatisticsResponse;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskQueryFactory;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetTaskStatisticsService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetTaskStatisticsService 테스트")
class GetTaskStatisticsServiceTest {

    @Mock private CrawlTaskReadManager taskReadManager;
    @Mock private CrawlExecutionReadManager executionReadManager;
    @Mock private CrawlTaskQueryFactory queryFactory;
    @Mock private CrawlTaskStatisticsCriteria taskCriteria;
    @Mock private CrawlExecutionStatisticsCriteria executionCriteria;

    private GetTaskStatisticsService service;

    @BeforeEach
    void setUp() {
        service = new GetTaskStatisticsService(taskReadManager, executionReadManager, queryFactory);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 통계 조회 성공")
        void shouldReturnStatistics() {
            // Given
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();
            GetTaskStatisticsQuery query = new GetTaskStatisticsQuery(1L, null, from, to);

            Map<CrawlTaskStatus, Long> statusCounts = new HashMap<>();
            statusCounts.put(CrawlTaskStatus.SUCCESS, 80L);
            statusCounts.put(CrawlTaskStatus.FAILED, 20L);

            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> taskTypeCounts = new HashMap<>();
            taskTypeCounts.put(
                    CrawlTaskType.META, new CrawlTaskQueryPort.TaskTypeCount(50L, 45L, 5L));
            taskTypeCounts.put(
                    CrawlTaskType.DETAIL, new CrawlTaskQueryPort.TaskTypeCount(50L, 35L, 15L));

            List<CrawlExecutionQueryPort.ErrorCount> topErrors =
                    List.of(
                            new CrawlExecutionQueryPort.ErrorCount("Timeout Error", 10L),
                            new CrawlExecutionQueryPort.ErrorCount("Connection Refused", 5L));

            given(queryFactory.createStatisticsCriteria(query)).willReturn(taskCriteria);
            given(queryFactory.createExecutionStatisticsCriteria(query))
                    .willReturn(executionCriteria);
            given(taskReadManager.countByStatus(taskCriteria)).willReturn(statusCounts);
            given(taskReadManager.countByTaskType(taskCriteria)).willReturn(taskTypeCounts);
            given(executionReadManager.getTopErrors(executionCriteria, 5)).willReturn(topErrors);

            // When
            TaskStatisticsResponse result = service.execute(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.summary().total()).isEqualTo(100L);
            assertThat(result.summary().successRate()).isEqualTo(80.0);
            assertThat(result.summary().byStatus()).containsEntry("SUCCESS", 80L);
            assertThat(result.summary().byStatus()).containsEntry("FAILED", 20L);
        }

        @Test
        @DisplayName("[성공] 빈 결과 처리")
        void shouldHandleEmptyResults() {
            // Given
            GetTaskStatisticsQuery query = new GetTaskStatisticsQuery(1L, null, null, null);

            Map<CrawlTaskStatus, Long> emptyStatusCounts = new HashMap<>();
            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> emptyTaskTypeCounts =
                    new HashMap<>();
            List<CrawlExecutionQueryPort.ErrorCount> emptyErrors = List.of();

            given(queryFactory.createStatisticsCriteria(query)).willReturn(taskCriteria);
            given(queryFactory.createExecutionStatisticsCriteria(query))
                    .willReturn(executionCriteria);
            given(taskReadManager.countByStatus(taskCriteria)).willReturn(emptyStatusCounts);
            given(taskReadManager.countByTaskType(taskCriteria)).willReturn(emptyTaskTypeCounts);
            given(executionReadManager.getTopErrors(executionCriteria, 5)).willReturn(emptyErrors);

            // When
            TaskStatisticsResponse result = service.execute(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.summary().total()).isZero();
            assertThat(result.summary().successRate()).isZero();
            assertThat(result.byTaskType()).isEmpty();
            assertThat(result.failureAnalysis().topErrors()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 태스크 유형별 통계 계산")
        void shouldCalculateByTaskType() {
            // Given
            GetTaskStatisticsQuery query = new GetTaskStatisticsQuery(1L, null, null, null);

            Map<CrawlTaskStatus, Long> statusCounts = new HashMap<>();
            statusCounts.put(CrawlTaskStatus.SUCCESS, 100L);

            Map<CrawlTaskType, CrawlTaskQueryPort.TaskTypeCount> taskTypeCounts = new HashMap<>();
            taskTypeCounts.put(
                    CrawlTaskType.META, new CrawlTaskQueryPort.TaskTypeCount(50L, 48L, 2L));
            taskTypeCounts.put(
                    CrawlTaskType.DETAIL, new CrawlTaskQueryPort.TaskTypeCount(30L, 28L, 2L));
            taskTypeCounts.put(
                    CrawlTaskType.OPTION, new CrawlTaskQueryPort.TaskTypeCount(20L, 20L, 0L));

            given(queryFactory.createStatisticsCriteria(query)).willReturn(taskCriteria);
            given(queryFactory.createExecutionStatisticsCriteria(query))
                    .willReturn(executionCriteria);
            given(taskReadManager.countByStatus(taskCriteria)).willReturn(statusCounts);
            given(taskReadManager.countByTaskType(taskCriteria)).willReturn(taskTypeCounts);
            given(executionReadManager.getTopErrors(executionCriteria, 5)).willReturn(List.of());

            // When
            TaskStatisticsResponse result = service.execute(query);

            // Then
            assertThat(result.byTaskType()).hasSize(3);
            assertThat(result.byTaskType()).containsKey("META");
            assertThat(result.byTaskType().get("META").total()).isEqualTo(50L);
            assertThat(result.byTaskType().get("META").success()).isEqualTo(48L);
            assertThat(result.byTaskType().get("META").failed()).isEqualTo(2L);
        }

        @Test
        @DisplayName("[성공] 실패 분석 정보 계산")
        void shouldCalculateFailureAnalysis() {
            // Given
            GetTaskStatisticsQuery query = new GetTaskStatisticsQuery(1L, null, null, null);

            Map<CrawlTaskStatus, Long> statusCounts = new HashMap<>();
            statusCounts.put(CrawlTaskStatus.SUCCESS, 80L);
            statusCounts.put(CrawlTaskStatus.FAILED, 20L);

            List<CrawlExecutionQueryPort.ErrorCount> topErrors =
                    List.of(
                            new CrawlExecutionQueryPort.ErrorCount("Timeout", 10L),
                            new CrawlExecutionQueryPort.ErrorCount("Network Error", 6L),
                            new CrawlExecutionQueryPort.ErrorCount("Parse Error", 4L));

            given(queryFactory.createStatisticsCriteria(query)).willReturn(taskCriteria);
            given(queryFactory.createExecutionStatisticsCriteria(query))
                    .willReturn(executionCriteria);
            given(taskReadManager.countByStatus(taskCriteria)).willReturn(statusCounts);
            given(taskReadManager.countByTaskType(taskCriteria)).willReturn(new HashMap<>());
            given(executionReadManager.getTopErrors(executionCriteria, 5)).willReturn(topErrors);

            // When
            TaskStatisticsResponse result = service.execute(query);

            // Then
            assertThat(result.failureAnalysis().topErrors()).hasSize(3);
            assertThat(result.failureAnalysis().topErrors().get(0).error()).isEqualTo("Timeout");
            assertThat(result.failureAnalysis().topErrors().get(0).count()).isEqualTo(10L);
            // 10/20 = 50%
            assertThat(result.failureAnalysis().topErrors().get(0).percentage()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("[성공] 기간 정보 포함")
        void shouldIncludePeriodInfo() {
            // Given
            LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2024, 1, 31, 23, 59);
            GetTaskStatisticsQuery query = new GetTaskStatisticsQuery(1L, null, from, to);

            given(queryFactory.createStatisticsCriteria(query)).willReturn(taskCriteria);
            given(queryFactory.createExecutionStatisticsCriteria(query))
                    .willReturn(executionCriteria);
            given(taskReadManager.countByStatus(taskCriteria)).willReturn(new HashMap<>());
            given(taskReadManager.countByTaskType(taskCriteria)).willReturn(new HashMap<>());
            given(executionReadManager.getTopErrors(executionCriteria, 5)).willReturn(List.of());

            // When
            TaskStatisticsResponse result = service.execute(query);

            // Then
            assertThat(result.period()).isNotNull();
            assertThat(result.period().from()).isNotNull();
            assertThat(result.period().to()).isNotNull();
        }
    }
}
