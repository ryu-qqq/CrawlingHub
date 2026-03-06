package com.ryuqq.crawlinghub.adapter.in.rest.monitoring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
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
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawlExecutionSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawlTaskSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetCrawledRawSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetDashboardSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetExternalSystemHealthUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetOutboxSummaryUseCase;
import com.ryuqq.crawlinghub.application.monitoring.port.in.query.GetProductSyncFailureSummaryUseCase;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * MonitoringQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(MonitoringQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("MonitoringQueryController REST Docs")
class MonitoringQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetDashboardSummaryUseCase getDashboardSummaryUseCase;

    @MockitoBean private GetCrawlTaskSummaryUseCase getCrawlTaskSummaryUseCase;

    @MockitoBean private GetOutboxSummaryUseCase getOutboxSummaryUseCase;

    @MockitoBean private GetCrawledRawSummaryUseCase getCrawledRawSummaryUseCase;

    @MockitoBean private GetExternalSystemHealthUseCase getExternalSystemHealthUseCase;

    @MockitoBean private GetProductSyncFailureSummaryUseCase getProductSyncFailureSummaryUseCase;

    @MockitoBean private GetCrawlExecutionSummaryUseCase getCrawlExecutionSummaryUseCase;

    @MockitoBean private MonitoringQueryApiMapper monitoringQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/monitoring/dashboard - 대시보드 요약 API 문서")
    void getDashboardSummary() throws Exception {
        // given
        DashboardSummaryResult useCaseResult =
                new DashboardSummaryResult(
                        5L, 3L, 10L, 0L, DashboardSummaryResult.SystemStatus.HEALTHY);

        DashboardSummaryApiResponse apiResponse =
                new DashboardSummaryApiResponse(5L, 3L, 10L, 0L, "HEALTHY");

        given(getDashboardSummaryUseCase.execute(any(Duration.class))).willReturn(useCaseResult);
        given(monitoringQueryApiMapper.toDashboardApiResponse(any(DashboardSummaryResult.class)))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/monitoring/dashboard").param("lookbackMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overallStatus").value("HEALTHY"))
                .andDo(
                        document(
                                "monitoring-query/dashboard",
                                RestDocsSecuritySnippets.authenticated(),
                                queryParameters(
                                        parameterWithName("lookbackMinutes")
                                                .description("조회 기간 (분, 기본값: 60)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.activeSchedulers")
                                                .type(JsonFieldType.NUMBER)
                                                .description("활성 스케줄러 수"),
                                        fieldWithPath("data.runningTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실행 중인 태스크 수"),
                                        fieldWithPath("data.pendingOutbox")
                                                .type(JsonFieldType.NUMBER)
                                                .description("대기 중인 아웃박스 수"),
                                        fieldWithPath("data.recentErrors")
                                                .type(JsonFieldType.NUMBER)
                                                .description("최근 에러 수"),
                                        fieldWithPath("data.overallStatus")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "전체 시스템 상태 (HEALTHY / WARNING / CRITICAL)"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/crawl-tasks/summary - 크롤 태스크 요약 API 문서")
    void getCrawlTaskSummary() throws Exception {
        // given
        CrawlTaskSummaryResult useCaseResult =
                new CrawlTaskSummaryResult(
                        Map.of("SUCCESS", 80L, "FAILED", 10L, "RUNNING", 5L, "PENDING", 5L),
                        2L,
                        100L);

        CrawlTaskSummaryApiResponse apiResponse =
                new CrawlTaskSummaryApiResponse(
                        Map.of("SUCCESS", 80L, "FAILED", 10L, "RUNNING", 5L, "PENDING", 5L),
                        2L,
                        100L);

        given(getCrawlTaskSummaryUseCase.execute(any(Duration.class))).willReturn(useCaseResult);
        given(
                        monitoringQueryApiMapper.toCrawlTaskSummaryApiResponse(
                                any(CrawlTaskSummaryResult.class)))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/monitoring/crawl-tasks/summary")
                                .param("lookbackMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTasks").value(100))
                .andDo(
                        document(
                                "monitoring-query/crawl-tasks-summary",
                                RestDocsSecuritySnippets.authenticated(),
                                queryParameters(
                                        parameterWithName("lookbackMinutes")
                                                .description("조회 기간 (분, 기본값: 60)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.countsByStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("상태별 태스크 카운트 맵"),
                                        fieldWithPath("data.countsByStatus.*")
                                                .type(JsonFieldType.NUMBER)
                                                .description("각 상태별 태스크 수"),
                                        fieldWithPath("data.stuckTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("교착 상태 태스크 수"),
                                        fieldWithPath("data.totalTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 태스크 수"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/outbox/summary - 아웃박스 요약 API 문서")
    void getOutboxSummary() throws Exception {
        // given
        OutboxSummaryResult.OutboxDetail crawlTaskDetail =
                new OutboxSummaryResult.OutboxDetail(
                        Map.of("PENDING", 10L, "PUBLISHED", 90L), 100L);
        OutboxSummaryResult.OutboxDetail schedulerDetail =
                new OutboxSummaryResult.OutboxDetail(Map.of("PENDING", 5L, "PUBLISHED", 45L), 50L);
        OutboxSummaryResult.OutboxDetail productSyncDetail =
                new OutboxSummaryResult.OutboxDetail(
                        Map.of("PENDING", 2L, "PUBLISHED", 198L), 200L);
        OutboxSummaryResult useCaseResult =
                new OutboxSummaryResult(crawlTaskDetail, schedulerDetail, productSyncDetail);

        OutboxSummaryApiResponse apiResponse =
                new OutboxSummaryApiResponse(
                        new OutboxSummaryApiResponse.OutboxDetailApiResponse(
                                Map.of("PENDING", 10L, "PUBLISHED", 90L), 100L),
                        new OutboxSummaryApiResponse.OutboxDetailApiResponse(
                                Map.of("PENDING", 5L, "PUBLISHED", 45L), 50L),
                        new OutboxSummaryApiResponse.OutboxDetailApiResponse(
                                Map.of("PENDING", 2L, "PUBLISHED", 198L), 200L));

        given(getOutboxSummaryUseCase.execute()).willReturn(useCaseResult);
        given(monitoringQueryApiMapper.toOutboxSummaryApiResponse(any(OutboxSummaryResult.class)))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/monitoring/outbox/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.crawlTaskOutbox.total").value(100))
                .andDo(
                        document(
                                "monitoring-query/outbox-summary",
                                RestDocsSecuritySnippets.authenticated(),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlTaskOutbox")
                                                .type(JsonFieldType.OBJECT)
                                                .description("크롤 태스크 아웃박스 요약"),
                                        fieldWithPath("data.crawlTaskOutbox.countsByStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("크롤 태스크 아웃박스 상태별 카운트 맵"),
                                        fieldWithPath("data.crawlTaskOutbox.countsByStatus.*")
                                                .type(JsonFieldType.NUMBER)
                                                .description("각 상태별 크롤 태스크 아웃박스 수"),
                                        fieldWithPath("data.crawlTaskOutbox.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 태스크 아웃박스 전체 수"),
                                        fieldWithPath("data.schedulerOutbox")
                                                .type(JsonFieldType.OBJECT)
                                                .description("스케줄러 아웃박스 요약"),
                                        fieldWithPath("data.schedulerOutbox.countsByStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("스케줄러 아웃박스 상태별 카운트 맵"),
                                        fieldWithPath("data.schedulerOutbox.countsByStatus.*")
                                                .type(JsonFieldType.NUMBER)
                                                .description("각 상태별 스케줄러 아웃박스 수"),
                                        fieldWithPath("data.schedulerOutbox.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("스케줄러 아웃박스 전체 수"),
                                        fieldWithPath("data.productSyncOutbox")
                                                .type(JsonFieldType.OBJECT)
                                                .description("상품 동기화 아웃박스 요약"),
                                        fieldWithPath("data.productSyncOutbox.countsByStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("상품 동기화 아웃박스 상태별 카운트 맵"),
                                        fieldWithPath("data.productSyncOutbox.countsByStatus.*")
                                                .type(JsonFieldType.NUMBER)
                                                .description("각 상태별 상품 동기화 아웃박스 수"),
                                        fieldWithPath("data.productSyncOutbox.total")
                                                .type(JsonFieldType.NUMBER)
                                                .description("상품 동기화 아웃박스 전체 수"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/crawled-raw/summary - 크롤 원시 데이터 요약 API 문서")
    void getCrawledRawSummary() throws Exception {
        // given
        CrawledRawSummaryResult useCaseResult =
                new CrawledRawSummaryResult(
                        Map.of("PROCESSED", 500L, "PENDING", 100L, "FAILED", 20L), 620L);

        CrawledRawSummaryApiResponse apiResponse =
                new CrawledRawSummaryApiResponse(
                        Map.of("PROCESSED", 500L, "PENDING", 100L, "FAILED", 20L), 620L);

        given(getCrawledRawSummaryUseCase.execute()).willReturn(useCaseResult);
        given(
                        monitoringQueryApiMapper.toCrawledRawSummaryApiResponse(
                                any(CrawledRawSummaryResult.class)))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/monitoring/crawled-raw/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRaw").value(620))
                .andDo(
                        document(
                                "monitoring-query/crawled-raw-summary",
                                RestDocsSecuritySnippets.authenticated(),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.countsByStatus")
                                                .type(JsonFieldType.OBJECT)
                                                .description("상태별 크롤 원시 데이터 카운트 맵"),
                                        fieldWithPath("data.countsByStatus.*")
                                                .type(JsonFieldType.NUMBER)
                                                .description("각 상태별 크롤 원시 데이터 수"),
                                        fieldWithPath("data.totalRaw")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 크롤 원시 데이터 수"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/monitoring/external-systems/health - 외부 시스템 헬스 API 문서")
    void getExternalSystemHealth() throws Exception {
        // given
        ExternalSystemHealthResult useCaseResult =
                new ExternalSystemHealthResult(
                        List.of(
                                new ExternalSystemHealthResult.SystemHealth(
                                        "AWS_SQS", 0L, "HEALTHY"),
                                new ExternalSystemHealthResult.SystemHealth(
                                        "MARKETPLACE_API", 3L, "WARNING"),
                                new ExternalSystemHealthResult.SystemHealth(
                                        "EVENTBRIDGE", 0L, "HEALTHY")));

        ExternalSystemHealthApiResponse apiResponse =
                new ExternalSystemHealthApiResponse(
                        List.of(
                                new ExternalSystemHealthApiResponse.SystemHealthApiResponse(
                                        "AWS_SQS", 0L, "HEALTHY"),
                                new ExternalSystemHealthApiResponse.SystemHealthApiResponse(
                                        "MARKETPLACE_API", 3L, "WARNING"),
                                new ExternalSystemHealthApiResponse.SystemHealthApiResponse(
                                        "EVENTBRIDGE", 0L, "HEALTHY")));

        given(getExternalSystemHealthUseCase.execute(any(Duration.class)))
                .willReturn(useCaseResult);
        given(
                        monitoringQueryApiMapper.toExternalSystemHealthApiResponse(
                                any(ExternalSystemHealthResult.class)))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/monitoring/external-systems/health")
                                .param("lookbackMinutes", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.systems").isArray())
                .andDo(
                        document(
                                "monitoring-query/external-systems-health",
                                RestDocsSecuritySnippets.authenticated(),
                                queryParameters(
                                        parameterWithName("lookbackMinutes")
                                                .description("조회 기간 (분, 기본값: 60)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.systems")
                                                .type(JsonFieldType.ARRAY)
                                                .description("외부 시스템 헬스 목록"),
                                        fieldWithPath("data.systems[].system")
                                                .type(JsonFieldType.STRING)
                                                .description("외부 시스템 이름"),
                                        fieldWithPath("data.systems[].recentFailures")
                                                .type(JsonFieldType.NUMBER)
                                                .description("최근 실패 횟수"),
                                        fieldWithPath("data.systems[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "시스템 상태 (HEALTHY / WARNING / CRITICAL)"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
