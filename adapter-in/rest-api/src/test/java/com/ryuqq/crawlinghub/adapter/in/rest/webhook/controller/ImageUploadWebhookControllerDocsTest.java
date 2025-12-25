package com.ryuqq.crawlinghub.adapter.in.rest.webhook.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.in.rest.common.RestDocsTestSupport;
import com.ryuqq.crawlinghub.adapter.in.rest.config.TestConfiguration;
import com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command.ImageUploadWebhookApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.webhook.mapper.WebhookApiMapper;
import com.ryuqq.crawlinghub.application.image.port.in.command.HandleImageUploadWebhookUseCase;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * ImageUploadWebhookController REST Docs 테스트
 *
 * <p>검증 범위:
 *
 * <ul>
 *   <li>이미지 업로드 웹훅 API 문서화 (성공 케이스)
 *   <li>이미지 업로드 웹훅 API 문서화 (실패 케이스)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@WebMvcTest(ImageUploadWebhookController.class)
@ContextConfiguration(classes = TestConfiguration.class)
@DisplayName("ImageUploadWebhookController REST Docs")
class ImageUploadWebhookControllerDocsTest extends RestDocsTestSupport {

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private HandleImageUploadWebhookUseCase handleImageUploadWebhookUseCase;

    @MockitoBean private WebhookApiMapper webhookApiMapper;

    @Test
    @DisplayName("POST /api/v1/webhook/image-upload - 이미지 업로드 성공 웹훅 API 문서")
    void handleImageUploadWebhook_Success() throws Exception {
        // given
        ImageUploadWebhookApiRequest request =
                new ImageUploadWebhookApiRequest(
                        "img-12345-abc",
                        "COMPLETED",
                        "https://cdn.set-of.com/images/product.jpg",
                        "asset-uuid-123",
                        null,
                        Instant.parse("2025-12-17T10:30:00Z"));

        doNothing().when(handleImageUploadWebhookUseCase).execute(any());

        // when & then
        mockMvc.perform(
                        post("/api/v1/webhook/image-upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(
                        document(
                                "webhook/image-upload-success",
                                requestFields(
                                        fieldWithPath("externalDownloadId")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "외부 다운로드 ID (Outbox idempotencyKey) - 필수"),
                                        fieldWithPath("status")
                                                .type(JsonFieldType.STRING)
                                                .description("처리 상태 (COMPLETED/FAILED) - 필수"),
                                        fieldWithPath("fileUrl")
                                                .type(JsonFieldType.STRING)
                                                .description("업로드된 파일 URL (성공 시)")
                                                .optional(),
                                        fieldWithPath("fileAssetId")
                                                .type(JsonFieldType.STRING)
                                                .description("Fileflow 파일 자산 ID")
                                                .optional(),
                                        fieldWithPath("errorMessage")
                                                .type(JsonFieldType.NULL)
                                                .description("에러 메시지 (실패 시)")
                                                .optional(),
                                        fieldWithPath("completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("완료 시각 (ISO-8601 형식)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.NULL)
                                                .description("응답 데이터 (없음)")
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
    @DisplayName("POST /api/v1/webhook/image-upload - 이미지 업로드 실패 웹훅 API 문서")
    void handleImageUploadWebhook_Failure() throws Exception {
        // given
        ImageUploadWebhookApiRequest request =
                new ImageUploadWebhookApiRequest(
                        "img-12345-def",
                        "FAILED",
                        null,
                        null,
                        "Connection timeout: Unable to download image",
                        Instant.parse("2025-12-17T10:35:00Z"));

        doNothing().when(handleImageUploadWebhookUseCase).execute(any());

        // when & then
        mockMvc.perform(
                        post("/api/v1/webhook/image-upload")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty())
                .andDo(
                        document(
                                "webhook/image-upload-failure",
                                requestFields(
                                        fieldWithPath("externalDownloadId")
                                                .type(JsonFieldType.STRING)
                                                .description(
                                                        "외부 다운로드 ID (Outbox idempotencyKey) - 필수"),
                                        fieldWithPath("status")
                                                .type(JsonFieldType.STRING)
                                                .description("처리 상태 (FAILED)"),
                                        fieldWithPath("fileUrl")
                                                .type(JsonFieldType.NULL)
                                                .description("업로드된 파일 URL (실패 시 null)")
                                                .optional(),
                                        fieldWithPath("fileAssetId")
                                                .type(JsonFieldType.NULL)
                                                .description("Fileflow 파일 자산 ID (실패 시 null)")
                                                .optional(),
                                        fieldWithPath("errorMessage")
                                                .type(JsonFieldType.STRING)
                                                .description("에러 메시지 (실패 원인)"),
                                        fieldWithPath("completedAt")
                                                .type(JsonFieldType.STRING)
                                                .description("완료 시각 (ISO-8601 형식)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("success")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("성공 여부"),
                                        fieldWithPath("data")
                                                .type(JsonFieldType.NULL)
                                                .description("응답 데이터 (없음)")
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
