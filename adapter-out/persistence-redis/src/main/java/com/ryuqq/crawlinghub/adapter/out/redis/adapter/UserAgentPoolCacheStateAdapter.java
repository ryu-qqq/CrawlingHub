package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolLuaScriptHolder;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheStatePort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Redis State Adapter (도메인 상태 동기화)
 *
 * <p>Health Score 변경 등 도메인 상태를 Redis에 원자적으로 동기화합니다.
 *
 * <p><strong>Phase 2 변경사항</strong>:
 *
 * <ul>
 *   <li>readySetKey -> idleSetKey 전환
 *   <li>Phase 4에서 borrow/return 패턴으로 완전 전환 시 applyHealthDelta는 returnAgent()로 대체 예정
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheStateAdapter implements UserAgentPoolCacheStatePort {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolCacheStateAdapter.class);

    private final RedissonClient redissonClient;
    private final TimeProvider timeProvider;
    private final UserAgentPoolKeyResolver keyResolver;
    private final UserAgentPoolProperties properties;
    private final UserAgentPoolLuaScriptHolder luaScriptHolder;

    public UserAgentPoolCacheStateAdapter(
            RedissonClient redissonClient,
            TimeProvider timeProvider,
            UserAgentPoolKeyResolver keyResolver,
            UserAgentPoolProperties properties,
            UserAgentPoolLuaScriptHolder luaScriptHolder) {
        this.redissonClient = redissonClient;
        this.timeProvider = timeProvider;
        this.keyResolver = keyResolver;
        this.properties = properties;
        this.luaScriptHolder = luaScriptHolder;
    }

    @Override
    public boolean applyHealthDelta(UserAgentId userAgentId, int delta) {
        if (delta > 0) {
            return applyPositiveDelta(userAgentId, delta);
        } else if (delta < 0) {
            return applyNegativeDelta(userAgentId, Math.abs(delta));
        }
        return false;
    }

    private boolean applyPositiveDelta(UserAgentId userAgentId, int delta) {
        String poolKey = keyResolver.poolKey(userAgentId);
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        script.eval(
                RScript.Mode.READ_WRITE,
                luaScriptHolder.recordSuccessScript(),
                RScript.ReturnType.INTEGER,
                List.of(poolKey),
                String.valueOf(delta));

        log.debug("UserAgent {} Health Score +{}", userAgentId.value(), delta);
        return false;
    }

    private boolean applyNegativeDelta(UserAgentId userAgentId, int penalty) {
        long nowMillis = timeProvider.now().toEpochMilli();
        String poolKey = keyResolver.poolKey(userAgentId);

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        luaScriptHolder.recordFailureScript(),
                        RScript.ReturnType.INTEGER,
                        List.of(poolKey, keyResolver.idleSetKey(), keyResolver.suspendedSetKey()),
                        String.valueOf(penalty),
                        String.valueOf(properties.getSuspensionThreshold()),
                        String.valueOf(userAgentId.value()),
                        String.valueOf(nowMillis));

        boolean suspended = result != null && result == 1L;
        if (suspended) {
            log.warn(
                    "UserAgent {} SUSPENDED (Health < {})",
                    userAgentId.value(),
                    properties.getSuspensionThreshold());
        }

        return suspended;
    }

    @Override
    public void setHealthScore(UserAgentId userAgentId, int healthScore) {
        String poolKey = keyResolver.poolKey(userAgentId);
        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("healthScore", String.valueOf(healthScore));
        log.debug("UserAgent {} Health Score 설정: {}", userAgentId.value(), healthScore);
    }
}
