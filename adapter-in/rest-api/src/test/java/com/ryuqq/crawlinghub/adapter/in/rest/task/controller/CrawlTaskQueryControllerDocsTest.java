package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

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
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.port.in.query.SearchCrawlTaskByOffsetUseCase;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.time.Instant;
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
 * CrawlTaskQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawlTaskQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlTaskQueryController REST Docs")
class CrawlTaskQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private SearchCrawlTaskByOffsetUseCase searchCrawlTaskByOffsetUseCase;

    @MockitoBean private CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회 API 문서")
    void listCrawlTasks() throws Exception {
        // given
        List<CrawlTaskResult> results =
                List.of(
                        new CrawlTaskResult(
                                1L,
                                1L,
                                1L,
                                "https://api.example.com/products",
                                "https://api.example.com",
                                "/products",
                                Map.of(),
                                "WAITING",
                                "META",
                                0,
                                Instant.parse("2025-11-20T10:30:00Z"),
                                Instant.parse("2025-11-20T10:30:00Z")),
                        new CrawlTaskResult(
                                2L,
                                1L,
                                1L,
                                "https://api.example.com/products/detail",
                                "https://api.example.com",
                                "/products/detail",
                                Map.of(),
                                "SUCCESS",
                                "DETAIL",
                                1,
                                Instant.parse("2025-11-20T10:35:00Z"),
                                Instant.parse("2025-11-20T10:35:00Z")));

        CrawlTaskPageResult pageResult = CrawlTaskPageResult.of(results, PageMeta.of(0, 20, 2L));

        List<CrawlTaskApiResponse> apiContent =
                List.of(
                        new CrawlTaskApiResponse(
                                1L,
                                1L,
                                1L,
                                "https://api.example.com/products",
                                "https://api.example.com",
                                "/products",
                                Map.of(),
                                "PENDING",
                                "META",
                                0,
                                "2025-11-20T10:30:00Z",
                                "2025-11-20T10:30:00Z"),
                        new CrawlTaskApiResponse(
                                2L,
                                1L,
                                1L,
                                "https://api.example.com/products/detail",
                                "https://api.example.com",
                                "/products/detail",
                                Map.of(),
                                "SUCCESS",
                                "DETAIL",
                                1,
                                "2025-11-20T10:35:00Z",
                                "2025-11-20T10:35:00Z"));

        PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(crawlTaskQueryApiMapper.toSearchParams(any())).willReturn(null);
        given(searchCrawlTaskByOffsetUseCase.execute(any())).willReturn(pageResult);
        given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/tasks")
                                .param("crawlSchedulerIds", "1")
                                .param("sellerIds", "1")
                                .param("statuses", "WAITING")
                                .param("taskTypes", "META")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "task-query/list",
                                RestDocsSecuritySnippets.authorization("task:read"),
                                queryParameters(
                                        parameterWithName("crawlSchedulerIds")
                                                .description("크롤 스케줄러 ID 필터 목록 (다중 선택 가능, 선택)")
                                                .optional(),
                                        parameterWithName("sellerIds")
                                                .description("셀러 ID 필터 목록 (다중 선택 가능, 선택)")
                                                .optional(),
                                        parameterWithName("statuses")
                                                .description(
                                                        "상태 필터"
                                                            + " (WAITING/PUBLISHED/RUNNING/SUCCESS/FAILED/RETRY/TIMEOUT,"
                                                            + " 선택)")
                                                .optional(),
                                        parameterWithName("taskTypes")
                                                .description(
                                                        "태스크 유형 필터 (META/MINI_SHOP/DETAIL/OPTION,"
                                                                + " 선택)")
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
                                                .description("크롤 태스크 목록"),
                                        fieldWithPath("data.content[].crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 태스크 ID"),
                                        fieldWithPath("data.content[].crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.content[].requestUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("전체 요청 URL"),
                                        fieldWithPath("data.content[].baseUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("기본 URL"),
                                        fieldWithPath("data.content[].path")
                                                .type(JsonFieldType.STRING)
                                                .description("경로"),
                                        fieldWithPath("data.content[].queryParams")
                                                .type(JsonFieldType.OBJECT)
                                                .description("쿼리 파라미터"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "상태 (WAITING/PUBLISHED/RUNNING/SUCCESS/FAILED/RETRY/TIMEOUT)"),
                                        fieldWithPath("data.content[].taskType")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "태스크 유형 (META/MINI_SHOP/DETAIL/OPTION)"),
                                        fieldWithPath("data.content[].retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
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
}
