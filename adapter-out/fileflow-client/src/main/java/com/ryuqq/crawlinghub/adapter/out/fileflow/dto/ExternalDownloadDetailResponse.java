package com.ryuqq.crawlinghub.adapter.out.fileflow.dto;

import java.time.Instant;

/**
 * Fileflow 외부 다운로드 상세 응답 DTO
 *
 * <p>Fileflow API의 ExternalDownloadDetailApiResponse에 매핑됩니다.
 *
 * @param id ExternalDownload ID
 * @param sourceUrl 외부 이미지 URL
 * @param status 현재 상태 (PENDING, PROCESSING, COMPLETED, FAILED)
 * @param fileAssetId 생성된 FileAsset ID (완료 시)
 * @param errorMessage 에러 메시지 (실패 시)
 * @param retryCount 재시도 횟수
 * @param webhookUrl 콜백 URL
 * @param createdAt 생성 시간
 * @param updatedAt 수정 시간
 * @author development-team
 * @since 1.0.0
 */
public record ExternalDownloadDetailResponse(
        String id,
        String sourceUrl,
        String status,
        String fileAssetId,
        String errorMessage,
        int retryCount,
        String webhookUrl,
        Instant createdAt,
        Instant updatedAt) {

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status) || "PROCESSING".equals(status);
    }
}
