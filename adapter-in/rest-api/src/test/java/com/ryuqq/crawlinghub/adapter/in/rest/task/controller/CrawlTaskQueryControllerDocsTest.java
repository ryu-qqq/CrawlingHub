package com.ryuqq.crawlinghub.adapter.in.rest.task.controller;

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
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetCrawlTaskUseCase;
import com.ryuqq.crawlinghub.application.task.port.in.query.ListCrawlTasksUseCase;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlTaskQueryController REST Docs")
class CrawlTaskQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private ListCrawlTasksUseCase listCrawlTasksUseCase;

    @MockitoBean private GetCrawlTaskUseCase getCrawlTaskUseCase;

    @MockitoBean private CrawlTaskQueryApiMapper crawlTaskQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/tasks - 크롤 태스크 목록 조회 API 문서")
    void listCrawlTasks() throws Exception {
        // given
        List<CrawlTaskResponse> content =
                List.of(
                        new CrawlTaskResponse(
                                1L,
                                1L,
                                1L,
                                "https://api.example.com/products",
                                CrawlTaskStatus.WAITING,
                                CrawlTaskType.META,
                                0,
                                Instant.parse("2025-11-20T10:30:00Z")),
                        new CrawlTaskResponse(
                                2L,
                                1L,
                                1L,
                                "https://api.example.com/products/detail",
                                CrawlTaskStatus.SUCCESS,
                                CrawlTaskType.DETAIL,
                                1,
                                Instant.parse("2025-11-20T10:35:00Z")));

        PageResponse<CrawlTaskResponse> pageResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<CrawlTaskApiResponse> apiContent =
                List.of(
                        new CrawlTaskApiResponse(
                                1L,
                                1L,
                                1L,
                                "https://api.example.com/products",
                                "PENDING",
                                "META",
                                0,
                                "2025-11-20T10:30:00Z"),
                        new CrawlTaskApiResponse(
                                2L,
                                1L,
                                1L,
                                "https://api.example.com/products/detail",
                                "SUCCESS",
                                "DETAIL",
                                1,
                                "2025-11-20T10:35:00Z"));

        PageApiResponse<CrawlTaskApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(crawlTaskQueryApiMapper.toQuery(any())).willReturn(null);
        given(listCrawlTasksUseCase.execute(any())).willReturn(pageResponse);
        given(crawlTaskQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/tasks")
                                .param("crawlSchedulerId", "1")
                                .param("sellerId", "1")
                                .param("status", "PENDING")
                                .param("taskType", "META")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "task-query/list",
                                RestDocsSecuritySnippets.authorization("task:read"),
                                queryParameters(
                                        parameterWithName("crawlSchedulerId")
                                                .description("크롤 스케줄러 ID 필터 (양수, 선택)"),
                                        parameterWithName("sellerId")
                                                .description("셀러 ID 필터 (양수, 선택)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description(
                                                        "상태 필터"
                                                            + " (PENDING/RUNNING/SUCCESS/FAILED/CANCELLED,"
                                                            + " 선택)")
                                                .optional(),
                                        parameterWithName("taskType")
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
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
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
                                                .description("요청 URL"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "상태 (PENDING/RUNNING/SUCCESS/FAILED/CANCELLED)"),
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

    @Test
    @DisplayName("GET /api/v1/crawling/tasks/{id} - 크롤 태스크 상세 조회 API 문서")
    void getCrawlTask() throws Exception {
        // given
        Long taskId = 1L;
        CrawlTaskDetailResponse useCaseResponse =
                new CrawlTaskDetailResponse(
                        1L,
                        1L,
                        1L,
                        CrawlTaskStatus.WAITING,
                        CrawlTaskType.META,
                        0,
                        "https://api.example.com",
                        "/products",
                        Map.of("page", "1", "size", "100"),
                        "https://api.example.com/products?page=1&size=100",
                        Instant.parse("2025-11-20T10:30:00Z"),
                        Instant.parse("2025-11-20T10:30:00Z"));

        CrawlTaskDetailApiResponse apiResponse =
                new CrawlTaskDetailApiResponse(
                        1L,
                        1L,
                        1L,
                        "PENDING",
                        "META",
                        0,
                        "https://api.example.com",
                        "/products",
                        Map.of("page", "1", "size", "100"),
                        "https://api.example.com/products?page=1&size=100",
                        "2025-11-20T10:30:00Z",
                        "2025-11-20T10:30:00Z");

        given(crawlTaskQueryApiMapper.toGetQuery(any())).willReturn(null);
        given(getCrawlTaskUseCase.execute(any())).willReturn(useCaseResponse);
        given(crawlTaskQueryApiMapper.toDetailApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlTaskId").value(1))
                .andDo(
                        document(
                                "task-query/get",
                                RestDocsSecuritySnippets.authorization("task:read"),
                                pathParameters(parameterWithName("id").description("크롤 태스크 ID")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 태스크 ID"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤 스케줄러 ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "상태 (PENDING/RUNNING/SUCCESS/FAILED/CANCELLED)"),
                                        fieldWithPath("data.taskType")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "태스크 유형 (META/MINI_SHOP/DETAIL/OPTION)"),
                                        fieldWithPath("data.retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.baseUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("기본 URL"),
                                        fieldWithPath("data.path")
                                                .type(JsonFieldType.STRING)
                                                .description("경로"),
                                        fieldWithPath("data.queryParams")
                                                .type(JsonFieldType.OBJECT)
                                                .description("쿼리 파라미터"),
                                        fieldWithPath("data.queryParams.page")
                                                .type(JsonFieldType.STRING)
                                                .description("페이지 파라미터"),
                                        fieldWithPath("data.queryParams.size")
                                                .type(JsonFieldType.STRING)
                                                .description("크기 파라미터"),
                                        fieldWithPath("data.fullUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("전체 URL"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("수정 시각"),
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
