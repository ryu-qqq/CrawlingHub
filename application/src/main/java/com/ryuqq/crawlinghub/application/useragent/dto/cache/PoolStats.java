package com.ryuqq.crawlinghub.application.useragent.dto.cache;

/**
 * UserAgent Pool 통계 DTO
 *
 * <p>Redis에서 가져온 Pool 상태 통계 정보입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public record PoolStats(
        long total,
        long available,
        long suspended,
        double avgHealthScore,
        int minHealthScore,
        int maxHealthScore) {
    /** 빈 통계 (Pool이 비어있을 때) */
    public static PoolStats empty() {
        return new PoolStats(0, 0, 0, 0.0, 0, 0);
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
