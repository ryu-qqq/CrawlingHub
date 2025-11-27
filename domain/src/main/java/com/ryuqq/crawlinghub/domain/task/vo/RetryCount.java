package com.ryuqq.crawlinghub.domain.task.vo;

/**
 * 재시도 횟수 Value Object
 *
 * <p>최대 재시도 횟수는 2회로 제한됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record RetryCount(int value) {

    public static final int MAX_RETRY_COUNT = 2;

    public RetryCount {
        if (value < 0) {
            throw new IllegalArgumentException("RetryCount는 0 이상이어야 합니다: " + value);
        }
        if (value > MAX_RETRY_COUNT) {
            throw new IllegalArgumentException(
                    "RetryCount는 최대 " + MAX_RETRY_COUNT + "회를 초과할 수 없습니다: " + value);
        }
    }

    /** 초기 재시도 횟수 (0) */
    public static RetryCount zero() {
        return new RetryCount(0);
    }

    /** 재시도 가능 여부 */
    public boolean canRetry() {
        return value < MAX_RETRY_COUNT;
    }

    /**
     * 재시도 횟수 증가
     *
     * @return 증가된 RetryCount
     * @throws IllegalArgumentException 최대 횟수 초과 시
     */
    public RetryCount increment() {
        return new RetryCount(value + 1);
    }
}
