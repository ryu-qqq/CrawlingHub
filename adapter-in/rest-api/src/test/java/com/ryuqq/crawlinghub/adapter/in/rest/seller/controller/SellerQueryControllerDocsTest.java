package com.ryuqq.crawlinghub.adapter.in.rest.seller.controller;

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
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerDetailStatisticsApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.SellerSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response.TaskSummaryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.seller.mapper.SellerQueryApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SchedulerSummary;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerDetailStatistics;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.dto.response.TaskSummary;
import com.ryuqq.crawlinghub.application.seller.port.in.query.GetSellerUseCase;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * SellerQueryController REST Docs 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(SellerQueryController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("SellerQueryController REST Docs")
class SellerQueryControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetSellerUseCase getSellerUseCase;

    @MockitoBean private SearchSellersUseCase searchSellersUseCase;

    @MockitoBean private SellerQueryApiMapper sellerQueryApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/sellers/{id} - 셀러 단건 조회 API 문서")
    void getSeller() throws Exception {
        // given
        Long sellerId = 1L;
        List<SchedulerSummary> schedulerSummaries =
                List.of(
                        new SchedulerSummary(
                                1L,
                                "일일 크롤링 스케줄러",
                                "ACTIVE",
                                "0 0 9 * * ?",
                                Instant.parse("2025-11-20T09:00:00Z")));
        List<TaskSummary> taskSummaries =
                List.of(
                        new TaskSummary(
                                1L, "SUCCESS", "FULL_SYNC", Instant.parse("2025-11-19T10:00:00Z")));
        SellerDetailResponse useCaseResponse =
                new SellerDetailResponse(
                        1L,
                        "머스트잇 셀러명",
                        "커머스 셀러명",
                        true,
                        Instant.parse("2025-11-19T10:30:00Z"),
                        null,
                        schedulerSummaries,
                        taskSummaries,
                        new SellerDetailStatistics(100L, 95L, 5L, 0.95));

        List<SchedulerSummaryApiResponse> schedulerApiResponses =
                List.of(
                        new SchedulerSummaryApiResponse(
                                1L,
                                "일일 크롤링 스케줄러",
                                "ACTIVE",
                                "0 0 9 * * ?",
                                "2025-11-20T09:00:00Z"));
        List<TaskSummaryApiResponse> taskApiResponses =
                List.of(
                        new TaskSummaryApiResponse(
                                1L, "SUCCESS", "FULL_SYNC", "2025-11-19T10:00:00Z"));
        SellerDetailApiResponse apiResponse =
                new SellerDetailApiResponse(
                        1L,
                        "머스트잇 셀러명",
                        "커머스 셀러명",
                        "ACTIVE",
                        "2025-11-19T10:30:00Z",
                        null,
                        schedulerApiResponses,
                        taskApiResponses,
                        new SellerDetailStatisticsApiResponse(100L, 95L, 5L, 0.95));

        given(sellerQueryApiMapper.toQuery(any(Long.class))).willReturn(null);
        given(getSellerUseCase.execute(any())).willReturn(useCaseResponse);
        given(sellerQueryApiMapper.toDetailApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/crawling/sellers/{id}", sellerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sellerId").value(1))
                .andDo(
                        document(
                                "seller-query/get",
                                RestDocsSecuritySnippets.authorization("seller:read"),
                                pathParameters(parameterWithName("id").description("셀러 ID (양수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명"),
                                        fieldWithPath("data.sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명"),
                                        fieldWithPath("data.status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (ACTIVE/INACTIVE)"),
                                        fieldWithPath("data.createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 일시"),
                                        fieldWithPath("data.updatedAt")
                                                .type(JsonFieldType.NULL)
                                                .description("수정 일시")
                                                .optional(),
                                        fieldWithPath("data.schedulers")
                                                .type(JsonFieldType.ARRAY)
                                                .description("연관 스케줄러 목록"),
                                        fieldWithPath("data.schedulers[].schedulerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("스케줄러 ID"),
                                        fieldWithPath("data.schedulers[].schedulerName")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 이름"),
                                        fieldWithPath("data.schedulers[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("스케줄러 상태"),
                                        fieldWithPath("data.schedulers[].cronExpression")
                                                .type(JsonFieldType.STRING)
                                                .description("Cron 표현식"),
                                        fieldWithPath("data.schedulers[].nextExecutionTime")
                                                .type(JsonFieldType.STRING)
                                                .description("다음 실행 예정 시각"),
                                        fieldWithPath("data.recentTasks")
                                                .type(JsonFieldType.ARRAY)
                                                .description("최근 태스크 목록"),
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
                                                .description("태스크 생성 일시"),
                                        fieldWithPath("data.statistics")
                                                .type(JsonFieldType.OBJECT)
                                                .description("셀러 상세 통계"),
                                        fieldWithPath("data.statistics.totalProducts")
                                                .type(JsonFieldType.NUMBER)
                                                .description("전체 상품 수"),
                                        fieldWithPath("data.statistics.syncedProducts")
                                                .type(JsonFieldType.NUMBER)
                                                .description("동기화 완료 상품 수"),
                                        fieldWithPath("data.statistics.pendingSyncProducts")
                                                .type(JsonFieldType.NUMBER)
                                                .description("동기화 대기 상품 수"),
                                        fieldWithPath("data.statistics.successRate")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤링 성공률"),
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
    @DisplayName("GET /api/v1/crawling/sellers - 셀러 목록 조회 API 문서")
    void listSellers() throws Exception {
        // given
        List<SellerSummaryResponse> content =
                List.of(
                        new SellerSummaryResponse(
                                1L,
                                "머스트잇 셀러1",
                                "커머스 셀러1",
                                true,
                                Instant.parse("2025-11-19T10:30:00Z"),
                                Instant.parse("2025-11-19T10:30:00Z"),
                                2,
                                3,
                                "COMPLETED",
                                Instant.parse("2025-11-19T11:00:00Z"),
                                50L),
                        new SellerSummaryResponse(
                                2L,
                                "머스트잇 셀러2",
                                "커머스 셀러2",
                                false,
                                Instant.parse("2025-11-19T10:30:00Z"),
                                null,
                                0,
                                0,
                                null,
                                null,
                                0L));

        PageResponse<SellerSummaryResponse> pageResponse =
                new PageResponse<>(content, 0, 20, 2, 1, true, true);

        List<SellerSummaryApiResponse> apiContent =
                List.of(
                        new SellerSummaryApiResponse(
                                1L,
                                "머스트잇 셀러1",
                                "커머스 셀러1",
                                "ACTIVE",
                                "2025-11-19 19:30:00",
                                "2025-11-19 19:30:00"),
                        new SellerSummaryApiResponse(
                                2L,
                                "머스트잇 셀러2",
                                "커머스 셀러2",
                                "INACTIVE",
                                "2025-11-19 19:30:00",
                                null));

        PageApiResponse<SellerSummaryApiResponse> apiPageResponse =
                new PageApiResponse<>(apiContent, 0, 20, 2, 1, true, true);

        given(
                        sellerQueryApiMapper.toQuery(
                                any(
                                        com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.query
                                                .SearchSellersApiRequest.class)))
                .willReturn(null);
        given(searchSellersUseCase.execute(any())).willReturn(pageResponse);
        given(sellerQueryApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/sellers")
                                .param("status", "ACTIVE")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "seller-query/list",
                                RestDocsSecuritySnippets.authorization("seller:read"),
                                queryParameters(
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
                                                .description("셀러 목록"),
                                        fieldWithPath("data.content[].sellerId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("셀러 ID"),
                                        fieldWithPath("data.content[].mustItSellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("머스트잇 셀러명"),
                                        fieldWithPath("data.content[].sellerName")
                                                .type(JsonFieldType.STRING)
                                                .description("커머스 셀러명"),
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
