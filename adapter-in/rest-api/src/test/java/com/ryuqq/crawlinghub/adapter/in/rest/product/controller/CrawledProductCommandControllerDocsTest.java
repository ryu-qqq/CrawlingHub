package com.ryuqq.crawlinghub.adapter.in.rest.product.controller;

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
import com.ryuqq.crawlinghub.adapter.in.rest.product.dto.response.ManualSyncTriggerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.product.mapper.CrawledProductCommandApiMapper;
import com.ryuqq.crawlinghub.application.product.dto.response.ManualSyncTriggerResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.TriggerManualSyncUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * CrawledProductCommandController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>수동 동기화 트리거 API 문서화
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(CrawledProductCommandController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("CrawledProductCommandController REST Docs")
class CrawledProductCommandControllerDocsTest extends RestDocsTestSupport {

    @MockitoBean private TriggerManualSyncUseCase triggerManualSyncUseCase;

    @MockitoBean private CrawledProductCommandApiMapper crawledProductCommandApiMapper;

    @Test
    @DisplayName("POST /api/v1/crawling/crawled-products/{id}/sync - 수동 동기화 트리거 API 문서")
    void triggerManualSync() throws Exception {
        // given
        Long crawledProductId = 1L;

        ManualSyncTriggerResponse useCaseResponse =
                new ManualSyncTriggerResponse(
                        crawledProductId, 100L, "CREATE", "동기화 요청이 정상적으로 등록되었습니다.");

        ManualSyncTriggerApiResponse apiResponse =
                new ManualSyncTriggerApiResponse(
                        crawledProductId, 100L, "CREATE", "동기화 요청이 정상적으로 등록되었습니다.");

        given(crawledProductCommandApiMapper.toTriggerManualSyncCommand(crawledProductId))
                .willReturn(null);
        given(triggerManualSyncUseCase.execute(any())).willReturn(useCaseResponse);
        given(crawledProductCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/crawled-products/{id}/sync", crawledProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawledProductId").value(crawledProductId))
                .andExpect(jsonPath("$.data.syncOutboxId").value(100))
                .andExpect(jsonPath("$.data.syncType").value("CREATE"))
                .andDo(
                        document(
                                "crawled-product-command/trigger-manual-sync",
                                RestDocsSecuritySnippets.authorization("product:update"),
                                pathParameters(
                                        parameterWithName("id").description("크롤링 상품 ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawledProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤링 상품 ID"),
                                        fieldWithPath("data.syncOutboxId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("생성된 SyncOutbox ID"),
                                        fieldWithPath("data.syncType")
                                                .type(JsonFieldType.STRING)
                                                .description("동기화 타입 (CREATE/UPDATE)"),
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
    @DisplayName("POST /api/v1/crawling/crawled-products/{id}/sync - UPDATE 타입 동기화")
    void triggerManualSync_UpdateType() throws Exception {
        // given
        Long crawledProductId = 2L;

        ManualSyncTriggerResponse useCaseResponse =
                new ManualSyncTriggerResponse(
                        crawledProductId, 101L, "UPDATE", "동기화 요청이 정상적으로 등록되었습니다.");

        ManualSyncTriggerApiResponse apiResponse =
                new ManualSyncTriggerApiResponse(
                        crawledProductId, 101L, "UPDATE", "동기화 요청이 정상적으로 등록되었습니다.");

        given(crawledProductCommandApiMapper.toTriggerManualSyncCommand(crawledProductId))
                .willReturn(null);
        given(triggerManualSyncUseCase.execute(any())).willReturn(useCaseResponse);
        given(crawledProductCommandApiMapper.toApiResponse(any())).willReturn(apiResponse);

        // when & then
        mockMvc.perform(post("/api/v1/crawling/crawled-products/{id}/sync", crawledProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.crawledProductId").value(crawledProductId))
                .andExpect(jsonPath("$.data.syncOutboxId").value(101))
                .andExpect(jsonPath("$.data.syncType").value("UPDATE"))
                .andDo(
                        document(
                                "crawled-product-command/trigger-manual-sync-update",
                                pathParameters(
                                        parameterWithName("id").description("크롤링 상품 ID (양수, 필수)")),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.OBJECT)
                                                .description("응답 데이터"),
                                        fieldWithPath("data.crawledProductId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("크롤링 상품 ID"),
                                        fieldWithPath("data.syncOutboxId")
                                                .type(JsonFieldType.NUMBER)
                                                .description("생성된 SyncOutbox ID"),
                                        fieldWithPath("data.syncType")
                                                .type(JsonFieldType.STRING)
                                                .description("동기화 타입 (CREATE/UPDATE)"),
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
