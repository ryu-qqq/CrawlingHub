package com.ryuqq.crawlinghub.application.image.dto.command;

/**
 * 이미지 업로드 재시도 결과
 *
 * @param processed 처리된 건수
 * @param succeeded 성공 건수
 * @param failed 실패 건수
 * @param hasMore 추가 데이터 유무
 * @author development-team
 * @since 1.0.0
 */
public record ImageUploadRetryResult(int processed, int succeeded, int failed, boolean hasMore) {

    public static ImageUploadRetryResult empty() {
        return new ImageUploadRetryResult(0, 0, 0, false);
    }

    public static ImageUploadRetryResult of(
            int processed, int succeeded, int failed, boolean hasMore) {
        return new ImageUploadRetryResult(processed, succeeded, failed, hasMore);
    }
}
