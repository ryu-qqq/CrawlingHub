package com.ryuqq.crawlinghub.application.dashboard.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.DailySuccessRate;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.FailedTaskSummary;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.OutboxStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.ScheduleStats;
import com.ryuqq.crawlinghub.application.dashboard.dto.response.DashboardStatsResponse.TodayTaskStats;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlScheduleQueryPort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerQueryCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatisticsCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetDashboardStatsService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: QueryPort 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetDashboardStatsService 테스트")
class GetDashboardStatsServiceTest {

    @Mock private CrawlTaskQueryPort taskQueryPort;

    @Mock private CrawlScheduleQueryPort scheduleQueryPort;

    @Mock private CrawlTaskOutboxQueryPort outboxQueryPort;

    @InjectMocks private GetDashboardStatsService service;

    @Nested
    @DisplayName("execute() 대시보드 통계 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 전체 통계 정보 조회")
        void shouldReturnDashboardStats() {
            // Given
            Map<CrawlTaskStatus, Long> todayStats =
                    Map.of(
                            CrawlTaskStatus.SUCCESS, 80L,
                            CrawlTaskStatus.FAILED, 10L,
                            CrawlTaskStatus.RUNNING, 5L,
                            CrawlTaskStatus.WAITING, 5L);

            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(todayStats);
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class)))
                    .willReturn(50L, 40L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(10L, 100L, 5L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.todayTaskStats()).isNotNull();
            assertThat(result.weeklySuccessRates()).hasSize(7);
            assertThat(result.scheduleStats()).isNotNull();
            assertThat(result.outboxStats()).isNotNull();
            assertThat(result.recentFailedTasks()).isNotNull();
        }

        @Test
        @DisplayName("[성공] 오늘 태스크 통계 계산 검증")
        void shouldCalculateTodayTaskStatsCorrectly() {
            // Given
            Map<CrawlTaskStatus, Long> todayStats =
                    Map.of(
                            CrawlTaskStatus.SUCCESS, 80L,
                            CrawlTaskStatus.FAILED, 15L,
                            CrawlTaskStatus.TIMEOUT, 5L,
                            CrawlTaskStatus.RUNNING, 10L,
                            CrawlTaskStatus.PUBLISHED, 5L,
                            CrawlTaskStatus.WAITING, 20L,
                            CrawlTaskStatus.RETRY, 5L);

            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(todayStats);
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            TodayTaskStats todayTaskStats = result.todayTaskStats();
            assertThat(todayTaskStats.success()).isEqualTo(80L);
            assertThat(todayTaskStats.failed()).isEqualTo(20L); // FAILED + TIMEOUT
            assertThat(todayTaskStats.inProgress()).isEqualTo(15L); // RUNNING + PUBLISHED
            assertThat(todayTaskStats.waiting()).isEqualTo(25L); // WAITING + RETRY
            assertThat(todayTaskStats.total()).isEqualTo(140L);
        }

        @Test
        @DisplayName("[성공] 성공률 계산 검증")
        void shouldCalculateSuccessRateCorrectly() {
            // Given
            Map<CrawlTaskStatus, Long> stats = Map.of(CrawlTaskStatus.SUCCESS, 80L);

            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(stats);
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            TodayTaskStats todayTaskStats = result.todayTaskStats();
            assertThat(todayTaskStats.successRate()).isEqualTo(1.0); // 80/80 = 100%
        }

        @Test
        @DisplayName("[성공] 태스크가 없는 경우 성공률 0 반환")
        void shouldReturnZeroSuccessRateWhenNoTasks() {
            // Given
            Map<CrawlTaskStatus, Long> emptyStats = Collections.emptyMap();

            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(emptyStats);
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            assertThat(result.todayTaskStats().successRate()).isEqualTo(0.0);
            assertThat(result.todayTaskStats().total()).isZero();
        }

        @Test
        @DisplayName("[성공] 주간 성공률 7일치 반환")
        void shouldReturnWeeklySuccessRates() {
            // Given
            Map<CrawlTaskStatus, Long> dailyStats =
                    Map.of(CrawlTaskStatus.SUCCESS, 50L, CrawlTaskStatus.FAILED, 10L);

            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(dailyStats);
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            List<DailySuccessRate> weeklyRates = result.weeklySuccessRates();
            assertThat(weeklyRates).hasSize(7);
            weeklyRates.forEach(
                    rate -> {
                        assertThat(rate.date()).isNotBlank();
                        assertThat(rate.total()).isGreaterThanOrEqualTo(0);
                    });
        }

        @Test
        @DisplayName("[성공] 스케줄 통계 조회")
        void shouldReturnScheduleStats() {
            // Given
            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(Collections.emptyMap());
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class)))
                    .willReturn(100L, 75L); // total, active
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            ScheduleStats scheduleStats = result.scheduleStats();
            assertThat(scheduleStats.total()).isEqualTo(100L);
            assertThat(scheduleStats.active()).isEqualTo(75L);
            assertThat(scheduleStats.inactive()).isEqualTo(25L);
        }

        @Test
        @DisplayName("[성공] Outbox 통계 조회")
        void shouldReturnOutboxStats() {
            // Given
            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(Collections.emptyMap());
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(50L, 200L, 10L); // pending, sent, failed
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            OutboxStats outboxStats = result.outboxStats();
            assertThat(outboxStats.pending()).isEqualTo(50L);
            assertThat(outboxStats.sent()).isEqualTo(200L);
            assertThat(outboxStats.failed()).isEqualTo(10L);
        }

        @Test
        @DisplayName("[성공] 최근 실패 태스크가 없는 경우 빈 목록 반환")
        void shouldReturnEmptyFailedTasksWhenNone() {
            // Given
            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(Collections.emptyMap());
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            DashboardStatsResponse result = service.execute();

            // Then
            List<FailedTaskSummary> failedTasks = result.recentFailedTasks();
            assertThat(failedTasks).isEmpty();
        }

        @Test
        @DisplayName("[성공] Port 호출 검증")
        void shouldCallAllPorts() {
            // Given
            given(taskQueryPort.countByStatus(any(CrawlTaskStatisticsCriteria.class)))
                    .willReturn(Collections.emptyMap());
            given(scheduleQueryPort.count(any(CrawlSchedulerQueryCriteria.class))).willReturn(0L);
            given(outboxQueryPort.countByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(0L);
            given(taskQueryPort.findByCriteria(any(CrawlTaskCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            service.execute();

            // Then
            then(taskQueryPort).should().findByCriteria(any(CrawlTaskCriteria.class));
            then(scheduleQueryPort)
                    .should(atLeastOnce())
                    .count(any(CrawlSchedulerQueryCriteria.class));
            then(outboxQueryPort)
                    .should(atLeastOnce())
                    .countByCriteria(any(CrawlTaskOutboxCriteria.class));
        }
    }
}
