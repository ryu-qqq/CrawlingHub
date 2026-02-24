package com.ryuqq.crawlinghub.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetCrawlTaskSummaryService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MonitoringCompositeQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawlTaskSummaryService 테스트")
class GetCrawlTaskSummaryServiceTest {

    @Mock private MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    @InjectMocks private GetCrawlTaskSummaryService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 요약 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조회 기간으로 크롤 태스크 요약 정보 반환")
        void shouldReturnCrawlTaskSummaryForLookback() {
            // Given
            Duration lookback = Duration.ofHours(1);
            Map<String, Long> countsByStatus =
                    Map.of("WAITING", 10L, "RUNNING", 5L, "SUCCESS", 100L, "FAILED", 3L);
            CrawlTaskSummaryResult expected = new CrawlTaskSummaryResult(countsByStatus, 2L, 118L);
            given(monitoringCompositeQueryPort.getCrawlTaskSummary(lookback)).willReturn(expected);

            // When
            CrawlTaskSummaryResult result = service.execute(lookback);

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result.totalTasks()).isEqualTo(118L);
            assertThat(result.stuckTasks()).isEqualTo(2L);
            assertThat(result.countsByStatus()).containsKey("RUNNING");
            then(monitoringCompositeQueryPort).should().getCrawlTaskSummary(lookback);
        }

        @Test
        @DisplayName("[성공] 모든 태스크 성공 상태의 요약 반환")
        void shouldReturnSummaryWithAllSuccessfulTasks() {
            // Given
            Duration lookback = Duration.ofDays(1);
            Map<String, Long> countsByStatus = Map.of("SUCCESS", 500L);
            CrawlTaskSummaryResult expected = new CrawlTaskSummaryResult(countsByStatus, 0L, 500L);
            given(monitoringCompositeQueryPort.getCrawlTaskSummary(lookback)).willReturn(expected);

            // When
            CrawlTaskSummaryResult result = service.execute(lookback);

            // Then
            assertThat(result.stuckTasks()).isZero();
            assertThat(result.totalTasks()).isEqualTo(500L);
        }
    }
}
