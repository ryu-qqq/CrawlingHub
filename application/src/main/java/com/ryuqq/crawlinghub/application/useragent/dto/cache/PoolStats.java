package com.ryuqq.crawlinghub.application.useragent.dto.cache;

/**
 * UserAgent Pool 통계 DTO
 *
 * <p>Redis에서 가져온 Pool 상태 통계 정보입니다.
 *
 * <p><strong>상태별 카운트</strong>:
 *
 * <ul>
 *   <li>available (IDLE): 즉시 borrow 가능한 UserAgent 수
 *   <li>borrowed: 현재 크롤링에 사용 중인 UserAgent 수
 *   <li>cooldown: COOLDOWN 대기 중인 UserAgent 수
 *   <li>suspended: 일시 정지 상태 UserAgent 수
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public record PoolStats(
        long total,
        long available,
        long borrowed,
        long cooldown,
        long suspended,
        double avgHealthScore,
        int minHealthScore,
        int maxHealthScore) {
    /** 빈 통계 (Pool이 비어있을 때) */
    public static PoolStats empty() {
        return new PoolStats(0, 0, 0, 0, 0, 0.0, 0, 0);
    }

    /**
     * 가용률 계산
     *
     * @return 가용률 (0-100%)
     */
    public double availableRate() {
        return total > 0 ? (double) available / total * 100 : 0.0;
    }
}
