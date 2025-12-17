package com.ryuqq.crawlinghub.adapter.in.rest.webhook.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * 이미지 업로드 웹훅 API Request
 *
 * <p>Fileflow에서 이미지 업로드 완료 시 전송하는 웹훅 요청입니다.
 *
 * <p>Fileflow 콜백 형식:
 *
 * <pre>
 * {
 *   "externalDownloadId": "img-123-abc",
 *   "status": "COMPLETED",
 *   "fileUrl": "https://cdn.set-of.com/...",
 *   "fileAssetId": "asset-uuid",
 *   "errorMessage": null,
 *   "completedAt": "2025-12-17T10:30:00Z"
 * }
 * </pre>
 *
 * @param externalDownloadId 외부 다운로드 ID (Outbox idempotencyKey)
 * @param status 처리 상태 (COMPLETED, FAILED)
 * @param fileUrl 업로드된 파일 URL (성공 시)
 * @param fileAssetId Fileflow 파일 자산 ID (나중에 파일 조회용)
 * @param errorMessage 에러 메시지 (실패 시)
 * @param completedAt 완료 시각
 * @author development-team
 * @since 1.0.0
 */
@Schema(description = "이미지 업로드 웹훅 요청")
public record ImageUploadWebhookApiRequest(
        @Schema(
                        description = "외부 다운로드 ID (Outbox idempotencyKey)",
                        example = "img-12345-abc",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "externalDownloadId는 필수입니다")
                String externalDownloadId,
        @Schema(
                        description = "처리 상태",
                        example = "COMPLETED",
                        requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "status는 필수입니다")
                String status,
        @Schema(
                        description = "업로드된 파일 URL (성공 시)",
                        example = "https://cdn.set-of.com/images/product.jpg")
                String fileUrl,
        @Schema(description = "Fileflow 파일 자산 ID", example = "asset-uuid-123") String fileAssetId,
        @Schema(description = "에러 메시지 (실패 시)", example = "Connection timeout") String errorMessage,
        @Schema(description = "완료 시각", example = "2025-12-17T10:30:00Z") Instant completedAt) {}
