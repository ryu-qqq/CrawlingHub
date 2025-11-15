package com.ryuqq.crawlinghub.domain.schedule.outbox;

import java.time.LocalDateTime;

/**
 * Retry Policy Value Object
 *
 * <p>재시도 정책을 캡슐화한 불변 객체입니다.
 * Outbox Pattern의 재시도 로직을 관리합니다.
 *
 * <p>주요 책임:
 * <ul>
 *   <li>재시도 가능 여부 확인</li>
 *   <li>재시도 횟수 증가</li>
 *   <li>타임아웃 체크</li>
 * </ul>
 *
 * @param maxRetries 최대 재시도 횟수
 * @param retryCount 현재 재시도 횟수
 * @param timeoutMillis 타임아웃 시간 (밀리초)
 *
 * @author windsurf
 * @since 1.0.0
 */
public record RetryPolicy(
    int maxRetries,
    int retryCount,
    long timeoutMillis
) {

    /**
     * Compact Constructor - 유효성 검증
     */
    public RetryPolicy {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries는 0 이상이어야 합니다: " + maxRetries);
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount는 0 이상이어야 합니다: " + retryCount);
        }
        if (retryCount > maxRetries) {
            throw new IllegalArgumentException(
                "retryCount는 maxRetries를 초과할 수 없습니다: " + retryCount + " > " + maxRetries
            );
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeoutMillis는 0보다 커야 합니다: " + timeoutMillis);
        }
    }

    /**
     * 기본 재시도 정책 생성 (최대 3회, 60초 타임아웃)
     *
     * @return 기본 RetryPolicy
     */
    public static RetryPolicy createDefault() {
        return new RetryPolicy(3, 0, 60000L);
    }

    /**
     * 재시도 가능 여부 확인
     *
     * @return 재시도 가능하면 true
     */
    public boolean canRetry() {
        return retryCount < maxRetries;
    }

    /**
     * 재시도 횟수 증가 (불변 객체 패턴)
     *
     * @return 재시도 횟수가 증가된 새로운 RetryPolicy
     * @throws IllegalStateException 최대 재시도 횟수를 초과한 경우
     */
    public RetryPolicy incrementRetry() {
        if (!canRetry()) {
            throw new IllegalStateException(
                "최대 재시도 횟수 초과: " + retryCount + "/" + maxRetries
            );
        }
        return new RetryPolicy(maxRetries, retryCount + 1, timeoutMillis);
    }

    /**
     * 재시도 횟수 리셋
     *
     * @return 재시도 횟수가 0으로 초기화된 새로운 RetryPolicy
     */
    public RetryPolicy reset() {
        return new RetryPolicy(maxRetries, 0, timeoutMillis);
    }

    /**
     * 타임아웃 체크
     *
     * @param createdAt 생성 시간
     * @return 타임아웃이면 true
     */
    public boolean isTimeout(LocalDateTime createdAt) {
        if (createdAt == null) {
            return false;
        }
        LocalDateTime deadline = createdAt.plusSeconds(timeoutMillis / 1000);
        return LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 타임아웃 데드라인 계산
     *
     * @param createdAt 생성 시간
     * @return 타임아웃 데드라인
     */
    public LocalDateTime calculateDeadline(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt은 null일 수 없습니다");
        }
        return createdAt.plusSeconds(timeoutMillis / 1000);
    }
}
