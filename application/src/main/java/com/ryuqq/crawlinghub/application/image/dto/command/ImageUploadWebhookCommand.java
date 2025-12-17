package com.ryuqq.crawlinghub.application.image.dto.command;

import java.time.Instant;

/**
 * 이미지 업로드 웹훅 Command
 *
 * <p>Fileflow에서 이미지 업로드 완료 시 웹훅으로 전달되는 데이터입니다.
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
public record ImageUploadWebhookCommand(
        String externalDownloadId,
        String status,
        String fileUrl,
        String fileAssetId,
        String errorMessage,
        Instant completedAt) {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    public static ImageUploadWebhookCommand completed(
            String externalDownloadId, String fileUrl, String fileAssetId, Instant completedAt) {
        return new ImageUploadWebhookCommand(
                externalDownloadId, STATUS_COMPLETED, fileUrl, fileAssetId, null, completedAt);
    }

    public static ImageUploadWebhookCommand failed(
            String externalDownloadId, String errorMessage, Instant completedAt) {
        return new ImageUploadWebhookCommand(
                externalDownloadId, STATUS_FAILED, null, null, errorMessage, completedAt);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(status);
    }
}
