package com.ryuqq.crawlinghub.application.image.dto.command;

/**
 * 이미지 업로드 웹훅 Command
 *
 * <p>Fileflow에서 이미지 업로드 완료 시 웹훅으로 전달되는 데이터입니다.
 *
 * @param idempotencyKey 멱등성 키 (Outbox 조회용)
 * @param eventType 이벤트 타입 (COMPLETED, FAILED)
 * @param s3Url 업로드된 S3 URL (성공 시)
 * @param errorMessage 에러 메시지 (실패 시)
 * @author development-team
 * @since 1.0.0
 */
public record ImageUploadWebhookCommand(
        String idempotencyKey, String eventType, String s3Url, String errorMessage) {

    public static ImageUploadWebhookCommand completed(String idempotencyKey, String s3Url) {
        return new ImageUploadWebhookCommand(idempotencyKey, "COMPLETED", s3Url, null);
    }

    public static ImageUploadWebhookCommand failed(String idempotencyKey, String errorMessage) {
        return new ImageUploadWebhookCommand(idempotencyKey, "FAILED", null, errorMessage);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(eventType);
    }

    public boolean isFailed() {
        return "FAILED".equals(eventType);
    }
}
