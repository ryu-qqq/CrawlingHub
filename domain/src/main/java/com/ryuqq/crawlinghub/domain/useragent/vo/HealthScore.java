package com.ryuqq.crawlinghub.domain.useragent.vo;

/**
 * UserAgent Health Score Value Object
 *
 * <p>0-100 범위의 건강 점수를 관리합니다.
 *
 * <p><strong>점수 규칙</strong>:
 *
 * <ul>
 *   <li>초기값: 100
 *   <li>성공 시: +5 (최대 100)
 *   <li>429 응답: -20
 *   <li>5xx 응답: -10
 *   <li>기타 실패: -5
 *   <li>복구 시: 70으로 리셋
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record HealthScore(int value) {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final int INITIAL_SCORE = 100;
    private static final int RECOVERY_SCORE = 70;
    private static final int SUSPENSION_THRESHOLD = 30;

    private static final int SUCCESS_INCREMENT = 5;
    private static final int RATE_LIMIT_DECREMENT = 20;
    private static final int SERVER_ERROR_DECREMENT = 10;
    private static final int OTHER_ERROR_DECREMENT = 5;

    /** Compact Constructor (검증 로직) */
    public HealthScore {
        if (value < MIN_SCORE || value > MAX_SCORE) {
            throw new IllegalArgumentException(
                    "HealthScore는 " + MIN_SCORE + "~" + MAX_SCORE + " 범위여야 합니다: " + value);
        }
    }

    /**
     * 초기 Health Score 생성 (100)
     *
     * @return HealthScore (100)
     */
    public static HealthScore initial() {
        return new HealthScore(INITIAL_SCORE);
    }

    /**
     * 복구 시 Health Score (70)
     *
     * @return HealthScore (70)
     */
    public static HealthScore recovered() {
        return new HealthScore(RECOVERY_SCORE);
    }

    /**
     * 값 기반 생성
     *
     * @param value 점수 값
     * @return HealthScore
     */
    public static HealthScore of(int value) {
        return new HealthScore(value);
    }

    /**
     * 성공 기록 (+5, 최대 100)
     *
     * @return 증가된 HealthScore
     */
    public HealthScore recordSuccess() {
        int newValue = Math.min(MAX_SCORE, this.value + SUCCESS_INCREMENT);
        return new HealthScore(newValue);
    }

    /**
     * 429 Rate Limit 응답 기록 (-20)
     *
     * @return 감소된 HealthScore
     */
    public HealthScore recordRateLimitFailure() {
        int newValue = Math.max(MIN_SCORE, this.value - RATE_LIMIT_DECREMENT);
        return new HealthScore(newValue);
    }

    /**
     * 5xx 서버 에러 응답 기록 (-10)
     *
     * @return 감소된 HealthScore
     */
    public HealthScore recordServerError() {
        int newValue = Math.max(MIN_SCORE, this.value - SERVER_ERROR_DECREMENT);
        return new HealthScore(newValue);
    }

    /**
     * 기타 에러 응답 기록 (-5)
     *
     * @return 감소된 HealthScore
     */
    public HealthScore recordOtherError() {
        int newValue = Math.max(MIN_SCORE, this.value - OTHER_ERROR_DECREMENT);
        return new HealthScore(newValue);
    }

    /**
     * 정지 임계값 미만인지 확인
     *
     * @return Health Score < 30이면 true
     */
    public boolean isBelowSuspensionThreshold() {
        return this.value < SUSPENSION_THRESHOLD;
    }

    /**
     * 정지 임계값 반환
     *
     * @return 30
     */
    public static int getSuspensionThreshold() {
        return SUSPENSION_THRESHOLD;
    }
}
