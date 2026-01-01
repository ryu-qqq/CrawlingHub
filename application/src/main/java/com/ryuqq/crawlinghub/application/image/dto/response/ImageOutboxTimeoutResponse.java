package com.ryuqq.crawlinghub.application.image.dto.response;

/**
 * 이미지 Outbox 타임아웃 처리 결과
 *
 * @param processed 처리된 Outbox 수
 * @param hasMore 추가 처리 대상 존재 여부
 */
public record ImageOutboxTimeoutResponse(int processed, boolean hasMore) {

    /**
     * 결과 생성
     *
     * @param processed 처리된 수
     * @param hasMore 추가 처리 대상 존재 여부
     * @return 결과
     */
    public static ImageOutboxTimeoutResponse of(int processed, boolean hasMore) {
        return new ImageOutboxTimeoutResponse(processed, hasMore);
    }

    /**
     * 처리 대상 없음
     *
     * @return 빈 결과
     */
    public static ImageOutboxTimeoutResponse empty() {
        return new ImageOutboxTimeoutResponse(0, false);
    }
}
