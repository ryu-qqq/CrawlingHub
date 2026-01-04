package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.ExecutionInfoApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SchedulerStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerDetailResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.response.SellerSummaryForScheduler;
import com.ryuqq.crawlinghub.application.schedule.dto.response.TaskSummaryForScheduler;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlScheduleUseCase;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * CrawlSchedulerQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawlSchedulerQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlSchedulerQueryController REST Docs")
class CrawlSchedulerQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private SearchCrawlSchedulesUseCase searchCrawlSchedulesUseCase;

    @MockitoBean private SearchCrawlScheduleUseCase searchCrawlScheduleUseCase;

    @MockitoBean private CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/schedules - 크롤 스케줄러 목록 조회 API 문서")
    void listCrawlSchedulers() throws Exception {
        // given
        List<CrawlSchedulerResponse> content =
                List.of(
                        new CrawlSchedulerResponse(
                                1L,
                                1L,
                                "daily-crawl",
                                "0 0 9 * * ?",
                                SchedulerStatus.ACTIVE,
                                Instant.parse("2025-11-20T10:30:00Z"),
                                null),
                        new CrawlSchedulerResponse(
                                2L,
                                1L,
                                "hourly-crawl",
                                "0 0 * * * ?",
                                SchedulerStatus.INACTIVE,
                                Instant.parse("2025-11-19T15:00:00Z"),
                                Instant.parse("2025-11-20T09:00:00Z")));

        PageResponse<CrawlSchedulerResponse> pageResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<CrawlSchedulerSummaryApiResponse> apiContent =
                List.of(
                        new CrawlSchedulerSummaryApiResponse(
                                1L,
                                1L,
                                "daily-crawl",
                                "0 0 9 * * ?",
                                "ACTIVE",
                                "2025-11-20 10:30:00",
                                null),
                        new CrawlSchedulerSummaryApiResponse(
                                2L,
                                1L,
                                "hourly-crawl",
                                "0 0 * * * ?",
                                "INACTIVE",
                                "2025-11-19 15:00:00",
                                "2025-11-20 09:00:00"));

        PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(crawlSchedulerQueryApiMapper.toQuery(any())).willReturn(null);
        given(searchCrawlSchedulesUseCase.execute(any())).willReturn(pageResponse);
        given(crawlSchedulerQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/schedules")
                                .param("sellerId", "1")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "schedule-query/list",
                                RestDocsSecuritySnippets.authorization("scheduler:read"),
                                queryParameters(
                                        parameterWithName("sellerId")
                                                .description("셀러 ID 필터 (양수, 선택)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description("상태 필터 (ACTIVE/INACTIVE, 선택)")
                                                .optional(),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0부터 시작, 기본값: 0)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 20, 최대: 100)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("스케줄러 목록"),
                                        fieldWithPath("data.content[].crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.content[].schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름"),
                                        fieldWithPath("data.content[].cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("크론 표현식"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 시각")
                                                .optional(),
                                        fieldWithPath("data.page")
                                                .type(JsonFieldType.NUMBER)
                                                .description("현재 페이지 번호"),
                                        fieldWithPath("data.size")
                                                .type(JsonFieldType.NUMBER)
                                                .description("페이지 크기"),
                                        fieldWithPath("data.totalElements")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 데이터 개수"),
                                        fieldWithPath("data.totalPages")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 페이지 수"),
                                        fieldWithPath("data.first")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("첫 페이지 여부"),
                                        fieldWithPath("data.last")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("마지막 페이지 여부"),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }

    @Test
    @DisplayName("GET /api/v1/crawling/schedules/{crawlSchedulerId} - 크롤 스케줄러 상세 조회 API 문서")
    void getCrawlScheduler() throws Exception {
        // given
        Long crawlSchedulerId = 1L;
        Instant now = Instant.parse("2025-11-20T10:30:00Z");
        Instant lastExecution = Instant.parse("2025-11-20T09:00:00Z");
        Instant nextExecution = Instant.parse("2025-11-21T09:00:00Z");

        SellerSummaryForScheduler sellerSummary =
                new SellerSummaryForScheduler(100L, "TestSeller", "머스트잇셀러");
        ExecutionInfo executionInfo = new ExecutionInfo(nextExecution, lastExecution, "SUCCESS");
        SchedulerStatistics statistics = new SchedulerStatistics(150, 145, 5, 0.9667, 12500);
        List<TaskSummaryForScheduler> recentTasks =
                List.of(
                        new TaskSummaryForScheduler(
                                1001L, "COMPLETED", "PRODUCT_CRAWL", now.minusSeconds(3600), now),
                        new TaskSummaryForScheduler(1002L, "RUNNING", "PRODUCT_CRAWL", now, null));

        CrawlSchedulerDetailResponse detailResponse =
                new CrawlSchedulerDetailResponse(
                        crawlSchedulerId,
                        "daily-crawl",
                        "0 0 9 * * ?",
                        SchedulerStatus.ACTIVE,
                        now.minusSeconds(86400 * 30),
                        now,
                        sellerSummary,
                        executionInfo,
                        statistics,
                        recentTasks);

        SellerSummaryApiResponse sellerApiResponse =
                new SellerSummaryApiResponse(100L, "TestSeller", "머스트잇셀러");
        ExecutionInfoApiResponse executionApiResponse =
                new ExecutionInfoApiResponse(
                        "2025-11-21T09:00:00Z", "2025-11-20T09:00:00Z", "SUCCESS");
        SchedulerStatisticsApiResponse statisticsApiResponse =
                new SchedulerStatisticsApiResponse(150, 145, 5, 0.9667, 12500);
        List<TaskSummaryApiResponse> taskApiResponses =
                List.of(
                        new TaskSummaryApiResponse(
                                1001L,
                                "COMPLETED",
                                "PRODUCT_CRAWL",
                                "2025-11-20T09:30:00Z",
                                "2025-11-20T10:30:00Z"),
                        new TaskSummaryApiResponse(
                                1002L, "RUNNING", "PRODUCT_CRAWL", "2025-11-20T10:30:00Z", null));

        CrawlSchedulerDetailApiResponse apiResponse =
                new CrawlSchedulerDetailApiResponse(
                        crawlSchedulerId,
                        "daily-crawl",
                        "0 0 9 * * ?",
                        "ACTIVE",
                        "2025-10-21T10:30:00Z",
                        "2025-11-20T10:30:00Z",
                        sellerApiResponse,
                        executionApiResponse,
                        statisticsApiResponse,
                        taskApiResponses);

        given(searchCrawlScheduleUseCase.execute(crawlSchedulerId)).willReturn(detailResponse);
        given(crawlSchedulerQueryApiMapper.toDetailApiResponse(detailResponse))
                .willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/schedules/{crawlSchedulerId}", crawlSchedulerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.crawlSchedulerId").value(crawlSchedulerId))
                .andExpect(jsonPath("$.data.schedulerName").value("daily-crawl"))
                .andExpect(jsonPath("$.data.seller.sellerId").value(100))
                .andExpect(jsonPath("$.data.statistics.totalTasks").value(150))
                .andDo(
                        document(
                                "schedule-query/detail",
                                RestDocsSecuritySnippets.authorization("scheduler:read"),
                                pathParameters(
                                        parameterWithName("crawlSchedulerId")
                                                .description("크롤 스케줄러 ID")),
                                responseFields(
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름"),
                                        fieldWithPath("data.cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("크론 표현식"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각 (ISO-8601)"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 시각 (ISO-8601)"),
                                        // Seller
                                        fieldWithPath("data.seller")
                                                .type(JsonFieldType.OBJECT)
                                                .description("셀러 요약 정보"),
                                        fieldWithPath("data.seller.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.seller.sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("셀러 이름"),
                                        fieldWithPath("data.seller.mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러 이름"),
                                        // Execution
                                        fieldWithPath("data.execution")
                                                .type(JsonFieldType.OBJECT)
                                                .description("실행 정보"),
                                        fieldWithPath("data.execution.nextExecutionTime")
                                                .type(JsonFieldType.STRING)
                                                .description("다음 실행 예정 시각 (ISO-8601)"),
                                        fieldWithPath("data.execution.lastExecutionTime")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 실행 시각 (ISO-8601)"),
                                        fieldWithPath("data.execution.lastExecutionStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("마지막 실행 상태"),
                                        // Statistics
                                        fieldWithPath("data.statistics")
                                                .type(JsonFieldType.OBJECT)
                                                .description("통계 정보"),
                                        fieldWithPath("data.statistics.totalTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 태스크 수"),
                                        fieldWithPath("data.statistics.successTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공 태스크 수"),
                                        fieldWithPath("data.statistics.failedTasks")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실패 태스크 수"),
                                        fieldWithPath("data.statistics.successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("성공률 (0.0 ~ 1.0)"),
                                        fieldWithPath("data.statistics.avgDurationMs")
                                                .type(JsonFieldType.NUMBER)
                                                .description("평균 실행 시간 (밀리초)"),
                                        // Recent Tasks
                                        fieldWithPath("data.recentTasks")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 태스크 목록 (최대 10개)"),
                                        fieldWithPath("data.recentTasks[].taskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("태스크 ID"),
                                        fieldWithPath("data.recentTasks[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("태스크 상태"),
                                        fieldWithPath("data.recentTasks[].taskType")
                                                .type(JsonFieldType.STRING)
                                                .description("태스크 유형"),
                                        fieldWithPath("data.recentTasks[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각 (ISO-8601)"),
                                        fieldWithPath("data.recentTasks[].completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("완료 시각 (ISO-8601, null 가능)")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
