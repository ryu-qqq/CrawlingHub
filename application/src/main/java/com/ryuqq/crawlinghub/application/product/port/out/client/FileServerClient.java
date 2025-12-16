package com.ryuqq.crawlinghub.application.product.port.out.client;

import java.util.Optional;

/**
 * 파일 서버 연동 Port (Port Out - External)
 *
 * <p>이미지 파일을 S3에 업로드하기 위한 파일서버 API 연동.
 *
 * <p>비동기 방식: 업로드 요청 후 웹훅으로 결과 수신
 *
 * @author development-team
 * @since 1.0.0
 */
public interface FileServerClient {

    /**
     * 이미지 업로드 요청
     *
     * <p>비동기 방식으로 동작하며, 업로드 완료 시 웹훅으로 결과를 수신합니다.
     *
     * @param request 업로드 요청 정보
     * @return 요청 성공 여부
     */
    boolean requestImageUpload(ImageUploadRequest request);

    /**
     * 이미지 업로드 상태 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return 업로드 결과 (완료된 경우 S3 URL 포함)
     */
    Optional<ImageUploadResult> getUploadStatus(String idempotencyKey);

    /**
     * 이미지 업로드 요청 정보
     *
     * <p>callbackUrl은 Adapter 설정(Properties)에서 관리합니다.
     */
    record ImageUploadRequest(String idempotencyKey, String originalUrl, String imageType) {

        public static ImageUploadRequest of(
                String idempotencyKey, String originalUrl, String imageType) {
            return new ImageUploadRequest(idempotencyKey, originalUrl, imageType);
        }
    }

    /** 이미지 업로드 결과 */
    record ImageUploadResult(
            String idempotencyKey, UploadStatus status, String s3Url, String errorMessage) {

        public boolean isCompleted() {
            return status == UploadStatus.COMPLETED;
        }

        public boolean isFailed() {
            return status == UploadStatus.FAILED;
        }

        public boolean isPending() {
            return status == UploadStatus.PENDING || status == UploadStatus.PROCESSING;
        }

        public static ImageUploadResult completed(String idempotencyKey, String s3Url) {
            return new ImageUploadResult(idempotencyKey, UploadStatus.COMPLETED, s3Url, null);
        }

        public static ImageUploadResult failed(String idempotencyKey, String errorMessage) {
            return new ImageUploadResult(idempotencyKey, UploadStatus.FAILED, null, errorMessage);
        }

        public static ImageUploadResult pending(String idempotencyKey) {
            return new ImageUploadResult(idempotencyKey, UploadStatus.PENDING, null, null);
        }
    }

    /** 업로드 상태 */
    enum UploadStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
