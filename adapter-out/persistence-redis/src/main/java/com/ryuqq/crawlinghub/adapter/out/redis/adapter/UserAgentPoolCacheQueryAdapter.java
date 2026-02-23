package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.CachedUserAgentRedisMapper;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.port.out.query.UserAgentPoolCacheQueryPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Redis Query Adapter
 *
 * <p>Redis 기반 UserAgent Pool 조회를 담당합니다.
 *
 * <p><strong>Phase 2 변경사항</strong>:
 *
 * <ul>
 *   <li>readySetKey -> idleSetKey 전환
 *   <li>getPoolStats()에 borrowed, cooldown 수 포함 (PoolStats record는 Phase 4에서 확장)
 *   <li>getCooldownUserAgents() 메서드 추가
 *   <li>getBorrowedUserAgents() 메서드 추가
 *   <li>getAllUserAgentIds()에 borrowed, cooldown Set 포함
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheQueryAdapter implements UserAgentPoolCacheQueryPort {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolCacheQueryAdapter.class);

    private final RedissonClient redissonClient;
    private final TimeProvider timeProvider;
    private final UserAgentPoolKeyResolver keyResolver;
    private final CachedUserAgentRedisMapper mapper;
    private final UserAgentPoolProperties properties;

    public UserAgentPoolCacheQueryAdapter(
            RedissonClient redissonClient,
            TimeProvider timeProvider,
            UserAgentPoolKeyResolver keyResolver,
            CachedUserAgentRedisMapper mapper,
            UserAgentPoolProperties properties) {
        this.redissonClient = redissonClient;
        this.timeProvider = timeProvider;
        this.keyResolver = keyResolver;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public PoolStats getPoolStats() {
        var idleSet = redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE);
        var borrowedSet = redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE);
        var cooldownSet = redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE);
        var sessionRequiredSet =
                redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE);
        var suspendedSet =
                redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE);

        int idleCount = idleSet.size();
        int borrowedCount = borrowedSet.size();
        int cooldownCount = cooldownSet.size();
        int sessionRequiredCount = sessionRequiredSet.size();
        int suspendedCount = suspendedSet.size();

        // Phase 2: total에 borrowed, cooldown 포함
        int total =
                idleCount + borrowedCount + cooldownCount + sessionRequiredCount + suspendedCount;

        // available = idle + borrowed + cooldown (활성 상태)
        int available = idleCount + borrowedCount + cooldownCount;

        int sumHealth = 0;
        int minHealth = 100;
        int maxHealth = 0;
        int count = 0;

        // Phase 2: idle Set 기준으로 Health 통계 계산
        for (Object idObj : idleSet.readAll()) {
            String poolKey = keyResolver.poolKey(Long.parseLong(idObj.toString()));
            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String healthStr = map.get("healthScore");
            if (healthStr != null) {
                int health = Integer.parseInt(healthStr);
                sumHealth += health;
                minHealth = Math.min(minHealth, health);
                maxHealth = Math.max(maxHealth, health);
                count++;
            }
        }

        double avgHealth = count > 0 ? (double) sumHealth / count : 0;

        return new PoolStats(
                total,
                available,
                borrowedCount,
                cooldownCount,
                suspendedCount,
                avgHealth,
                count > 0 ? minHealth : 0,
                count > 0 ? maxHealth : 0);
    }

    @Override
    public Optional<CachedUserAgent> findById(UserAgentId userAgentId) {
        String poolKey = keyResolver.poolKey(userAgentId);
        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);

        Map<String, String> data = map.readAllMap();
        if (data.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapper.mapToCachedUserAgent(data));
    }

    @Override
    public List<UserAgentId> getSessionRequiredUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        var sessionRequiredSet =
                redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE);

        for (Object idObj : sessionRequiredSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            result.add(UserAgentId.of(id));
        }

        return result;
    }

    @Override
    public List<UserAgentId> getRecoverableUserAgents() {
        Instant now = timeProvider.now();
        long thresholdMillis = now.minus(Duration.ofHours(1)).toEpochMilli();
        List<UserAgentId> recoverableIds = new ArrayList<>();

        var suspendedSet =
                redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE);
        for (Object idObj : suspendedSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            String poolKey = keyResolver.poolKey(id);

            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String suspendedAtStr = map.get("suspendedAt");
            String healthScoreStr = map.get("healthScore");

            if (suspendedAtStr == null || "0".equals(suspendedAtStr)) {
                continue;
            }

            long suspendedAtMillis = Long.parseLong(suspendedAtStr);
            int healthScore = Integer.parseInt(healthScoreStr != null ? healthScoreStr : "0");

            if (suspendedAtMillis < thresholdMillis
                    && healthScore >= properties.getSuspensionThreshold()) {
                recoverableIds.add(UserAgentId.of(id));
            }
        }

        return recoverableIds;
    }

    @Override
    public List<UserAgentId> getSessionExpiringUserAgents(int bufferMinutes) {
        Instant now = timeProvider.now();
        long thresholdMillis = now.plus(Duration.ofMinutes(bufferMinutes)).toEpochMilli();
        List<UserAgentId> expiringIds = new ArrayList<>();

        // Phase 2: idle Set 기준으로 세션 만료 임박 확인
        var idleSet = redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE);
        for (Object idObj : idleSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            String poolKey = keyResolver.poolKey(id);

            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String sessionExpiresAtStr = map.get("sessionExpiresAt");

            if (sessionExpiresAtStr == null || "0".equals(sessionExpiresAtStr)) {
                continue;
            }

            long sessionExpiresAtMillis = Long.parseLong(sessionExpiresAtStr);

            if (sessionExpiresAtMillis > 0 && sessionExpiresAtMillis <= thresholdMillis) {
                expiringIds.add(UserAgentId.of(id));
                log.debug(
                        "세션 만료 임박 UserAgent 발견: id={}, expiresAt={}",
                        id,
                        Instant.ofEpochMilli(sessionExpiresAtMillis));
            }
        }

        return expiringIds;
    }

    @Override
    public List<UserAgentId> getAllSuspendedUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        var suspendedSet =
                redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE);

        for (Object idObj : suspendedSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            result.add(UserAgentId.of(id));
        }

        return result;
    }

    @Override
    public List<UserAgentId> getAllUserAgentIds() {
        List<UserAgentId> result = new ArrayList<>();

        // Phase 2: idle, borrowed, cooldown, session_required, suspended 모든 Set 포함
        var idleSet = redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE);
        var borrowedSet = redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE);
        var cooldownSet = redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE);
        var sessionRequiredSet =
                redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE);
        var suspendedSet =
                redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE);

        for (Object idObj : idleSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idObj.toString())));
        }
        for (Object idObj : borrowedSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idObj.toString())));
        }
        for (Object idObj : cooldownSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idObj.toString())));
        }
        for (Object idObj : sessionRequiredSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idObj.toString())));
        }
        for (Object idObj : suspendedSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idObj.toString())));
        }

        return result;
    }

    // ========================================
    // Phase 2: 신규 조회 메서드
    // ========================================

    /**
     * COOLDOWN 상태의 모든 UserAgent ID 조회
     *
     * @return COOLDOWN 상태 UserAgent ID 목록
     */
    public List<UserAgentId> getCooldownUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        RSet<String> cooldownSet =
                redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE);

        for (String idStr : cooldownSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idStr)));
        }

        return result;
    }

    /**
     * BORROWED 상태의 모든 UserAgent ID 조회
     *
     * @return BORROWED 상태 UserAgent ID 목록
     */
    public List<UserAgentId> getBorrowedUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        RSet<String> borrowedSet =
                redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE);

        for (String idStr : borrowedSet.readAll()) {
            result.add(UserAgentId.of(Long.parseLong(idStr)));
        }

        return result;
    }
}
