package com.ryuqq.crawlinghub.application.useragent.port.out.command;

import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;

/**
 * UserAgent Pool Cache State Port (도메인 상태 동기화)
 *
 * <p>Health Score 변경 등 도메인 상태를 Redis 캐시에 동기화합니다.
 *
 * <p>비즈니스 로직(penalty 계산 등)은 도메인 VO({@code HealthScore})에서 결정하고, 이 Port는 결정된 delta 값만 적용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentPoolCacheStatePort {

    /**
     * Health Score delta 적용 (atomic)
     *
     * <p>양수 delta: Health Score 증가 (최대 100)
     *
     * <p>음수 delta: Health Score 감소 (최소 0), threshold 미만 시 SUSPENDED 처리
     *
     * @param userAgentId UserAgent ID
     * @param delta 양수: 증가, 음수: 감소 (clamp 0-100)
     * @return SUSPENDED로 변경되었으면 true (양수 delta에선 항상 false)
     */
    boolean applyHealthDelta(UserAgentId userAgentId, int delta);

    /**
     * Health Score 직접 설정 (리셋/관리 용도)
     *
     * @param userAgentId UserAgent ID
     * @param healthScore 새 Health Score 값
     */
    void setHealthScore(UserAgentId userAgentId, int healthScore);
}
