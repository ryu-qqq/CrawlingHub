package com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.DashboardCountsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.OutboxStatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.StatusCountDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.monitoring.dto.SystemFailureCountDto;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult.SystemStatus;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("MonitoringCompositeMapper 단위 테스트")
@Tag("unit")
@Tag("adapter-persistence")
class MonitoringCompositeMapperTest {

    private MonitoringCompositeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MonitoringCompositeMapper();
    }

    @Nested
    @DisplayName("toDashboardSummaryResult()")
    class ToDashboardSummaryResult {

        @Test
        @DisplayName("성공: HEALTHY 상태 반환 (에러 < 10)")
        void healthy() {
            DashboardCountsDto dto = new DashboardCountsDto(5, 10, 3, 5);

            DashboardSummaryResult result = mapper.toDashboardSummaryResult(dto);

            assertThat(result.activeSchedulers()).isEqualTo(5);
            assertThat(result.runningTasks()).isEqualTo(10);
            assertThat(result.pendingOutbox()).isEqualTo(3);
            assertThat(result.recentErrors()).isEqualTo(5);
            assertThat(result.overallStatus()).isEqualTo(SystemStatus.HEALTHY);
        }

        @Test
        @DisplayName("성공: WARNING 상태 반환 (에러 >= 10)")
        void warning() {
            DashboardCountsDto dto = new DashboardCountsDto(5, 10, 3, 50);

            DashboardSummaryResult result = mapper.toDashboardSummaryResult(dto);

            assertThat(result.overallStatus()).isEqualTo(SystemStatus.WARNING);
        }

        @Test
        @DisplayName("성공: CRITICAL 상태 반환 (에러 >= 100)")
        void critical() {
            DashboardCountsDto dto = new DashboardCountsDto(5, 10, 3, 150);

            DashboardSummaryResult result = mapper.toDashboardSummaryResult(dto);

            assertThat(result.overallStatus()).isEqualTo(SystemStatus.CRITICAL);
        }
    }

    @Nested
    @DisplayName("toCrawlTaskSummaryResult()")
    class ToCrawlTaskSummaryResult {

        @Test
        @DisplayName("성공: 상태별 카운트 및 총 태스크 수 반환")
        void success() {
            List<StatusCountDto> statusCounts =
                    List.of(
                            new StatusCountDto("RUNNING", 10),
                            new StatusCountDto("FAILED", 5),
                            new StatusCountDto("SUCCESS", 85));
            long stuckTasks = 3;

            CrawlTaskSummaryResult result =
                    mapper.toCrawlTaskSummaryResult(statusCounts, stuckTasks);

            assertThat(result.countsByStatus()).hasSize(3);
            assertThat(result.countsByStatus().get("RUNNING")).isEqualTo(10);
            assertThat(result.stuckTasks()).isEqualTo(3);
            assertThat(result.totalTasks()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("toOutboxSummaryResult()")
    class ToOutboxSummaryResult {

        @Test
        @DisplayName("성공: 아웃박스 타입별 그룹핑")
        void success() {
            List<OutboxStatusCountDto> outboxCounts =
                    List.of(
                            new OutboxStatusCountDto("CRAWL_TASK", "PENDING", 10),
                            new OutboxStatusCountDto("CRAWL_TASK", "SENT", 90),
                            new OutboxStatusCountDto("SCHEDULER", "PENDING", 5),
                            new OutboxStatusCountDto("PRODUCT_SYNC", "FAILED", 3));

            OutboxSummaryResult result = mapper.toOutboxSummaryResult(outboxCounts);

            assertThat(result.crawlTaskOutbox().total()).isEqualTo(100);
            assertThat(result.crawlTaskOutbox().countsByStatus().get("PENDING")).isEqualTo(10);
            assertThat(result.schedulerOutbox().total()).isEqualTo(5);
            assertThat(result.productSyncOutbox().total()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("toCrawledRawSummaryResult()")
    class ToCrawledRawSummaryResult {

        @Test
        @DisplayName("성공: 상태별 카운트 및 총 수 반환")
        void success() {
            List<StatusCountDto> statusCounts =
                    List.of(new StatusCountDto("PENDING", 20), new StatusCountDto("PROCESSED", 80));

            CrawledRawSummaryResult result = mapper.toCrawledRawSummaryResult(statusCounts);

            assertThat(result.countsByStatus()).hasSize(2);
            assertThat(result.totalRaw()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("toExternalSystemHealthResult()")
    class ToExternalSystemHealthResult {

        @Test
        @DisplayName("성공: 시스템별 헬스 상태 반환")
        void success() {
            List<SystemFailureCountDto> failures =
                    List.of(
                            new SystemFailureCountDto("CRAWL_TASK", 2),
                            new SystemFailureCountDto("CRAWL_TASK_OUTBOX", 60));

            ExternalSystemHealthResult result = mapper.toExternalSystemHealthResult(failures);

            assertThat(result.systems()).hasSize(2);
            assertThat(result.systems().get(0).status()).isEqualTo("HEALTHY");
            assertThat(result.systems().get(1).status()).isEqualTo("CRITICAL");
        }
    }
}
