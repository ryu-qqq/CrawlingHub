package com.ryuqq.crawlinghub.application.monitoring.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.out.query.MonitoringCompositeQueryPort;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * GetCrawledRawSummaryService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MonitoringCompositeQueryPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawledRawSummaryService 테스트")
class GetCrawledRawSummaryServiceTest {

    @Mock private MonitoringCompositeQueryPort monitoringCompositeQueryPort;

    @InjectMocks private GetCrawledRawSummaryService service;

    @Nested
    @DisplayName("execute() 크롤 원시 데이터 요약 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 크롤 원시 데이터 요약 정보 반환")
        void shouldReturnCrawledRawSummary() {
            // Given
            Map<String, Long> countsByStatus =
                    Map.of("PENDING", 50L, "PROCESSED", 300L, "FAILED", 10L);
            CrawledRawSummaryResult expected = new CrawledRawSummaryResult(countsByStatus, 360L);
            given(monitoringCompositeQueryPort.getCrawledRawSummary()).willReturn(expected);

            // When
            CrawledRawSummaryResult result = service.execute();

            // Then
            assertThat(result).isEqualTo(expected);
            assertThat(result.totalRaw()).isEqualTo(360L);
            assertThat(result.countsByStatus()).containsKey("PENDING");
            then(monitoringCompositeQueryPort).should().getCrawledRawSummary();
        }

        @Test
        @DisplayName("[성공] 데이터 없을 때 빈 요약 반환")
        void shouldReturnEmptySummaryWhenNoData() {
            // Given
            Map<String, Long> emptyStatus = Map.of();
            CrawledRawSummaryResult expected = new CrawledRawSummaryResult(emptyStatus, 0L);
            given(monitoringCompositeQueryPort.getCrawledRawSummary()).willReturn(expected);

            // When
            CrawledRawSummaryResult result = service.execute();

            // Then
            assertThat(result.totalRaw()).isZero();
            assertThat(result.countsByStatus()).isEmpty();
        }
    }
}
