package com.ryuqq.crawlinghub.domain.useragent.vo;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 429 응답에 대한 Graduated Backoff 정책
 *
 * <p>HikariCP에서 커넥션 실패 시 exponential backoff으로 재연결하듯이, UserAgent가 429를 받으면 점진적으로 길어지는 대기 시간을 적용한다.
 *
 * <p>AWS Exponential Backoff with Jitter 패턴 적용:
 *
 * <ul>
 *   <li>base * 2^attempt (지수 증가)
 *   <li>cap으로 상한선 제한
 *   <li>jitter로 Thundering Herd 방지
 * </ul>
 *
 * <p><strong>대기 시간</strong>:
 *
 * <ul>
 *   <li>1차 429: 30초
 *   <li>2차 429: 2분
 *   <li>3차 429: 10분
 *   <li>4차+: 1시간 (cap)
 * </ul>
 *
 * <p>성공 시 카운터 완전 리셋.
 *
 * @author development-team
 * @since 1.0.0
 */
public record CooldownPolicy(int consecutiveRateLimits, Instant cooldownUntil) {

    private static final Duration BASE = Duration.ofSeconds(30);
    private static final Duration CAP = Duration.ofHours(1);
    private static final int SUSPENSION_ESCALATION_THRESHOLD = 5;

    /**
     * 쿨다운 정책 에스컬레이션
     *
     * <p>현재 연속 429 카운트를 기반으로 지수 증가 + jitter 적용
     *
     * @param currentCount 현재 연속 429 횟수
     * @param now 현재 시각
     * @return 에스컬레이트된 CooldownPolicy
     */
    public static CooldownPolicy escalate(int currentCount, Instant now) {
        long seconds =
                Math.min(BASE.toSeconds() * (1L << Math.min(currentCount, 6)), CAP.toSeconds());
        long jitter = ThreadLocalRandom.current().nextLong(-seconds / 10, seconds / 10 + 1);
        Duration cooldown = Duration.ofSeconds(seconds + jitter);
        return new CooldownPolicy(currentCount + 1, now.plus(cooldown));
    }

    /**
     * 초기 상태 (쿨다운 없음)
     *
     * @return 카운터 0, 쿨다운 시각 없는 CooldownPolicy
     */
    public static CooldownPolicy none() {
        return new CooldownPolicy(0, null);
    }

    /**
     * 기존 값으로 복원 (영속성 계층 전용)
     *
     * @param consecutiveRateLimits 연속 429 횟수
     * @param cooldownUntil 쿨다운 만료 시각 (nullable)
     * @return 복원된 CooldownPolicy
     */
    public static CooldownPolicy reconstitute(int consecutiveRateLimits, Instant cooldownUntil) {
        return new CooldownPolicy(consecutiveRateLimits, cooldownUntil);
    }

    /**
     * 쿨다운이 만료되었는지 확인
     *
     * @param now 현재 시각
     * @return 만료되었으면 true
     */
    public boolean isExpired(Instant now) {
        return cooldownUntil != null && now.isAfter(cooldownUntil);
    }

    /**
     * SUSPENDED로 에스컬레이션해야 하는지 확인
     *
     * @return 연속 429가 5회 이상이면 true
     */
    public boolean shouldEscalateToSuspended() {
        return consecutiveRateLimits >= SUSPENSION_ESCALATION_THRESHOLD;
    }
}
