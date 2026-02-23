package com.ryuqq.crawlinghub.application.monitoring.dto.composite;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult.SystemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * DashboardSummaryResult 단위 테스트
 *
 * <p>대시보드 요약 결과 생성 및 enum 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("DashboardSummaryResult 테스트")
class DashboardSummaryResultTest {

    @Nested
    @DisplayName("생성 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] HEALTHY 상태 대시보드 결과 생성")
        void shouldCreateHealthyDashboard() {
            // When
            DashboardSummaryResult result =
                    new DashboardSummaryResult(5L, 3L, 2L, 0L, SystemStatus.HEALTHY);

            // Then
            assertThat(result.activeSchedulers()).isEqualTo(5L);
            assertThat(result.runningTasks()).isEqualTo(3L);
            assertThat(result.pendingOutbox()).isEqualTo(2L);
            assertThat(result.recentErrors()).isZero();
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.HEALTHY);
        }

        @Test
        @DisplayName("[성공] WARNING 상태 대시보드 결과 생성")
        void shouldCreateWarningDashboard() {
            // When
            DashboardSummaryResult result =
                    new DashboardSummaryResult(10L, 8L, 15L, 5L, SystemStatus.WARNING);

            // Then
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.WARNING);
            assertThat(result.recentErrors()).isEqualTo(5L);
        }

        @Test
        @DisplayName("[성공] CRITICAL 상태 대시보드 결과 생성")
        void shouldCreateCriticalDashboard() {
            // When
            DashboardSummaryResult result =
                    new DashboardSummaryResult(0L, 0L, 100L, 50L, SystemStatus.CRITICAL);

            // Then
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.CRITICAL);
        }
    }

    @Nested
    @DisplayName("SystemStatus enum 테스트")
    class SystemStatusEnum {

        @Test
        @DisplayName("[성공] HEALTHY, WARNING, CRITICAL 세 가지 상태 존재")
        void shouldHaveThreeStatusValues() {
            // Then
            assertThat(SystemStatus.values())
                    .containsExactlyInAnyOrder(
                            SystemStatus.HEALTHY, SystemStatus.WARNING, SystemStatus.CRITICAL);
        }
    }
}
