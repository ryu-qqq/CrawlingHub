package com.ryuqq.crawlinghub.adapter.in.rest.schedule.controller;

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

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper.CrawlSchedulerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.port.in.query.SearchCrawlSchedulesUseCase;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.LocalDateTime;
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

    @MockitoBean private CrawlSchedulerQueryApiMapper crawlSchedulerQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/schedules - 크롤 스케줄러 목록 조회 API 문서")
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
                                LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                                null),
                        new CrawlSchedulerResponse(
                                2L,
                                1L,
                                "hourly-crawl",
                                "0 0 * * * ?",
                                SchedulerStatus.INACTIVE,
                                LocalDateTime.of(2025, 11, 19, 15, 0, 0),
                                LocalDateTime.of(2025, 11, 20, 9, 0, 0)));

        PageResponse<CrawlSchedulerResponse> pageResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<CrawlSchedulerSummaryApiResponse> apiContent =
                List.of(
                        new CrawlSchedulerSummaryApiResponse(
                                1L, 1L, "daily-crawl", "0 0 9 * * ?", "ACTIVE"),
                        new CrawlSchedulerSummaryApiResponse(
                                2L, 1L, "hourly-crawl", "0 0 * * * ?", "INACTIVE"));

        PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(crawlSchedulerQueryApiMapper.toQuery(any())).willReturn(null);
        given(searchCrawlSchedulesUseCase.execute(any())).willReturn(pageResponse);
        given(crawlSchedulerQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/schedules")
                                .param("sellerId", "1")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "schedule-query/list",
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
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
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
                                        fieldWithPath("error")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 정보")
                                                .optional(),
                                        fieldWithPath("timestamp")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 시각"),
                                        fieldWithPath("requestId")
                                                .type(JsonFieldType.STRING)
                                                .description("요청 ID"))));
    }
}
