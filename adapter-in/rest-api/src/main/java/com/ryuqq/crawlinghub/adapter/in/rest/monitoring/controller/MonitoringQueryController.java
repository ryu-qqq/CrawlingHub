package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.controller;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.ApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.MonitoringEndpoints;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawlTaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.CrawledRawSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.DashboardSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.ExternalSystemHealthApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.dto.response.OutboxSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.monitoring.mapper.MonitoringQueryApiMapper;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawlTaskSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.CrawledRawSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.DashboardSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.ExternalSystemHealthResult;
import com.ryuqq.crawlinghub.application.monitoring.dto.composite.OutboxSummaryResult;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawlTaskSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawledRawSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetDashboardSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetExternalSystemHealthUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetOutboxSummaryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@Tag(name = "Monitoring", description = "모니터링 집계 API")
public class MonitoringQueryController {

    private static final int DEFAULT_LOOKBACK_MINUTES = 60;

    private final GetDashboardSummaryUseCase getDashboardSummaryUseCase;
    private final GetCrawlTaskSummaryUseCase getCrawlTaskSummaryUseCase;
    private final GetOutboxSummaryUseCase getOutboxSummaryUseCase;
    private final GetCrawledRawSummaryUseCase getCrawledRawSummaryUseCase;
    private final GetExternalSystemHealthUseCase getExternalSystemHealthUseCase;
    private final MonitoringQueryApiMapper monitoringQueryApiMapper;

    public MonitoringQueryController(
            GetDashboardSummaryUseCase getDashboardSummaryUseCase,
            GetCrawlTaskSummaryUseCase getCrawlTaskSummaryUseCase,
            GetOutboxSummaryUseCase getOutboxSummaryUseCase,
            GetCrawledRawSummaryUseCase getCrawledRawSummaryUseCase,
            GetExternalSystemHealthUseCase getExternalSystemHealthUseCase,
            MonitoringQueryApiMapper monitoringQueryApiMapper) {
        this.getDashboardSummaryUseCase = getDashboardSummaryUseCase;
        this.getCrawlTaskSummaryUseCase = getCrawlTaskSummaryUseCase;
        this.getOutboxSummaryUseCase = getOutboxSummaryUseCase;
        this.getCrawledRawSummaryUseCase = getCrawledRawSummaryUseCase;
        this.getExternalSystemHealthUseCase = getExternalSystemHealthUseCase;
        this.monitoringQueryApiMapper = monitoringQueryApiMapper;
    }

    @GetMapping(MonitoringEndpoints.DASHBOARD)
    @Operation(summary = "대시보드 요약", description = "시스템 전체 대시보드 요약 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DashboardSummaryApiResponse>> getDashboardSummary(
            @Parameter(description = "조회 기간 (분)", example = "60")
                    @RequestParam(value = "lookbackMinutes", required = false)
                    Integer lookbackMinutes) {
        Duration lookback = Duration.ofMinutes(resolveMinutes(lookbackMinutes));
        DashboardSummaryResult result = getDashboardSummaryUseCase.execute(lookback);
        return ResponseEntity.ok(
                ApiResponse.of(monitoringQueryApiMapper.toDashboardApiResponse(result)));
    }

    @GetMapping(MonitoringEndpoints.CRAWL_TASKS_SUMMARY)
    @Operation(summary = "크롤 태스크 요약", description = "크롤 태스크 상태별 집계를 조회합니다.")
    public ResponseEntity<ApiResponse<CrawlTaskSummaryApiResponse>> getCrawlTaskSummary(
            @Parameter(description = "조회 기간 (분)", example = "60")
                    @RequestParam(value = "lookbackMinutes", required = false)
                    Integer lookbackMinutes) {
        Duration lookback = Duration.ofMinutes(resolveMinutes(lookbackMinutes));
        CrawlTaskSummaryResult result = getCrawlTaskSummaryUseCase.execute(lookback);
        return ResponseEntity.ok(
                ApiResponse.of(monitoringQueryApiMapper.toCrawlTaskSummaryApiResponse(result)));
    }

    @GetMapping(MonitoringEndpoints.OUTBOX_SUMMARY)
    @Operation(summary = "아웃박스 요약", description = "아웃박스 상태별 집계를 조회합니다.")
    public ResponseEntity<ApiResponse<OutboxSummaryApiResponse>> getOutboxSummary() {
        OutboxSummaryResult result = getOutboxSummaryUseCase.execute();
        return ResponseEntity.ok(
                ApiResponse.of(monitoringQueryApiMapper.toOutboxSummaryApiResponse(result)));
    }

    @GetMapping(MonitoringEndpoints.CRAWLED_RAW_SUMMARY)
    @Operation(summary = "크롤 원시 데이터 요약", description = "크롤 원시 데이터 상태별 집계를 조회합니다.")
    public ResponseEntity<ApiResponse<CrawledRawSummaryApiResponse>> getCrawledRawSummary() {
        CrawledRawSummaryResult result = getCrawledRawSummaryUseCase.execute();
        return ResponseEntity.ok(
                ApiResponse.of(monitoringQueryApiMapper.toCrawledRawSummaryApiResponse(result)));
    }

    @GetMapping(MonitoringEndpoints.EXTERNAL_SYSTEMS_HEALTH)
    @Operation(summary = "외부 시스템 헬스", description = "외부 시스템별 최근 실패율을 조회합니다.")
    public ResponseEntity<ApiResponse<ExternalSystemHealthApiResponse>> getExternalSystemHealth(
            @Parameter(description = "조회 기간 (분)", example = "60")
                    @RequestParam(value = "lookbackMinutes", required = false)
                    Integer lookbackMinutes) {
        Duration lookback = Duration.ofMinutes(resolveMinutes(lookbackMinutes));
        ExternalSystemHealthResult result = getExternalSystemHealthUseCase.execute(lookback);
        return ResponseEntity.ok(
                ApiResponse.of(monitoringQueryApiMapper.toExternalSystemHealthApiResponse(result)));
    }

    private int resolveMinutes(Integer lookbackMinutes) {
        return lookbackMinutes != null && lookbackMinutes > 0
                ? lookbackMinutes
                : DEFAULT_LOOKBACK_MINUTES;
    }
}
