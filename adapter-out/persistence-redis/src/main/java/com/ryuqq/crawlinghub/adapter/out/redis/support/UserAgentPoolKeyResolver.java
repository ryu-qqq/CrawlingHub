package com.ryuqq.crawlinghub.adapter.out.redis.support;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Redis Key Resolver
 *
 * <p>Redis key 이름 해석을 담당합니다.
 *
 * <p><strong>Key 구조 (Phase 2: Borrow/Return 패턴)</strong>:
 *
 * <ul>
 *   <li>{prefix}pool:{id} - UserAgent Hash
 *   <li>{prefix}idle - IDLE 상태 Set
 *   <li>{prefix}borrowed - BORROWED 상태 Set
 *   <li>{prefix}cooldown - COOLDOWN 상태 Set
 *   <li>{prefix}session_required - SESSION_REQUIRED 상태 Set
 *   <li>{prefix}suspended - SUSPENDED 상태 Set
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties(UserAgentPoolProperties.class)
public class UserAgentPoolKeyResolver {

    private final String poolKeyPrefix;
    private final String idleSetKey;
    private final String borrowedSetKey;
    private final String cooldownSetKey;
    private final String sessionRequiredSetKey;
    private final String suspendedSetKey;

    public UserAgentPoolKeyResolver(UserAgentPoolProperties properties) {
        String keyPrefix = properties.getKeyPrefix();
        this.poolKeyPrefix = keyPrefix + "pool:";
        this.idleSetKey = keyPrefix + "idle";
        this.borrowedSetKey = keyPrefix + "borrowed";
        this.cooldownSetKey = keyPrefix + "cooldown";
        this.sessionRequiredSetKey = keyPrefix + "session_required";
        this.suspendedSetKey = keyPrefix + "suspended";
    }

    public String poolKey(long userAgentId) {
        return poolKeyPrefix + userAgentId;
    }

    public String poolKey(UserAgentId id) {
        return poolKeyPrefix + id.value();
    }

    public String poolKeyPrefix() {
        return poolKeyPrefix;
    }

    public String idleSetKey() {
        return idleSetKey;
    }

    public String borrowedSetKey() {
        return borrowedSetKey;
    }

    public String cooldownSetKey() {
        return cooldownSetKey;
    }

    public String sessionRequiredSetKey() {
        return sessionRequiredSetKey;
    }

    public String suspendedSetKey() {
        return suspendedSetKey;
    }

    /**
     * @deprecated Use {@link #idleSetKey()} instead
     */
    @Deprecated
    public String readySetKey() {
        return idleSetKey;
    }
}
