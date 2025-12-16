package com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 이미지 업로드 웹훅 API Request
 *
 * <p>Fileflow에서 이미지 업로드 완료 시 전송하는 웹훅 요청입니다.
 *
 * @param idempotencyKey 멱등성 키 (Outbox 식별용)
 * @param eventType 이벤트 타입 (COMPLETED, FAILED)
 * @param s3Url 업로드된 S3 URL (성공 시)
 * @param errorMessage 에러 메시지 (실패 시)
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "이미지 업로드 웹훅 요청")
public record ImageUploadWebhookApiRequest(
        @Schema(
                        description = "멱등성 키",
                        example = "img-12345-abc",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "idempotencyKey는 필수입니다")
                String idempotencyKey,
        @Schema(
                        description = "이벤트 타입",
                        example = "COMPLETED",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "eventType은 필수입니다")
                String eventType,
        @Schema(
                        description = "업로드된 S3 URL (성공 시)",
                        example = "https://s3.amazonaws.com/bucket/image.jpg")
                String s3Url,
        @Schema(description = "에러 메시지 (실패 시)", example = "Download timeout") String errorMessage) {}
