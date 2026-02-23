package com.ryuqq.crawlinghub.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult.SystemStatus;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetDashboardSummaryService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MonitoringCompositeQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetDashboardSummaryService 테스트")
class GetDashboardSummaryServiceTest {

    @Mock private MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    @InjectMocks private GetDashboardSummaryService service;

    @Nested
    @DisplayName("execute() 대시보드 요약 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조회 기간으로 대시보드 요약 정보 반환")
        void shouldReturnDashboardSummaryForLookback() {
            // Given
            Duration lookback = Duration.ofHours(1);
            DashboardSummaryResult expected =
                    new DashboardSummaryResult(5L, 3L, 2L, 0L, SystemStatus.HEALTHY);
            given(monitoringCompositeQueryPort.getDashboardSummary(lookback)).willReturn(expected);

            // When
            DashboardSummaryResult result = service.execute(lookback);

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result.activeSchedulers()).isEqualTo(5L);
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.HEALTHY);
            then(monitoringCompositeQueryPort).should().getDashboardSummary(lookback);
        }

        @Test
        @DisplayName("[성공] 경고 상태 대시보드 반환")
        void shouldReturnWarningDashboard() {
            // Given
            Duration lookback = Duration.ofHours(24);
            DashboardSummaryResult expected =
                    new DashboardSummaryResult(10L, 8L, 5L, 3L, SystemStatus.WARNING);
            given(monitoringCompositeQueryPort.getDashboardSummary(lookback)).willReturn(expected);

            // When
            DashboardSummaryResult result = service.execute(lookback);

            // Then
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.WARNING);
            assertThat(result.recentErrors()).isEqualTo(3L);
        }
    }
}
