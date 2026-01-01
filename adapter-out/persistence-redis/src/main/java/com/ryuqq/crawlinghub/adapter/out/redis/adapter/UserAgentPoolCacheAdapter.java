package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CacheStatus;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

/**
 * UserAgent Pool Redis Adapter
 *
 * <p>Redis 기반 UserAgent Pool 관리를 담당합니다.
 *
 * <p><strong>데이터 구조</strong>:
 *
 * <ul>
 *   <li>Hash: useragent:pool:{id} - 각 UserAgent의 상세 정보
 *   <li>Set: useragent:ready - READY 상태 (세션 있음, 사용 가능)
 *   <li>Set: useragent:session_required - SESSION_REQUIRED 상태 (세션 필요)
 *   <li>Set: useragent:suspended - SUSPENDED 상태 (일시 정지)
 * </ul>
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * SESSION_REQUIRED (Pool 추가, 복구)
 *       ↓ updateSession()
 *     READY (세션 발급 완료)
 *       ↓ 429 또는 Health < 30
 *   SUSPENDED (일시 정지)
 *       ↓ restoreToPool() + 세션 발급
 * SESSION_REQUIRED → READY
 * </pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@EnableConfigurationProperties(UserAgentPoolProperties.class)
public class UserAgentPoolCacheAdapter implements UserAgentPoolCachePort {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolCacheAdapter.class);

    private final String keyPrefix;
    private final String poolKeyPrefix;
    private final String readySetKey;
    private final String sessionRequiredSetKey;
    private final String suspendedSetKey;

    private final int maxTokens;
    private final long windowDurationMillis;
    private final int suspensionThreshold;

    private final RedissonClient redissonClient;
    private final Clock clock;

    private final String consumeTokenScript;
    private final String recordSuccessScript;
    private final String recordFailureScript;

    public UserAgentPoolCacheAdapter(
            RedissonClient redissonClient, Clock clock, UserAgentPoolProperties properties) {
        this.redissonClient = redissonClient;
        this.clock = clock;

        // Properties에서 설정값 로드
        this.keyPrefix = properties.getKeyPrefix();
        this.poolKeyPrefix = keyPrefix + "pool:";
        this.readySetKey = keyPrefix + "ready";
        this.sessionRequiredSetKey = keyPrefix + "session_required";
        this.suspendedSetKey = keyPrefix + "suspended";

        this.maxTokens = properties.getMaxTokens();
        this.windowDurationMillis = properties.getWindowDurationMillis();
        this.suspensionThreshold = properties.getSuspensionThreshold();

        this.consumeTokenScript = loadLuaScript("lua/useragent_consume_token.lua");
        this.recordSuccessScript = loadLuaScript("lua/useragent_record_success.lua");
        this.recordFailureScript = loadLuaScript("lua/useragent_record_failure.lua");

        log.info(
                "UserAgentPoolCacheAdapter 초기화: maxTokens={}, windowDurationMillis={},"
                        + " suspensionThreshold={}",
                maxTokens,
                windowDurationMillis,
                suspensionThreshold);
    }

    private String loadLuaScript(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lua script 로드 실패: " + path, e);
        }
    }

    @Override
    public Optional<CachedUserAgent> consumeToken() {
        Instant now = clock.instant();
        long nowMillis = now.toEpochMilli();

        // READY 상태에서만 선택 (세션이 있고 만료되지 않은 UserAgent만)
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Object result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        consumeTokenScript,
                        RScript.ReturnType.VALUE,
                        List.of(readySetKey, poolKeyPrefix, sessionRequiredSetKey),
                        String.valueOf(nowMillis),
                        String.valueOf(maxTokens),
                        String.valueOf(windowDurationMillis));

        if (result == null) {
            return Optional.empty();
        }

        Long selectedId = Long.parseLong(result.toString());
        return findById(UserAgentId.of(selectedId));
    }

    @Override
    public void recordSuccess(UserAgentId userAgentId) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        script.eval(
                RScript.Mode.READ_WRITE,
                recordSuccessScript,
                RScript.ReturnType.INTEGER,
                List.of(poolKey));

        log.debug("UserAgent {} 성공 기록 (Health +5)", userAgentId.value());
    }

    @Override
    public boolean recordFailure(UserAgentId userAgentId, int httpStatusCode) {
        int penalty = httpStatusCode >= 500 ? 10 : 5;
        Instant now = clock.instant();
        long nowMillis = now.toEpochMilli();

        String poolKey = poolKeyPrefix + userAgentId.value();
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        recordFailureScript,
                        RScript.ReturnType.INTEGER,
                        List.of(poolKey, readySetKey, suspendedSetKey),
                        String.valueOf(penalty),
                        String.valueOf(suspensionThreshold),
                        String.valueOf(userAgentId.value()),
                        String.valueOf(nowMillis));

        boolean suspended = result != null && result == 1L;
        if (suspended) {
            log.warn(
                    "UserAgent {} SUSPENDED (Health < {})",
                    userAgentId.value(),
                    suspensionThreshold);
        }

        return suspended;
    }

    @Override
    public void addToPool(CachedUserAgent cachedUserAgent) {
        String poolKey = poolKeyPrefix + cachedUserAgent.userAgentId();
        String idStr = String.valueOf(cachedUserAgent.userAgentId());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("userAgentId", idStr);
        map.put("userAgentValue", cachedUserAgent.userAgentValue());
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");
        map.put("remainingTokens", String.valueOf(cachedUserAgent.remainingTokens()));
        map.put("maxTokens", String.valueOf(cachedUserAgent.maxTokens()));
        map.put("healthScore", String.valueOf(cachedUserAgent.healthScore()));
        map.put("cacheStatus", CacheStatus.SESSION_REQUIRED.name());
        map.put("windowStart", "0");
        map.put("windowEnd", "0");
        map.put("suspendedAt", "0");

        // SESSION_REQUIRED Set에 추가
        redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에 추가됨 (SESSION_REQUIRED)", cachedUserAgent.userAgentId());
    }

    @Override
    public void removeFromPool(UserAgentId userAgentId) {
        Instant now = clock.instant();
        long nowMillis = now.toEpochMilli();

        String poolKey = poolKeyPrefix + userAgentId.value();
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("cacheStatus", CacheStatus.SUSPENDED.name());
        map.put("suspendedAt", String.valueOf(nowMillis));
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");

        // Set 이동: READY → SUSPENDED
        redissonClient.getSet(readySetKey, StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에서 제거됨 (SUSPENDED)", userAgentId.value());
    }

    @Override
    public void updateSession(
            UserAgentId userAgentId, String sessionToken, Instant sessionExpiresAt) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("sessionToken", sessionToken);
        map.put("sessionExpiresAt", String.valueOf(sessionExpiresAt.toEpochMilli()));
        map.put("cacheStatus", CacheStatus.READY.name());

        // Set 이동: SESSION_REQUIRED → READY
        redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(readySetKey, StringCodec.INSTANCE).add(idStr);

        log.info(
                "UserAgent {} 세션 업데이트 완료 (READY), expiresAt={}",
                userAgentId.value(),
                sessionExpiresAt);
    }

    @Override
    public void expireSession(UserAgentId userAgentId) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");
        map.put("cacheStatus", CacheStatus.SESSION_REQUIRED.name());

        // Set 이동: READY → SESSION_REQUIRED
        redissonClient.getSet(readySetKey, StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} 세션 만료 처리 (SESSION_REQUIRED)", userAgentId.value());
    }

    @Override
    public void restoreToPool(UserAgentId userAgentId, String userAgentValue) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("cacheStatus", CacheStatus.SESSION_REQUIRED.name());
        map.put("healthScore", "70");
        map.put("remainingTokens", String.valueOf(maxTokens));
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");
        map.put("windowStart", "0");
        map.put("windowEnd", "0");
        map.put("suspendedAt", "0");

        // Set 이동: SUSPENDED → SESSION_REQUIRED
        redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에 복구됨 (SESSION_REQUIRED, Health=70)", userAgentId.value());
    }

    @Override
    public List<UserAgentId> getSessionRequiredUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        var sessionRequiredSet = redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE);

        for (Object idObj : sessionRequiredSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            result.add(UserAgentId.of(id));
        }

        return result;
    }

    @Override
    public List<UserAgentId> getRecoverableUserAgents() {
        Instant now = clock.instant();
        long thresholdMillis = now.minus(Duration.ofHours(1)).toEpochMilli();
        List<UserAgentId> recoverableIds = new ArrayList<>();

        var suspendedSet = redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE);
        for (Object idObj : suspendedSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            String poolKey = poolKeyPrefix + id;

            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String suspendedAtStr = map.get("suspendedAt");
            String healthScoreStr = map.get("healthScore");

            if (suspendedAtStr == null || "0".equals(suspendedAtStr)) {
                continue;
            }

            long suspendedAtMillis = Long.parseLong(suspendedAtStr);
            int healthScore = Integer.parseInt(healthScoreStr != null ? healthScoreStr : "0");

            // 복구 조건: 1시간 경과 + Health >= threshold
            if (suspendedAtMillis < thresholdMillis && healthScore >= suspensionThreshold) {
                recoverableIds.add(UserAgentId.of(id));
            }
        }

        return recoverableIds;
    }

    @Override
    public List<UserAgentId> getSessionExpiringUserAgents(int bufferMinutes) {
        Instant now = clock.instant();
        long thresholdMillis = now.plus(Duration.ofMinutes(bufferMinutes)).toEpochMilli();
        List<UserAgentId> expiringIds = new ArrayList<>();

        var readySet = redissonClient.getSet(readySetKey, StringCodec.INSTANCE);
        for (Object idObj : readySet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            String poolKey = poolKeyPrefix + id;

            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String sessionExpiresAtStr = map.get("sessionExpiresAt");

            if (sessionExpiresAtStr == null || "0".equals(sessionExpiresAtStr)) {
                continue;
            }

            long sessionExpiresAtMillis = Long.parseLong(sessionExpiresAtStr);

            // 세션 만료 임박 조건: 현재 시간 + buffer 이내에 만료 예정
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
    public PoolStats getPoolStats() {
        var readySet = redissonClient.getSet(readySetKey, StringCodec.INSTANCE);
        var sessionRequiredSet = redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE);
        var suspendedSet = redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE);

        int readyCount = readySet.size();
        int sessionRequiredCount = sessionRequiredSet.size();
        int suspendedCount = suspendedSet.size();
        int total = readyCount + sessionRequiredCount + suspendedCount;

        int sumHealth = 0;
        int minHealth = 100;
        int maxHealth = 0;
        int count = 0;

        // READY 상태 UserAgent들의 Health Score 계산
        for (Object idObj : readySet.readAll()) {
            String poolKey = poolKeyPrefix + idObj.toString();
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
                readyCount,
                suspendedCount,
                avgHealth,
                count > 0 ? minHealth : 0,
                count > 0 ? maxHealth : 0);
    }

    @Override
    public Optional<CachedUserAgent> findById(UserAgentId userAgentId) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);

        Map<String, String> data = map.readAllMap();
        if (data.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(mapToCachedUserAgent(data));
    }

    @Override
    public void clearPool() {
        var readySet = redissonClient.getSet(readySetKey, StringCodec.INSTANCE);
        var sessionRequiredSet = redissonClient.getSet(sessionRequiredSetKey, StringCodec.INSTANCE);
        var suspendedSet = redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE);

        for (Object id : readySet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : sessionRequiredSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : suspendedSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }

        readySet.delete();
        sessionRequiredSet.delete();
        suspendedSet.delete();

        log.info("UserAgent Pool 전체 삭제 완료");
    }

    @Override
    public void updateHealthScore(UserAgentId userAgentId, int healthScore) {
        String poolKey = poolKeyPrefix + userAgentId.value();
        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("healthScore", String.valueOf(healthScore));
        log.debug("UserAgent {} Health Score 업데이트: {}", userAgentId.value(), healthScore);
    }

    @Override
    public int warmUp(List<CachedUserAgent> cachedUserAgents) {
        int addedCount = 0;
        for (CachedUserAgent cachedUserAgent : cachedUserAgents) {
            addToPool(cachedUserAgent);
            addedCount++;
        }
        log.info("WarmUp 완료: {} UserAgent Pool에 추가", addedCount);
        return addedCount;
    }

    @Override
    public List<UserAgentId> getAllSuspendedUserAgents() {
        List<UserAgentId> result = new ArrayList<>();
        var suspendedSet = redissonClient.getSet(suspendedSetKey, StringCodec.INSTANCE);

        for (Object idObj : suspendedSet.readAll()) {
            Long id = Long.parseLong(idObj.toString());
            result.add(UserAgentId.of(id));
        }

        return result;
    }

    private CachedUserAgent mapToCachedUserAgent(Map<String, String> data) {
        Long userAgentId = Long.parseLong(data.get("userAgentId"));
        String userAgentValue = data.get("userAgentValue");
        String sessionToken = data.get("sessionToken");
        String nid = data.get("nid");
        String mustitUid = data.get("mustitUid");
        int remainingTokens = Integer.parseInt(data.getOrDefault("remainingTokens", "80"));
        int maxTokens = Integer.parseInt(data.getOrDefault("maxTokens", "80"));
        int healthScore = Integer.parseInt(data.getOrDefault("healthScore", "100"));
        CacheStatus cacheStatus =
                CacheStatus.valueOf(
                        data.getOrDefault("cacheStatus", CacheStatus.SESSION_REQUIRED.name()));

        String sessionExpiresAtStr = data.get("sessionExpiresAt");
        String windowStartStr = data.get("windowStart");
        String windowEndStr = data.get("windowEnd");
        String suspendedAtStr = data.get("suspendedAt");

        Instant sessionExpiresAt = parseInstant(sessionExpiresAtStr);
        Instant windowStart = parseInstant(windowStartStr);
        Instant windowEnd = parseInstant(windowEndStr);
        Instant suspendedAt = parseInstant(suspendedAtStr);

        // 빈 문자열은 null로 처리
        if (sessionToken != null && sessionToken.isEmpty()) {
            sessionToken = null;
        }
        if (nid != null && nid.isEmpty()) {
            nid = null;
        }
        if (mustitUid != null && mustitUid.isEmpty()) {
            mustitUid = null;
        }

        return new CachedUserAgent(
                userAgentId,
                userAgentValue,
                sessionToken,
                nid,
                mustitUid,
                sessionExpiresAt,
                remainingTokens,
                maxTokens,
                windowStart,
                windowEnd,
                healthScore,
                cacheStatus,
                suspendedAt);
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty() || "0".equals(value)) {
            return null;
        }
        return Instant.ofEpochMilli(Long.parseLong(value));
    }
}
