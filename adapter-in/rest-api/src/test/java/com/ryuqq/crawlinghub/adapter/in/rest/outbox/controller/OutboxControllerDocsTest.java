package com.ryuqq.crawlinghub.adapter.in.rest.outbox.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
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
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper.OutboxApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.application.outbox.port.in.command.RepublishOutboxUseCase;
import com.ryuqq.crawlinghub.application.outbox.port.in.query.GetOutboxListUseCase;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * OutboxController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>Outbox 목록 조회 API 문서화 (페이징)
 *   <li>Outbox 재발행 API 문서화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(OutboxController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@TestPropertySource(properties = "app.messaging.sqs.enabled=true")
@DisplayName("OutboxController REST Docs")
class OutboxControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private GetOutboxListUseCase getOutboxListUseCase;

    @MockitoBean private RepublishOutboxUseCase republishOutboxUseCase;

    @MockitoBean private OutboxApiMapper outboxApiMapper;

    @Test
    @DisplayName("GET /api/v1/crawling/outbox - Outbox 목록 조회 API 문서 (페이징)")
    void getOutboxList() throws Exception {
        // given
        Instant now = Instant.now();

        OutboxResponse response1 =
                new OutboxResponse(1L, "outbox-1-abc12345", OutboxStatus.PENDING, 0, now, null);

        OutboxResponse response2 =
                new OutboxResponse(
                        2L,
                        "outbox-2-def67890",
                        OutboxStatus.FAILED,
                        3,
                        now.minusSeconds(3600),
                        now);

        PageResponse<OutboxResponse> pageResponse =
                new PageResponse<>(List.of(response1, response2), 0, 20, 2, 1, true, true);

        OutboxApiResponse apiResponse1 =
                new OutboxApiResponse(1L, "outbox-1-abc12345", "PENDING", 0, now.toString(), null);

        OutboxApiResponse apiResponse2 =
                new OutboxApiResponse(
                        2L,
                        "outbox-2-def67890",
                        "FAILED",
                        3,
                        now.minusSeconds(3600).toString(),
                        now.toString());

        PageApiResponse<OutboxApiResponse> apiPageResponse =
                new PageApiResponse<>(List.of(apiResponse1, apiResponse2), 0, 20, 2, 1, true, true);

        GetOutboxListQuery query = GetOutboxListQuery.pendingOrFailed(0, 20);
        given(outboxApiMapper.toQuery(anyList(), any(), any(), anyInt(), anyInt()))
                .willReturn(query);
        given(getOutboxListUseCase.execute(any())).willReturn(pageResponse);
        given(outboxApiMapper.toPageApiResponse(any())).willReturn(apiPageResponse);

        // when & then
        mockMvc.perform(
                        get("/api/v1/crawling/outbox")
                                .param("statuses", "FAILED")
                                .param("createdFrom", "2024-01-01T00:00:00Z")
                                .param("createdTo", "2024-12-31T23:59:59Z")
                                .param("page", "0")
                                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andDo(
                        document(
                                "outbox/get-list",
                                RestDocsSecuritySnippets.authorization("outbox:read"),
                                queryParameters(
                                        parameterWithName("statuses")
                                                .description(
                                                        "상태 필터 목록 (PENDING, FAILED, SENT) - 다중 선택"
                                                                + " 가능 (선택)")
                                                .optional(),
                                        parameterWithName("createdFrom")
                                                .description(
                                                        "생성일 시작 범위 (ISO-8601 형식, inclusive) (선택)")
                                                .optional(),
                                        parameterWithName("createdTo")
                                                .description(
                                                        "생성일 종료 범위 (ISO-8601 형식, exclusive) (선택)")
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
                                                .description("페이징 응답 데이터"),
                                        fieldWithPath("data.content")
                                                .type(JsonFieldType.ARRAY)
                                                .description("Outbox 목록"),
                                        fieldWithPath("data.content[].crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Task ID"),
                                        fieldWithPath("data.content[].idempotencyKey")
                                                .type(JsonFieldType.STRING)
                                                .description("멱등성 키"),
                                        fieldWithPath("data.content[].status")
                                                .type(JsonFieldType.STRING)
                                                .description("상태 (PENDING, FAILED, SENT)"),
                                        fieldWithPath("data.content[].retryCount")
                                                .type(JsonFieldType.NUMBER)
                                                .description("재시도 횟수"),
                                        fieldWithPath("data.content[].createdAt")
                                                .type(JsonFieldType.STRING)
                                                .description("생성 시각"),
                                        fieldWithPath("data.content[].processedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("처리 시각")
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
    @DisplayName("POST /api/v1/crawling/outbox/{crawlTaskId}/republish - Outbox 재발행 API 문서")
    void republishOutbox() throws Exception {
        // given
        Long crawlTaskId = 1L;

        RepublishResultResponse useCaseResponse =
                new RepublishResultResponse(crawlTaskId, true, "SQS 재발행이 완료되었습니다.");

        RepublishResultApiResponse apiResponse =
                new RepublishResultApiResponse(crawlTaskId, true, "SQS 재발행이 완료되었습니다.");

        given(republishOutboxUseCase.republish(anyLong())).willReturn(useCaseResponse);
        given(outboxApiMapper.toRepublishApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/outbox/{crawlTaskId}/republish", crawlTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlTaskId").value(crawlTaskId))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("SQS 재발행이 완료되었습니다."))
                .andDo(
                        document(
                                "outbox/republish",
                                RestDocsSecuritySnippets.authorization("outbox:update"),
                                pathParameters(
                                        parameterWithName("crawlTaskId")
                                                .description("재발행할 Task ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("API 호출 성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("재발행 결과 데이터"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Task ID"),
                                        fieldWithPath("data.success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("재발행 성공 여부"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("결과 메시지"),
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
    @DisplayName("POST /api/v1/crawling/outbox/{crawlTaskId}/republish - 재발행 실패 케이스")
    void republishOutbox_Failure() throws Exception {
        // given
        Long crawlTaskId = 999L;

        RepublishResultResponse useCaseResponse =
                new RepublishResultResponse(
                        crawlTaskId, false, "해당 Task ID에 대한 Outbox를 찾을 수 없습니다.");

        RepublishResultApiResponse apiResponse =
                new RepublishResultApiResponse(
                        crawlTaskId, false, "해당 Task ID에 대한 Outbox를 찾을 수 없습니다.");

        given(republishOutboxUseCase.republish(anyLong())).willReturn(useCaseResponse);
        given(outboxApiMapper.toRepublishApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/outbox/{crawlTaskId}/republish", crawlTaskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawlTaskId").value(crawlTaskId))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.message").value("해당 Task ID에 대한 Outbox를 찾을 수 없습니다."))
                .andDo(
                        document(
                                "outbox/republish-failure",
                                pathParameters(
                                        parameterWithName("crawlTaskId")
                                                .description("재발행할 Task ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("API 호출 성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("재발행 결과 데이터"),
                                        fieldWithPath("data.crawlTaskId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Task ID"),
                                        fieldWithPath("data.success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("재발행 성공 여부 (false)"),
                                        fieldWithPath("data.message")
                                                .type(JsonFieldType.STRING)
                                                .description("실패 사유"),
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
