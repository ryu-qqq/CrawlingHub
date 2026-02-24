package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.DashboardCountsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.OutboxStatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.StatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.SystemFailureCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.mapper.MonitoringCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.repository.MonitoringCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult.SystemStatus;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult.OutboxDetail;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringCompositeQueryAdapter 단위 테스트")
@Tag("unit")
@Tag("adapter-persistence")
class MonitoringCompositeQueryAdapterTest {

    @Mock private MonitoringCompositeQueryDslRepository repository;
    @Mock private MonitoringCompositeMapper mapper;
    @InjectMocks private MonitoringCompositeQueryAdapter adapter;

    @Nested
    @DisplayName("getDashboardSummary()")
    class GetDashboardSummary {

        @Test
        @DisplayName("성공: 대시보드 요약 반환")
        void success() {
            Duration lookback = Duration.ofMinutes(60);
            DashboardCountsDto countsDto = new DashboardCountsDto(5, 10, 3, 2);
            DashboardSummaryResult expected =
                    new DashboardSummaryResult(5, 10, 3, 2, SystemStatus.HEALTHY);

            given(repository.fetchDashboardCounts(any(Instant.class))).willReturn(countsDto);
            given(mapper.toDashboardSummaryResult(countsDto)).willReturn(expected);

            DashboardSummaryResult result = adapter.getDashboardSummary(lookback);

            assertThat(result).isEqualTo(expected);
            then(repository).should().fetchDashboardCounts(any(Instant.class));
            then(mapper).should().toDashboardSummaryResult(countsDto);
        }
    }

    @Nested
    @DisplayName("getCrawlTaskSummary()")
    class GetCrawlTaskSummary {

        @Test
        @DisplayName("성공: 크롤 태스크 요약 반환")
        void success() {
            Duration lookback = Duration.ofMinutes(60);
            List<StatusCountDto> statusCounts = List.of(new StatusCountDto("RUNNING", 10));
            long stuckTasks = 3L;
            CrawlTaskSummaryResult expected =
                    new CrawlTaskSummaryResult(Map.of("RUNNING", 10L), 3, 10);

            given(repository.fetchCrawlTaskCountsByStatus()).willReturn(statusCounts);
            given(repository.fetchStuckCrawlTasks(any(Instant.class))).willReturn(stuckTasks);
            given(mapper.toCrawlTaskSummaryResult(statusCounts, stuckTasks)).willReturn(expected);

            CrawlTaskSummaryResult result = adapter.getCrawlTaskSummary(lookback);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getOutboxSummary()")
    class GetOutboxSummary {

        @Test
        @DisplayName("성공: 아웃박스 요약 반환")
        void success() {
            List<OutboxStatusCountDto> outboxCounts =
                    List.of(new OutboxStatusCountDto("CRAWL_TASK", "PENDING", 10));
            OutboxSummaryResult expected =
                    new OutboxSummaryResult(
                            new OutboxDetail(Map.of("PENDING", 10L), 10),
                            new OutboxDetail(Map.of(), 0),
                            new OutboxDetail(Map.of(), 0));

            given(repository.fetchOutboxCountsByType()).willReturn(outboxCounts);
            given(mapper.toOutboxSummaryResult(outboxCounts)).willReturn(expected);

            OutboxSummaryResult result = adapter.getOutboxSummary();

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getCrawledRawSummary()")
    class GetCrawledRawSummary {

        @Test
        @DisplayName("성공: 크롤 원시 데이터 요약 반환")
        void success() {
            List<StatusCountDto> statusCounts = List.of(new StatusCountDto("PENDING", 20));
            CrawledRawSummaryResult expected =
                    new CrawledRawSummaryResult(Map.of("PENDING", 20L), 20);

            given(repository.fetchCrawledRawCountsByStatus()).willReturn(statusCounts);
            given(mapper.toCrawledRawSummaryResult(statusCounts)).willReturn(expected);

            CrawledRawSummaryResult result = adapter.getCrawledRawSummary();

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getExternalSystemHealth()")
    class GetExternalSystemHealth {

        @Test
        @DisplayName("성공: 외부 시스템 헬스 반환")
        void success() {
            Duration lookback = Duration.ofMinutes(60);
            List<SystemFailureCountDto> failures =
                    List.of(new SystemFailureCountDto("CRAWL_TASK", 2));
            ExternalSystemHealthResult expected =
                    new ExternalSystemHealthResult(
                            List.of(
                                    new ExternalSystemHealthResult.SystemHealth(
                                            "CRAWL_TASK", 2, "HEALTHY")));

            given(repository.fetchRecentFailureCounts(any(Instant.class))).willReturn(failures);
            given(mapper.toExternalSystemHealthResult(failures)).willReturn(expected);

            ExternalSystemHealthResult result = adapter.getExternalSystemHealth(lookback);

            assertThat(result).isEqualTo(expected);
        }
    }
}
