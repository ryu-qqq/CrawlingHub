package com.ryuqq.crawlinghub.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult.SystemHealth;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetExternalSystemHealthService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MonitoringCompositeQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetExternalSystemHealthService 테스트")
class GetExternalSystemHealthServiceTest {

    @Mock private MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    @InjectMocks private GetExternalSystemHealthService service;

    @Nested
    @DisplayName("execute() 외부 시스템 상태 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 외부 시스템 상태 목록 반환")
        void shouldReturnExternalSystemHealthList() {
            // Given
            Duration lookback = Duration.ofHours(1);
            List<SystemHealth> healthList =
                    List.of(
                            new SystemHealth("EventBridge", 0L, "HEALTHY"),
                            new SystemHealth("SQS", 2L, "WARNING"),
                            new SystemHealth("S3", 0L, "HEALTHY"));
            ExternalSystemHealthResult expected = new ExternalSystemHealthResult(healthList);
            given(monitoringCompositeQueryPort.getExternalSystemHealth(lookback))
                    .willReturn(expected);

            // When
            ExternalSystemHealthResult result = service.execute(lookback);

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result.systems()).hasSize(3);
            assertThat(result.systems().get(0).system()).isEqualTo("EventBridge");
            assertThat(result.systems().get(0).status()).isEqualTo("HEALTHY");
            assertThat(result.systems().get(1).recentFailures()).isEqualTo(2L);
            then(monitoringCompositeQueryPort).should().getExternalSystemHealth(lookback);
        }

        @Test
        @DisplayName("[성공] 외부 시스템 없을 때 빈 목록 반환")
        void shouldReturnEmptyListWhenNoSystems() {
            // Given
            Duration lookback = Duration.ofMinutes(30);
            ExternalSystemHealthResult expected = new ExternalSystemHealthResult(List.of());
            given(monitoringCompositeQueryPort.getExternalSystemHealth(lookback))
                    .willReturn(expected);

            // When
            ExternalSystemHealthResult result = service.execute(lookback);

            // Then
            assertThat(result.systems()).isEmpty();
        }
    }
}
