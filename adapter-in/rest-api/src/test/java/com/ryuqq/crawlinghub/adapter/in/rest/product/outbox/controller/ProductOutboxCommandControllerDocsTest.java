package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsSecuritySnippets;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.OutboxRetryApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper.ProductOutboxCommandApiMapper;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetryImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetrySyncOutboxUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * ProductOutboxCommandController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>SyncOutbox 재시도 API 문서화
 *   <li>ImageOutbox 재시도 API 문서화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(ProductOutboxCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("ProductOutboxCommandController REST Docs")
class ProductOutboxCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private RetrySyncOutboxUseCase retrySyncOutboxUseCase;

    @MockitoBean private RetryImageOutboxUseCase retryImageOutboxUseCase;

    @MockitoBean private ProductOutboxCommandApiMapper productOutboxCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/product-outbox/sync/{id}/retry - SyncOutbox 재시도 API 문서")
    void retrySyncOutbox() throws Exception {
        // given
        Long outboxId = 1L;

        OutboxRetryResponse useCaseResponse =
                new OutboxRetryResponse(outboxId, "FAILED", "PENDING", "재시도 요청이 정상적으로 등록되었습니다.");

        OutboxRetryApiResponse apiResponse =
                new OutboxRetryApiResponse(outboxId, "FAILED", "PENDING", "재시도 요청이 정상적으로 등록되었습니다.");

        given(productOutboxCommandApiMapper.toRetrySyncOutboxCommand(outboxId)).willReturn(null);
        given(retrySyncOutboxUseCase.execute(any())).willReturn(useCaseResponse);
        given(productOutboxCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/product-outbox/sync/{id}/retry", outboxId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outboxId").value(outboxId))
                .andExpect(jsonPath("$.data.previousStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.newStatus").value("PENDING"))
                .andDo(
                        document(
                                "product-outbox-command/retry-sync",
                                RestDocsSecuritySnippets.authorization("outbox:update"),
                                pathParameters(
                                        parameterWithName("id")
                                                .description("SyncOutbox ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.outboxId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Outbox ID"),
                                        fieldWithPath("data.previousStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("이전 상태 (FAILED)"),
                                        fieldWithPath("data.newStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("새 상태 (PENDING)"),
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
    @DisplayName("POST /api/v1/crawling/product-outbox/image/{id}/retry - ImageOutbox 재시도 API 문서")
    void retryImageOutbox() throws Exception {
        // given
        Long outboxId = 2L;

        OutboxRetryResponse useCaseResponse =
                new OutboxRetryResponse(outboxId, "FAILED", "PENDING", "재시도 요청이 정상적으로 등록되었습니다.");

        OutboxRetryApiResponse apiResponse =
                new OutboxRetryApiResponse(outboxId, "FAILED", "PENDING", "재시도 요청이 정상적으로 등록되었습니다.");

        given(productOutboxCommandApiMapper.toRetryImageOutboxCommand(outboxId)).willReturn(null);
        given(retryImageOutboxUseCase.execute(any())).willReturn(useCaseResponse);
        given(productOutboxCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/product-outbox/image/{id}/retry", outboxId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.outboxId").value(outboxId))
                .andExpect(jsonPath("$.data.previousStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.newStatus").value("PENDING"))
                .andDo(
                        document(
                                "product-outbox-command/retry-image",
                                RestDocsSecuritySnippets.authorization("outbox:update"),
                                pathParameters(
                                        parameterWithName("id")
                                                .description("ImageOutbox ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.outboxId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("Outbox ID"),
                                        fieldWithPath("data.previousStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("이전 상태 (FAILED)"),
                                        fieldWithPath("data.newStatus")
                                                .type(JsonFieldType.STRING)
                                                .description("새 상태 (PENDING)"),
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
}
