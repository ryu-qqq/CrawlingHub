package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheStatePort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Cache State Manager
 *
 * <p><strong>책임</strong>: 도메인 상태 동기화 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 StatePort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheStateManager {

    private final UserAgentPoolCacheStatePort statePort;

    public UserAgentPoolCacheStateManager(UserAgentPoolCacheStatePort statePort) {
        this.statePort = statePort;
    }

    /**
     * Health Score delta 적용
     *
     * @param userAgentId UserAgent ID
     * @param delta 양수: 증가, 음수: 감소
     * @return SUSPENDED로 변경되었으면 true
     */
    public boolean applyHealthDelta(UserAgentId userAgentId, int delta) {
        return statePort.applyHealthDelta(userAgentId, delta);
    }

    /**
     * Health Score 직접 설정
     *
     * @param userAgentId UserAgent ID
     * @param healthScore 새 Health Score 값
     */
    public void setHealthScore(UserAgentId userAgentId, int healthScore) {
        statePort.setHealthScore(userAgentId, healthScore);
    }
}
