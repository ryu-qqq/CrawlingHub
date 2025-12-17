package com.ryuqq.crawlinghub.application.sync.dto.command;

/**
 * 외부 서버 동기화 재시도 결과
 *
 * @param processed 처리된 건수
 * @param succeeded 성공 건수
 * @param failed 실패 건수
 * @param hasMore 추가 데이터 유무
 * @author development-team
 * @since 1.0.0
 */
public record SyncRetryResult(int processed, int succeeded, int failed, boolean hasMore) {

    public static SyncRetryResult empty() {
        return new SyncRetryResult(0, 0, 0, false);
    }

    public static SyncRetryResult of(int processed, int succeeded, int failed, boolean hasMore) {
        return new SyncRetryResult(processed, succeeded, failed, hasMore);
    }
}
