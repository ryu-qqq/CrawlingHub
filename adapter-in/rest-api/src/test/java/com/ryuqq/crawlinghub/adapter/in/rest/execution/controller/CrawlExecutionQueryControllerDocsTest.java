package com.ryuqq.crawlinghub.adapter.in.rest.execution.controller;

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

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.mapper.CrawlExecutionQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.application.execution.port.in.query.GetCrawlExecutionUseCase;
import com.ryuqq.crawlinghub.application.execution.port.in.query.ListCrawlExecutionsUseCase;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * CrawlExecutionQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawlExecutionQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawlExecutionQueryController REST Docs")
class CrawlExecutionQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private ListCrawlExecutionsUseCase listCrawlExecutionsUseCase;

    @MockitoBean private GetCrawlExecutionUseCase getCrawlExecutionUseCase;

    @MockitoBean private CrawlExecutionQueryApiMapper mapper;

    @Test
    @DisplayName("GET /api/v1/executions - CrawlExecution 목록 조회 API 문서")
    void listCrawlExecutions() throws Exception {
        // given
        List<CrawlExecutionResponse> content =
                List.of(
                        new CrawlExecutionResponse(
                                1L,
                                1L,
                                1L,
                                1L,
                                CrawlExecutionStatus.SUCCESS,
                                200,
                                1500L,
                                LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                                LocalDateTime.of(2025, 11, 20, 10, 30, 1)),
                        new CrawlExecutionResponse(
                                2L,
                                2L,
                                1L,
                                1L,
                                CrawlExecutionStatus.FAILED,
                                500,
                                3000L,
                                LocalDateTime.of(2025, 11, 20, 10, 35, 0),
                                LocalDateTime.of(2025, 11, 20, 10, 35, 3)));

        PageResponse<CrawlExecutionResponse> pageResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<CrawlExecutionApiResponse> apiContent =
                List.of(
                        new CrawlExecutionApiResponse(
                                1L,
                                1L,
                                1L,
                                1L,
                                "SUCCESS",
                                200,
                                1500L,
                                LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                                LocalDateTime.of(2025, 11, 20, 10, 30, 1)),
                        new CrawlExecutionApiResponse(
                                2L,
                                2L,
                                1L,
                                1L,
                                "FAILED",
                                500,
                                3000L,
                                LocalDateTime.of(2025, 11, 20, 10, 35, 0),
                                LocalDateTime.of(2025, 11, 20, 10, 35, 3)));

        PageApiResponse<CrawlExecutionApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(mapper.toQuery(any())).willReturn(null);
        given(listCrawlExecutionsUseCase.execute(any())).willReturn(pageResponse);
        given(mapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/executions")
                                .param("crawlTaskId", "1")
                                .param("crawlSchedulerId", "1")
                                .param("sellerId", "1")
                                .param("status", "SUCCESS")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "execution-query/list",
                                queryParameters(
                                        parameterWithName("crawlTaskId")
                                                .description("태스크 ID 필터 (양수, 선택)")
                                                .optional(),
                                        parameterWithName("crawlSchedulerId")
                                                .description("스케줄러 ID 필터 (양수, 선택)")
                                                .optional(),
                                        parameterWithName("sellerId")
                                                .description("셀러 ID 필터 (양수, 선택)")
                                                .optional(),
                                        parameterWithName("status")
                                                .description(
                                                        "상태 필터 (RUNNING/SUCCESS/FAILED/TIMEOUT,"
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
                                                .description("CrawlExecution 목록"),
                                        fieldWithPath("data.content[].crawlExecutionId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlExecution ID"),
                                        fieldWithPath("data.content[].crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlTask ID"),
                                        fieldWithPath("data.content[].crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlScheduler ID"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (RUNNING/SUCCESS/FAILED/TIMEOUT)"),
                                        fieldWithPath("data.content[].httpStatusCode")
                                                .type(JsonFieldType.NUMBER)
                                                .description("HTTP 상태 코드")
                                                .optional(),
                                        fieldWithPath("data.content[].durationMs")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실행 시간 (밀리초)")
                                                .optional(),
                                        fieldWithPath("data.content[].startedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 시작 시각"),
                                        fieldWithPath("data.content[].completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 완료 시각")
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
    @DisplayName("GET /api/v1/executions/{id} - CrawlExecution 상세 조회 API 문서")
    void getCrawlExecution() throws Exception {
        // given
        Long executionId = 1L;
        CrawlExecutionDetailResponse useCaseResponse =
                new CrawlExecutionDetailResponse(
                        1L,
                        1L,
                        1L,
                        1L,
                        CrawlExecutionStatus.SUCCESS,
                        200,
                        "{\"products\": [{\"id\": 1, \"name\": \"Product1\"}]}",
                        null,
                        1500L,
                        LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                        LocalDateTime.of(2025, 11, 20, 10, 30, 1));

        CrawlExecutionDetailApiResponse apiResponse =
                new CrawlExecutionDetailApiResponse(
                        1L,
                        1L,
                        1L,
                        1L,
                        "SUCCESS",
                        200,
                        "{\"products\": [{\"id\": 1, \"name\": \"Product1\"}]}",
                        null,
                        1500L,
                        LocalDateTime.of(2025, 11, 20, 10, 30, 0),
                        LocalDateTime.of(2025, 11, 20, 10, 30, 1));

        given(mapper.toGetQuery(any())).willReturn(null);
        given(getCrawlExecutionUseCase.execute(any())).willReturn(useCaseResponse);
        given(mapper.toDetailApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/executions/{id}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlExecutionId").value(1))
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andDo(
                        document(
                                "execution-query/get",
                                pathParameters(
                                        parameterWithName("id").description("CrawlExecution ID")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlExecutionId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlExecution ID"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlTask ID"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlScheduler ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (RUNNING/SUCCESS/FAILED/TIMEOUT)"),
                                        fieldWithPath("data.httpStatusCode")
                                                .type(JsonFieldType.NUMBER)
                                                .description("HTTP 상태 코드")
                                                .optional(),
                                        fieldWithPath("data.responseBody")
                                                .type(JsonFieldType.STRING)
                                                .description("응답 본문 (크롤링 결과 JSON)")
                                                .optional(),
                                        fieldWithPath("data.errorMessage")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 메시지 (실패 시)")
                                                .optional(),
                                        fieldWithPath("data.durationMs")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실행 시간 (밀리초)")
                                                .optional(),
                                        fieldWithPath("data.startedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 시작 시각"),
                                        fieldWithPath("data.completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 완료 시각")
                                                .optional(),
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
    @DisplayName("GET /api/v1/executions/{id} - 실패한 CrawlExecution 상세 조회 API 문서")
    void getCrawlExecution_failed() throws Exception {
        // given
        Long executionId = 2L;
        CrawlExecutionDetailResponse useCaseResponse =
                new CrawlExecutionDetailResponse(
                        2L,
                        2L,
                        1L,
                        1L,
                        CrawlExecutionStatus.FAILED,
                        500,
                        null,
                        "Internal Server Error: Connection timeout",
                        3000L,
                        LocalDateTime.of(2025, 11, 20, 10, 35, 0),
                        LocalDateTime.of(2025, 11, 20, 10, 35, 3));

        CrawlExecutionDetailApiResponse apiResponse =
                new CrawlExecutionDetailApiResponse(
                        2L,
                        2L,
                        1L,
                        1L,
                        "FAILED",
                        500,
                        null,
                        "Internal Server Error: Connection timeout",
                        3000L,
                        LocalDateTime.of(2025, 11, 20, 10, 35, 0),
                        LocalDateTime.of(2025, 11, 20, 10, 35, 3));

        given(mapper.toGetQuery(any())).willReturn(null);
        given(getCrawlExecutionUseCase.execute(any())).willReturn(useCaseResponse);
        given(mapper.toDetailApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/executions/{id}", executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlExecutionId").value(2))
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(
                        jsonPath("$.data.errorMessage")
                                .value("Internal Server Error: Connection timeout"))
                .andDo(
                        document(
                                "execution-query/get-failed",
                                pathParameters(
                                        parameterWithName("id").description("CrawlExecution ID")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawlExecutionId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlExecution ID"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlTask ID"),
                                        fieldWithPath("data.crawlSchedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("CrawlScheduler ID"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (FAILED)"),
                                        fieldWithPath("data.httpStatusCode")
                                                .type(JsonFieldType.NUMBER)
                                                .description("HTTP 상태 코드 (500)")
                                                .optional(),
                                        fieldWithPath("data.responseBody")
                                                .type(JsonFieldType.NULL)
                                                .description("응답 본문 (실패 시 null)")
                                                .optional(),
                                        fieldWithPath("data.errorMessage")
                                                .type(JsonFieldType.STRING)
                                                .description("에러 메시지"),
                                        fieldWithPath("data.durationMs")
                                                .type(JsonFieldType.NUMBER)
                                                .description("실행 시간 (밀리초)")
                                                .optional(),
                                        fieldWithPath("data.startedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 시작 시각"),
                                        fieldWithPath("data.completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("실행 완료 시각")
                                                .optional(),
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
