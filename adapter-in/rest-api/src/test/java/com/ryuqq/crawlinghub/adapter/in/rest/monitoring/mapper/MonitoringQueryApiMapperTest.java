package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawlTaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawledRawSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.DashboardSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.ExternalSystemHealthApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.OutboxSummaryApiResponse;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * MonitoringQueryApiMapper 단위 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>Application Result → API Response 변환
 *   <li>필드 매핑 정확성
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("MonitoringQueryApiMapper 단위 테스트")
@Tag("unit")
@Tag("adapter-rest")
class MonitoringQueryApiMapperTest {

    private MonitoringQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MonitoringQueryApiMapper();
    }

    @Nested
    @DisplayName("toDashboardApiResponse(DashboardSummaryResult) - 대시보드 응답 변환")
    class ToDashboardApiResponseTests {

        @Test
        @DisplayName("성공: HEALTHY 상태 대시보드 변환")
        void toDashboardApiResponse_Healthy() {
            // Given
            DashboardSummaryResult result =
                    new DashboardSummaryResult(
                            5L, 3L, 10L, 0L, DashboardSummaryResult.SystemStatus.HEALTHY);

            // When
            DashboardSummaryApiResponse response = mapper.toDashboardApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.activeSchedulers()).isEqualTo(5L);
            assertThat(response.runningTasks()).isEqualTo(3L);
            assertThat(response.pendingOutbox()).isEqualTo(10L);
            assertThat(response.recentErrors()).isEqualTo(0L);
            assertThat(response.overallStatus()).isEqualTo("HEALTHY");
        }

        @Test
        @DisplayName("성공: WARNING 상태 대시보드 변환")
        void toDashboardApiResponse_Warning() {
            // Given
            DashboardSummaryResult result =
                    new DashboardSummaryResult(
                            3L, 2L, 50L, 5L, DashboardSummaryResult.SystemStatus.WARNING);

            // When
            DashboardSummaryApiResponse response = mapper.toDashboardApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.recentErrors()).isEqualTo(5L);
            assertThat(response.overallStatus()).isEqualTo("WARNING");
        }

        @Test
        @DisplayName("성공: CRITICAL 상태 대시보드 변환")
        void toDashboardApiResponse_Critical() {
            // Given
            DashboardSummaryResult result =
                    new DashboardSummaryResult(
                            1L, 0L, 100L, 30L, DashboardSummaryResult.SystemStatus.CRITICAL);

            // When
            DashboardSummaryApiResponse response = mapper.toDashboardApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.activeSchedulers()).isEqualTo(1L);
            assertThat(response.runningTasks()).isEqualTo(0L);
            assertThat(response.pendingOutbox()).isEqualTo(100L);
            assertThat(response.recentErrors()).isEqualTo(30L);
            assertThat(response.overallStatus()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("성공: 모든 값이 0인 초기 상태 변환")
        void toDashboardApiResponse_ZeroValues() {
            // Given
            DashboardSummaryResult result =
                    new DashboardSummaryResult(
                            0L, 0L, 0L, 0L, DashboardSummaryResult.SystemStatus.HEALTHY);

            // When
            DashboardSummaryApiResponse response = mapper.toDashboardApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.activeSchedulers()).isEqualTo(0L);
            assertThat(response.runningTasks()).isEqualTo(0L);
            assertThat(response.pendingOutbox()).isEqualTo(0L);
            assertThat(response.recentErrors()).isEqualTo(0L);
            assertThat(response.overallStatus()).isEqualTo("HEALTHY");
        }
    }

    @Nested
    @DisplayName("toCrawlTaskSummaryApiResponse(CrawlTaskSummaryResult) - 크롤 태스크 요약 변환")
    class ToCrawlTaskSummaryApiResponseTests {

        @Test
        @DisplayName("성공: 상태별 카운트 맵 포함 변환")
        void toCrawlTaskSummaryApiResponse_WithStatusCounts() {
            // Given
            Map<String, Long> countsByStatus =
                    Map.of("SUCCESS", 80L, "FAILED", 10L, "RUNNING", 5L, "PENDING", 5L);
            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(countsByStatus, 2L, 100L);

            // When
            CrawlTaskSummaryApiResponse response = mapper.toCrawlTaskSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.countsByStatus()).containsEntry("SUCCESS", 80L);
            assertThat(response.countsByStatus()).containsEntry("FAILED", 10L);
            assertThat(response.stuckTasks()).isEqualTo(2L);
            assertThat(response.totalTasks()).isEqualTo(100L);
        }

        @Test
        @DisplayName("성공: 빈 상태 맵 변환")
        void toCrawlTaskSummaryApiResponse_EmptyStatusMap() {
            // Given
            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(Map.of(), 0L, 0L);

            // When
            CrawlTaskSummaryApiResponse response = mapper.toCrawlTaskSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.countsByStatus()).isEmpty();
            assertThat(response.stuckTasks()).isEqualTo(0L);
            assertThat(response.totalTasks()).isEqualTo(0L);
        }

        @Test
        @DisplayName("성공: stuckTasks가 존재하는 경우 변환")
        void toCrawlTaskSummaryApiResponse_WithStuckTasks() {
            // Given
            Map<String, Long> countsByStatus = Map.of("RUNNING", 5L, "PENDING", 3L);
            CrawlTaskSummaryResult result = new CrawlTaskSummaryResult(countsByStatus, 5L, 8L);

            // When
            CrawlTaskSummaryApiResponse response = mapper.toCrawlTaskSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.stuckTasks()).isEqualTo(5L);
            assertThat(response.totalTasks()).isEqualTo(8L);
        }
    }

    @Nested
    @DisplayName("toOutboxSummaryApiResponse(OutboxSummaryResult) - 아웃박스 요약 변환")
    class ToOutboxSummaryApiResponseTests {

        @Test
        @DisplayName("성공: 세 가지 아웃박스 변환")
        void toOutboxSummaryApiResponse_AllOutboxes() {
            // Given
            OutboxSummaryResult.OutboxDetail crawlTaskOutbox =
                    new OutboxSummaryResult.OutboxDetail(
                            Map.of("PENDING", 10L, "PUBLISHED", 90L), 100L);
            OutboxSummaryResult.OutboxDetail schedulerOutbox =
                    new OutboxSummaryResult.OutboxDetail(
                            Map.of("PENDING", 5L, "PUBLISHED", 45L), 50L);
            OutboxSummaryResult.OutboxDetail productSyncOutbox =
                    new OutboxSummaryResult.OutboxDetail(
                            Map.of("PENDING", 2L, "PUBLISHED", 198L), 200L);
            OutboxSummaryResult result =
                    new OutboxSummaryResult(crawlTaskOutbox, schedulerOutbox, productSyncOutbox);

            // When
            OutboxSummaryApiResponse response = mapper.toOutboxSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.crawlTaskOutbox()).isNotNull();
            assertThat(response.crawlTaskOutbox().total()).isEqualTo(100L);
            assertThat(response.crawlTaskOutbox().countsByStatus()).containsEntry("PENDING", 10L);
            assertThat(response.schedulerOutbox()).isNotNull();
            assertThat(response.schedulerOutbox().total()).isEqualTo(50L);
            assertThat(response.productSyncOutbox()).isNotNull();
            assertThat(response.productSyncOutbox().total()).isEqualTo(200L);
        }

        @Test
        @DisplayName("성공: 모든 아웃박스가 비어있는 경우 변환")
        void toOutboxSummaryApiResponse_EmptyOutboxes() {
            // Given
            OutboxSummaryResult.OutboxDetail emptyOutbox =
                    new OutboxSummaryResult.OutboxDetail(Map.of(), 0L);
            OutboxSummaryResult result =
                    new OutboxSummaryResult(emptyOutbox, emptyOutbox, emptyOutbox);

            // When
            OutboxSummaryApiResponse response = mapper.toOutboxSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.crawlTaskOutbox().total()).isEqualTo(0L);
            assertThat(response.crawlTaskOutbox().countsByStatus()).isEmpty();
            assertThat(response.schedulerOutbox().total()).isEqualTo(0L);
            assertThat(response.productSyncOutbox().total()).isEqualTo(0L);
        }

        @Test
        @DisplayName("성공: FAILED 상태 포함 아웃박스 변환")
        void toOutboxSummaryApiResponse_WithFailedStatus() {
            // Given
            OutboxSummaryResult.OutboxDetail failedOutbox =
                    new OutboxSummaryResult.OutboxDetail(
                            Map.of("PENDING", 3L, "FAILED", 7L, "PUBLISHED", 90L), 100L);
            OutboxSummaryResult.OutboxDetail normalOutbox =
                    new OutboxSummaryResult.OutboxDetail(Map.of("PUBLISHED", 20L), 20L);
            OutboxSummaryResult result =
                    new OutboxSummaryResult(failedOutbox, normalOutbox, normalOutbox);

            // When
            OutboxSummaryApiResponse response = mapper.toOutboxSummaryApiResponse(result);

            // Then
            assertThat(response.crawlTaskOutbox().countsByStatus()).containsEntry("FAILED", 7L);
            assertThat(response.crawlTaskOutbox().total()).isEqualTo(100L);
        }
    }

    @Nested
    @DisplayName("toCrawledRawSummaryApiResponse(CrawledRawSummaryResult) - 크롤 원시 데이터 요약 변환")
    class ToCrawledRawSummaryApiResponseTests {

        @Test
        @DisplayName("성공: 상태별 카운트 포함 변환")
        void toCrawledRawSummaryApiResponse_WithStatusCounts() {
            // Given
            Map<String, Long> countsByStatus =
                    Map.of("PROCESSED", 500L, "PENDING", 100L, "FAILED", 20L);
            CrawledRawSummaryResult result = new CrawledRawSummaryResult(countsByStatus, 620L);

            // When
            CrawledRawSummaryApiResponse response = mapper.toCrawledRawSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.countsByStatus()).containsEntry("PROCESSED", 500L);
            assertThat(response.countsByStatus()).containsEntry("PENDING", 100L);
            assertThat(response.countsByStatus()).containsEntry("FAILED", 20L);
            assertThat(response.totalRaw()).isEqualTo(620L);
        }

        @Test
        @DisplayName("성공: 빈 상태 맵 변환")
        void toCrawledRawSummaryApiResponse_EmptyStatusMap() {
            // Given
            CrawledRawSummaryResult result = new CrawledRawSummaryResult(Map.of(), 0L);

            // When
            CrawledRawSummaryApiResponse response = mapper.toCrawledRawSummaryApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.countsByStatus()).isEmpty();
            assertThat(response.totalRaw()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("toExternalSystemHealthApiResponse(ExternalSystemHealthResult) - 외부 시스템 헬스 변환")
    class ToExternalSystemHealthApiResponseTests {

        @Test
        @DisplayName("성공: 다수 시스템 헬스 변환")
        void toExternalSystemHealthApiResponse_MultipleSystems() {
            // Given
            List<ExternalSystemHealthResult.SystemHealth> systems =
                    List.of(
                            new ExternalSystemHealthResult.SystemHealth("AWS_SQS", 0L, "HEALTHY"),
                            new ExternalSystemHealthResult.SystemHealth(
                                    "MARKETPLACE_API", 3L, "WARNING"),
                            new ExternalSystemHealthResult.SystemHealth(
                                    "EVENTBRIDGE", 10L, "CRITICAL"));
            ExternalSystemHealthResult result = new ExternalSystemHealthResult(systems);

            // When
            ExternalSystemHealthApiResponse response =
                    mapper.toExternalSystemHealthApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.systems()).hasSize(3);
            assertThat(response.systems().get(0).system()).isEqualTo("AWS_SQS");
            assertThat(response.systems().get(0).recentFailures()).isEqualTo(0L);
            assertThat(response.systems().get(0).status()).isEqualTo("HEALTHY");
            assertThat(response.systems().get(1).system()).isEqualTo("MARKETPLACE_API");
            assertThat(response.systems().get(1).recentFailures()).isEqualTo(3L);
            assertThat(response.systems().get(1).status()).isEqualTo("WARNING");
            assertThat(response.systems().get(2).system()).isEqualTo("EVENTBRIDGE");
            assertThat(response.systems().get(2).status()).isEqualTo("CRITICAL");
        }

        @Test
        @DisplayName("성공: 시스템 목록이 비어있는 경우 변환")
        void toExternalSystemHealthApiResponse_EmptySystems() {
            // Given
            ExternalSystemHealthResult result = new ExternalSystemHealthResult(List.of());

            // When
            ExternalSystemHealthApiResponse response =
                    mapper.toExternalSystemHealthApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.systems()).isEmpty();
        }

        @Test
        @DisplayName("성공: 단일 시스템 헬스 변환")
        void toExternalSystemHealthApiResponse_SingleSystem() {
            // Given
            List<ExternalSystemHealthResult.SystemHealth> systems =
                    List.of(new ExternalSystemHealthResult.SystemHealth("AWS_SQS", 0L, "HEALTHY"));
            ExternalSystemHealthResult result = new ExternalSystemHealthResult(systems);

            // When
            ExternalSystemHealthApiResponse response =
                    mapper.toExternalSystemHealthApiResponse(result);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.systems()).hasSize(1);
            assertThat(response.systems().get(0).system()).isEqualTo("AWS_SQS");
        }
    }
}
