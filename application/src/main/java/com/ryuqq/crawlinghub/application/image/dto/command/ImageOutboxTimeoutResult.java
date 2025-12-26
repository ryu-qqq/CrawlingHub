package com.ryuqq.crawlinghub.application.image.dto.command;

/**
 * 이미지 Outbox 타임아웃 처리 결과
 *
 * @param processed 처리된 Outbox 수
 * @param hasMore 추가 처리 대상 존재 여부
 */
public record ImageOutboxTimeoutResult(int processed, boolean hasMore) {

    /**
     * 결과 생성
     *
     * @param processed 처리된 수
     * @param hasMore 추가 처리 대상 존재 여부
     * @return 결과
     */
    public static ImageOutboxTimeoutResult of(int processed, boolean hasMore) {
        return new ImageOutboxTimeoutResult(processed, hasMore);
    }

    /**
     * 처리 대상 없음
     *
     * @return 빈 결과
     */
    public static ImageOutboxTimeoutResult empty() {
        return new ImageOutboxTimeoutResult(0, false);
    }
}
