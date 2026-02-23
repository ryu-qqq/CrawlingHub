package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.CachedUserAgentRedisMapper;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolLuaScriptHolder;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.port.out.command.UserAgentPoolCacheCommandPort;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Redis Command Adapter
 *
 * <p>Redis 기반 UserAgent Pool 상태 변경을 담당합니다.
 *
 * <p><strong>Phase 2 변경사항</strong>:
 *
 * <ul>
 *   <li>borrow() 메서드 추가 (Lua Script - IDLE -> BORROWED 원자적 전환)
 *   <li>returnAgent() 메서드 추가 (Lua Script - BORROWED -> IDLE/COOLDOWN/SUSPENDED)
 *   <li>recoverExpiredCooldowns() 메서드 추가 (Lua Script - COOLDOWN -> IDLE/SESSION_REQUIRED)
 *   <li>detectLeakedAgents() 메서드 추가 (Borrowed Set 스캔으로 누수 감지)
 *   <li>readySetKey -> idleSetKey 전환
 *   <li>READY -> IDLE 상태값 전환
 *   <li>기존 consumeToken(), suspendForRateLimit() 레거시 유지 (Phase 4 Port 변경 시 제거)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheCommandAdapter implements UserAgentPoolCacheCommandPort {

    private static final Logger log =
            LoggerFactory.getLogger(UserAgentPoolCacheCommandAdapter.class);

    private final RedissonClient redissonClient;
    private final TimeProvider timeProvider;
    private final UserAgentPoolKeyResolver keyResolver;
    private final CachedUserAgentRedisMapper mapper;
    private final UserAgentPoolProperties properties;
    private final UserAgentPoolLuaScriptHolder luaScriptHolder;

    public UserAgentPoolCacheCommandAdapter(
            RedissonClient redissonClient,
            TimeProvider timeProvider,
            UserAgentPoolKeyResolver keyResolver,
            CachedUserAgentRedisMapper mapper,
            UserAgentPoolProperties properties,
            UserAgentPoolLuaScriptHolder luaScriptHolder) {
        this.redissonClient = redissonClient;
        this.timeProvider = timeProvider;
        this.keyResolver = keyResolver;
        this.mapper = mapper;
        this.properties = properties;
        this.luaScriptHolder = luaScriptHolder;

        log.info(
                "UserAgentPoolCacheCommandAdapter 초기화: maxTokens={}, windowDurationMillis={},"
                        + " suspensionThreshold={}",
                properties.getMaxTokens(),
                properties.getWindowDurationMillis(),
                properties.getSuspensionThreshold());
    }

    // ========================================
    // Phase 2: 신규 Borrow/Return 메서드
    // ========================================

    /**
     * UserAgent Borrow (IDLE -> BORROWED 원자적 전환)
     *
     * <p>HikariCP getConnection() 패턴 대응:
     *
     * <ol>
     *   <li>IDLE Set에서 3개 후보 랜덤 선택 (SharedList 스캔 대응)
     *   <li>세션 만료 체크 (isAlive 대응)
     *   <li>Token Bucket Lazy Refill
     *   <li>IDLE -> BORROWED 원자적 전환
     * </ol>
     *
     * @return 선택된 UserAgent (없으면 empty)
     */
    @Override
    public Optional<CachedUserAgent> borrow() {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Object result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        luaScriptHolder.borrowScript(),
                        RScript.ReturnType.VALUE,
                        List.of(
                                keyResolver.idleSetKey(),
                                keyResolver.poolKeyPrefix(),
                                keyResolver.borrowedSetKey(),
                                keyResolver.sessionRequiredSetKey()),
                        String.valueOf(nowMillis),
                        String.valueOf(properties.getMaxTokens()),
                        String.valueOf(properties.getWindowDurationMillis()));

        if (result == null) {
            return Optional.empty();
        }

        long selectedId = Long.parseLong(result.toString());
        String poolKey = keyResolver.poolKey(selectedId);
        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        Map<String, String> data = map.readAllMap();
        return data.isEmpty() ? Optional.empty() : Optional.of(mapper.mapToCachedUserAgent(data));
    }

    /**
     * UserAgent Return (BORROWED -> IDLE/COOLDOWN/SUSPENDED 원자적 전환)
     *
     * <p>HikariCP connection.close() 패턴 대응:
     *
     * <ul>
     *   <li>성공: BORROWED -> IDLE (Health 회복)
     *   <li>429 응답: BORROWED -> COOLDOWN (Graduated Backoff)
     *   <li>연속 429 5회: BORROWED -> SUSPENDED
     *   <li>Health 임계값 이하: BORROWED -> SUSPENDED
     *   <li>경미한 실패: BORROWED -> IDLE (Health 감소)
     * </ul>
     *
     * @param userAgentId UserAgent ID
     * @param success 성공 여부
     * @param httpStatusCode HTTP 상태 코드
     * @param healthDelta Health 변화량 (양수=증가, 음수=감소)
     * @param cooldownUntil Cooldown 만료 시간 (epoch millis, 0이면 없음)
     * @param consecutiveRateLimits 연속 429 횟수
     * @return 전환된 상태 (0=IDLE, 1=COOLDOWN, 2=SUSPENDED)
     */
    @Override
    public int returnAgent(
            long userAgentId,
            boolean success,
            int httpStatusCode,
            int healthDelta,
            Long cooldownUntil,
            int consecutiveRateLimits) {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        luaScriptHolder.returnScript(),
                        RScript.ReturnType.INTEGER,
                        List.of(
                                keyResolver.borrowedSetKey(),
                                keyResolver.idleSetKey(),
                                keyResolver.cooldownSetKey(),
                                keyResolver.suspendedSetKey(),
                                keyResolver.poolKeyPrefix()),
                        String.valueOf(userAgentId),
                        String.valueOf(success ? 1 : 0),
                        String.valueOf(httpStatusCode),
                        String.valueOf(nowMillis),
                        String.valueOf(healthDelta),
                        String.valueOf(cooldownUntil != null ? cooldownUntil : 0L),
                        String.valueOf(consecutiveRateLimits),
                        String.valueOf(properties.getSuspensionThreshold()));

        int returnCode = result != null ? result.intValue() : 0;

        if (returnCode == 0) {
            log.debug("UserAgent {} 반환 완료 (IDLE)", userAgentId);
        } else if (returnCode == 1) {
            log.info("UserAgent {} 반환 완료 (COOLDOWN, until={})", userAgentId, cooldownUntil);
        } else if (returnCode == 2) {
            log.warn("UserAgent {} 반환 완료 (SUSPENDED)", userAgentId);
        }

        return returnCode;
    }

    /**
     * 만료된 Cooldown UserAgent 복구 (Housekeeper 호출)
     *
     * <p>COOLDOWN Set의 모든 UserAgent를 스캔하여 cooldownUntil이 지난 에이전트를 복구합니다.
     *
     * <ul>
     *   <li>세션 유효: COOLDOWN -> IDLE
     *   <li>세션 만료: COOLDOWN -> SESSION_REQUIRED
     * </ul>
     *
     * @return 복구된 UserAgent 수
     */
    @Override
    public int recoverExpiredCooldowns() {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        luaScriptHolder.cooldownRecoverScript(),
                        RScript.ReturnType.INTEGER,
                        List.of(
                                keyResolver.cooldownSetKey(),
                                keyResolver.idleSetKey(),
                                keyResolver.sessionRequiredSetKey(),
                                keyResolver.poolKeyPrefix()),
                        String.valueOf(nowMillis));

        int recovered = result != null ? result.intValue() : 0;
        if (recovered > 0) {
            log.info("Cooldown 복구 완료: {} UserAgent", recovered);
        }

        return recovered;
    }

    /**
     * Borrowed Set에서 누수된 에이전트 감지
     *
     * <p>borrowedAt이 leakThresholdMillis보다 오래된 에이전트를 감지합니다. HikariCP의 leak detection과 유사한 패턴입니다.
     *
     * @param leakThresholdMillis 누수 감지 임계값 (밀리초)
     * @return 누수된 UserAgent ID 목록
     */
    @Override
    public List<Long> detectLeakedAgents(long leakThresholdMillis) {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();
        long threshold = nowMillis - leakThresholdMillis;

        List<Long> leaked = new ArrayList<>();
        RSet<String> borrowedSet =
                redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE);

        for (String idStr : borrowedSet.readAll()) {
            long id = Long.parseLong(idStr);
            String poolKey = keyResolver.poolKey(id);
            RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
            String borrowedAtStr = map.get("borrowedAt");

            if (borrowedAtStr != null && !"0".equals(borrowedAtStr)) {
                long borrowedAt = Long.parseLong(borrowedAtStr);
                if (borrowedAt > 0 && borrowedAt < threshold) {
                    leaked.add(id);
                    log.warn(
                            "Leaked UserAgent 감지: id={}, borrowedAt={}, elapsed={}ms",
                            id,
                            borrowedAt,
                            nowMillis - borrowedAt);
                }
            }
        }

        return leaked;
    }

    // ========================================
    // 기존 Port 인터페이스 구현 (Phase 4에서 Port 변경 시 수정)
    // ========================================

    @Override
    public Optional<CachedUserAgent> consumeToken() {
        // Phase 2: 내부적으로 borrow()에 위임
        return borrow();
    }

    @Override
    public void addToPool(CachedUserAgent cachedUserAgent) {
        String poolKey = keyResolver.poolKey(cachedUserAgent.userAgentId());
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
        map.put("status", UserAgentStatus.SESSION_REQUIRED.name());
        map.put("windowStart", "0");
        map.put("windowEnd", "0");
        map.put("suspendedAt", "0");
        // Phase 2: 신규 필드 초기값
        map.put("borrowedAt", "0");
        map.put("cooldownUntil", "0");
        map.put("consecutiveRateLimits", "0");

        redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에 추가됨 (SESSION_REQUIRED)", cachedUserAgent.userAgentId());
    }

    @Override
    public void removeFromPool(UserAgentId userAgentId) {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();

        String poolKey = keyResolver.poolKey(userAgentId);
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("status", UserAgentStatus.SUSPENDED.name());
        map.put("suspendedAt", String.valueOf(nowMillis));
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");

        // Phase 2: idle, borrowed, cooldown Set에서도 제거
        redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient
                .getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE)
                .remove(idStr);
        redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에서 제거됨 (SUSPENDED)", userAgentId.value());
    }

    @Override
    public void updateSession(
            UserAgentId userAgentId,
            String sessionToken,
            String nid,
            String mustitUid,
            Instant sessionExpiresAt) {
        String poolKey = keyResolver.poolKey(userAgentId);
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("sessionToken", sessionToken);
        map.put("nid", nid != null ? nid : "");
        map.put("mustitUid", mustitUid != null ? mustitUid : "");
        map.put("sessionExpiresAt", String.valueOf(sessionExpiresAt.toEpochMilli()));
        map.put("status", UserAgentStatus.IDLE.name());

        redissonClient
                .getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE)
                .remove(idStr);
        redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE).add(idStr);

        log.info(
                "UserAgent {} 세션 업데이트 완료 (IDLE), nid={}, mustitUid={}, expiresAt={}",
                userAgentId.value(),
                nid != null ? "있음" : "없음",
                mustitUid != null ? "있음" : "없음",
                sessionExpiresAt);
    }

    @Override
    public void expireSession(UserAgentId userAgentId) {
        String poolKey = keyResolver.poolKey(userAgentId);
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");
        map.put("status", UserAgentStatus.SESSION_REQUIRED.name());

        // Phase 2: idle, borrowed, cooldown Set에서도 제거
        redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} 세션 만료 처리 (SESSION_REQUIRED)", userAgentId.value());
    }

    @Override
    public void suspendForRateLimit(UserAgentId userAgentId) {
        Instant now = timeProvider.now();
        long nowMillis = now.toEpochMilli();

        String poolKey = keyResolver.poolKey(userAgentId);
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        script.eval(
                RScript.Mode.READ_WRITE,
                luaScriptHolder.rateLimitSuspendScript(),
                RScript.ReturnType.INTEGER,
                List.of(
                        poolKey,
                        keyResolver.idleSetKey(),
                        keyResolver.sessionRequiredSetKey(),
                        keyResolver.suspendedSetKey()),
                String.valueOf(userAgentId.value()),
                String.valueOf(nowMillis));

        log.info(
                "UserAgent {} Rate Limit SUSPENDED (atomic: 세션 만료 + Pool 제거)", userAgentId.value());
    }

    @Override
    public void restoreToPool(UserAgentId userAgentId, String userAgentValue) {
        String poolKey = keyResolver.poolKey(userAgentId);
        String idStr = String.valueOf(userAgentId.value());

        RMap<String, String> map = redissonClient.getMap(poolKey, StringCodec.INSTANCE);
        map.put("status", UserAgentStatus.SESSION_REQUIRED.name());
        map.put("healthScore", "70");
        map.put("remainingTokens", String.valueOf(properties.getMaxTokens()));
        map.put("sessionToken", "");
        map.put("nid", "");
        map.put("mustitUid", "");
        map.put("sessionExpiresAt", "0");
        map.put("windowStart", "0");
        map.put("windowEnd", "0");
        map.put("suspendedAt", "0");
        // Phase 2: 신규 필드 초기화
        map.put("borrowedAt", "0");
        map.put("cooldownUntil", "0");
        map.put("consecutiveRateLimits", "0");

        redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE).remove(idStr);
        redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE).add(idStr);

        log.info("UserAgent {} Pool에 복구됨 (SESSION_REQUIRED, Health=70)", userAgentId.value());
    }

    @Override
    public void clearPool() {
        var idleSet = redissonClient.getSet(keyResolver.idleSetKey(), StringCodec.INSTANCE);
        var borrowedSet = redissonClient.getSet(keyResolver.borrowedSetKey(), StringCodec.INSTANCE);
        var cooldownSet = redissonClient.getSet(keyResolver.cooldownSetKey(), StringCodec.INSTANCE);
        var sessionRequiredSet =
                redissonClient.getSet(keyResolver.sessionRequiredSetKey(), StringCodec.INSTANCE);
        var suspendedSet =
                redissonClient.getSet(keyResolver.suspendedSetKey(), StringCodec.INSTANCE);

        String poolKeyPrefix = keyResolver.poolKeyPrefix();

        // Phase 2: 모든 Set의 UserAgent Hash 삭제
        for (Object id : idleSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : borrowedSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : cooldownSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : sessionRequiredSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }
        for (Object id : suspendedSet.readAll()) {
            redissonClient.getMap(poolKeyPrefix + id.toString()).delete();
        }

        idleSet.delete();
        borrowedSet.delete();
        cooldownSet.delete();
        sessionRequiredSet.delete();
        suspendedSet.delete();

        log.info("UserAgent Pool 전체 삭제 완료");
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
    public boolean isPoolInitialized() {
        RBucket<String> bucket =
                redissonClient.getBucket(
                        keyResolver.poolKeyPrefix() + "initialized", StringCodec.INSTANCE);
        return bucket.isExists();
    }

    @Override
    public void markPoolInitialized() {
        RBucket<String> bucket =
                redissonClient.getBucket(
                        keyResolver.poolKeyPrefix() + "initialized", StringCodec.INSTANCE);
        bucket.set("true");
        log.info("Pool WarmUp 완료 플래그 설정");
    }

    @Override
    public boolean tryAcquireWarmUpLock() {
        String lockKey = keyResolver.poolKeyPrefix() + "warmup-lock";
        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result =
                script.eval(
                        RScript.Mode.READ_WRITE,
                        "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then "
                                + "redis.call('expire', KEYS[1], ARGV[2]) "
                                + "return 1 "
                                + "else return 0 end",
                        RScript.ReturnType.INTEGER,
                        List.of(lockKey),
                        "locked",
                        "300");
        boolean acquired = result != null && result == 1;
        if (acquired) {
            log.info("WarmUp 분산 락 획득 성공");
        }
        return acquired;
    }
}
