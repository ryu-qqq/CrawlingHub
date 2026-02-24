package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.CachedUserAgentRedisMapper;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolLuaScriptHolder;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

/**
 * UserAgentPoolCacheCommandAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("cache")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheCommandAdapter 단위 테스트")
@SuppressWarnings({"unchecked", "rawtypes"})
class UserAgentPoolCacheCommandAdapterTest {

    @Mock private RedissonClient redissonClient;
    @Mock private RMap rMap;
    @Mock private RSet rSet;
    @Mock private RScript rScript;
    @Mock private RBucket rBucket;
    @Mock private UserAgentPoolLuaScriptHolder luaScriptHolder;

    private TimeProvider timeProvider;
    private UserAgentPoolKeyResolver keyResolver;
    private CachedUserAgentRedisMapper mapper;
    private UserAgentPoolProperties properties;

    private static final Instant FIXED_NOW = Instant.parse("2024-01-15T10:00:00Z");
    private static final String KEY_PREFIX = "useragent:";
    private static final String POOL_KEY_PREFIX = KEY_PREFIX + "pool:";
    private static final String IDLE_SET_KEY = KEY_PREFIX + "idle";
    private static final String BORROWED_SET_KEY = KEY_PREFIX + "borrowed";
    private static final String COOLDOWN_SET_KEY = KEY_PREFIX + "cooldown";
    private static final String SESSION_REQUIRED_SET_KEY = KEY_PREFIX + "session_required";
    private static final String SUSPENDED_SET_KEY = KEY_PREFIX + "suspended";

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        timeProvider = new TimeProvider(fixedClock);
        properties = createDefaultProperties();
        keyResolver = new UserAgentPoolKeyResolver(properties);
        mapper = new CachedUserAgentRedisMapper();
    }

    private UserAgentPoolProperties createDefaultProperties() {
        UserAgentPoolProperties props = new UserAgentPoolProperties();
        props.setKeyPrefix(KEY_PREFIX);

        UserAgentPoolProperties.RateLimit rateLimit = new UserAgentPoolProperties.RateLimit();
        rateLimit.setMaxTokens(80);
        rateLimit.setWindowDuration(Duration.ofHours(1));
        props.setRateLimit(rateLimit);

        UserAgentPoolProperties.Health health = new UserAgentPoolProperties.Health();
        health.setSuspensionThreshold(30);
        props.setHealth(health);

        return props;
    }

    private UserAgentPoolCacheCommandAdapter createAdapter() {
        return new UserAgentPoolCacheCommandAdapter(
                redissonClient, timeProvider, keyResolver, mapper, properties, luaScriptHolder);
    }

    /** 기본 CachedUserAgent 생성 헬퍼 */
    private CachedUserAgent buildCachedUserAgent(long id, String uaValue) {
        return new CachedUserAgent(
                id,
                uaValue,
                null,
                null,
                null,
                null,
                80,
                80,
                null,
                null,
                100,
                UserAgentStatus.SESSION_REQUIRED,
                null,
                null,
                null,
                0);
    }

    /** Redis Hash 데이터 생성 헬퍼 */
    private Map<String, String> buildRedisHashData(
            long id, String uaValue, UserAgentStatus status) {
        Map<String, String> data = new HashMap<>();
        data.put("userAgentId", String.valueOf(id));
        data.put("userAgentValue", uaValue);
        data.put("sessionToken", "session-token");
        data.put("sessionExpiresAt", String.valueOf(FIXED_NOW.plusSeconds(3600).toEpochMilli()));
        data.put("remainingTokens", "80");
        data.put("maxTokens", "80");
        data.put("healthScore", "100");
        data.put("status", status.name());
        data.put("windowStart", "0");
        data.put("windowEnd", "0");
        data.put("suspendedAt", "0");
        data.put("borrowedAt", "0");
        data.put("cooldownUntil", "0");
        data.put("consecutiveRateLimits", "0");
        data.put("nid", "");
        data.put("mustitUid", "");
        return data;
    }

    // ========================================
    // addToPool 테스트
    // ========================================

    @Nested
    @DisplayName("addToPool - Pool에 UserAgent 추가")
    class AddToPoolTests {

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태로 Pool에 추가")
        void shouldAddUserAgentWithSessionRequiredStatus() {
            // Given
            CachedUserAgent cachedUserAgent = buildCachedUserAgent(1L, "Mozilla/5.0");
            String poolKey = POOL_KEY_PREFIX + "1";
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.addToPool(cachedUserAgent);

            // Then
            verify(rMap).put("userAgentId", "1");
            verify(rMap).put("userAgentValue", "Mozilla/5.0");
            verify(rMap).put("status", UserAgentStatus.SESSION_REQUIRED.name());
            verify(rMap).put("healthScore", "100");
            verify(rMap).put("borrowedAt", "0");
            verify(rMap).put("cooldownUntil", "0");
            verify(rMap).put("consecutiveRateLimits", "0");
            verify(rSet).add("1");
        }

        @Test
        @DisplayName("성공 - remainingTokens, maxTokens 값이 올바르게 저장됨")
        void shouldStoreTokenValues() {
            // Given
            CachedUserAgent cachedUserAgent = buildCachedUserAgent(2L, "Chrome/120");
            String poolKey = POOL_KEY_PREFIX + "2";
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.addToPool(cachedUserAgent);

            // Then
            verify(rMap).put("remainingTokens", "80");
            verify(rMap).put("maxTokens", "80");
        }
    }

    // ========================================
    // borrow 테스트
    // ========================================

    @Nested
    @DisplayName("borrow - UserAgent 대여 (IDLE -> BORROWED)")
    class BorrowTests {

        @Test
        @DisplayName("성공 - Lua 스크립트 결과로 선택된 UserAgent 반환")
        void shouldReturnBorrowedUserAgent() {
            // Given
            String borrowLua = "borrow_lua_script";
            given(luaScriptHolder.borrowScript()).willReturn(borrowLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(borrowLua),
                                    eq(RScript.ReturnType.VALUE),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn("1");

            String poolKey = POOL_KEY_PREFIX + "1";
            Map<String, String> data = buildRedisHashData(1L, "Mozilla/5.0", UserAgentStatus.IDLE);
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(data);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            Optional<CachedUserAgent> result = adapter.borrow();

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().userAgentId()).isEqualTo(1L);
            assertThat(result.get().userAgentValue()).isEqualTo("Mozilla/5.0");
        }

        @Test
        @DisplayName("실패 - Lua 스크립트가 null 반환 시 빈 Optional")
        void shouldReturnEmptyWhenScriptReturnsNull() {
            // Given
            String borrowLua = "borrow_lua_script";
            given(luaScriptHolder.borrowScript()).willReturn(borrowLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(borrowLua),
                                    eq(RScript.ReturnType.VALUE),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            Optional<CachedUserAgent> result = adapter.borrow();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("실패 - Lua 스크립트 결과로 ID 선택 후 Hash 데이터가 비어있으면 빈 Optional")
        void shouldReturnEmptyWhenPoolHashIsEmpty() {
            // Given
            String borrowLua = "borrow_lua_script";
            given(luaScriptHolder.borrowScript()).willReturn(borrowLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(borrowLua),
                                    eq(RScript.ReturnType.VALUE),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn("99");

            String poolKey = POOL_KEY_PREFIX + "99";
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(Map.of());

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            Optional<CachedUserAgent> result = adapter.borrow();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - consumeToken은 borrow에 위임하여 동일 결과 반환")
        void consumeTokenDelegatesToBorrow() {
            // Given
            String borrowLua = "borrow_lua_script";
            given(luaScriptHolder.borrowScript()).willReturn(borrowLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(borrowLua),
                                    eq(RScript.ReturnType.VALUE),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            Optional<CachedUserAgent> result = adapter.consumeToken();

            // Then
            assertThat(result).isEmpty();
        }
    }

    // ========================================
    // returnAgent 테스트
    // ========================================

    @Nested
    @DisplayName("returnAgent - UserAgent 반환 (BORROWED -> IDLE/COOLDOWN/SUSPENDED)")
    class ReturnAgentTests {

        @Test
        @DisplayName("성공 반환 - returnCode 0 (IDLE)")
        void shouldReturnIdleCode() {
            // Given
            String returnLua = "return_lua_script";
            given(luaScriptHolder.returnScript()).willReturn(returnLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(returnLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(0L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.returnAgent(1L, true, 200, 5, null, 0);

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("429 응답 반환 - returnCode 1 (COOLDOWN)")
        void shouldReturnCooldownCode() {
            // Given
            String returnLua = "return_lua_script";
            given(luaScriptHolder.returnScript()).willReturn(returnLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(returnLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(1L);

            long cooldownUntil = FIXED_NOW.plusSeconds(60).toEpochMilli();
            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.returnAgent(1L, false, 429, -10, cooldownUntil, 3);

            // Then
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("연속 429 초과 반환 - returnCode 2 (SUSPENDED)")
        void shouldReturnSuspendedCode() {
            // Given
            String returnLua = "return_lua_script";
            given(luaScriptHolder.returnScript()).willReturn(returnLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(returnLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(2L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.returnAgent(1L, false, 429, -20, null, 5);

            // Then
            assertThat(result).isEqualTo(2);
        }

        @Test
        @DisplayName("Lua 결과가 null이면 0(IDLE)로 처리")
        void shouldDefaultToIdleWhenScriptReturnsNull() {
            // Given
            String returnLua = "return_lua_script";
            given(luaScriptHolder.returnScript()).willReturn(returnLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(returnLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.returnAgent(1L, true, 200, 5, null, 0);

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("cooldownUntil이 null이면 0으로 전달")
        void shouldPassZeroWhenCooldownUntilIsNull() {
            // Given
            String returnLua = "return_lua_script";
            given(luaScriptHolder.returnScript()).willReturn(returnLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(returnLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class),
                                    any(String.class)))
                    .willReturn(0L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.returnAgent(1L, true, 200, 5, null, 0);

            // Then
            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================
    // recoverExpiredCooldowns 테스트
    // ========================================

    @Nested
    @DisplayName("recoverExpiredCooldowns - 만료된 Cooldown 복구")
    class RecoverExpiredCooldownsTests {

        @Test
        @DisplayName("성공 - 복구된 UserAgent 수 반환")
        void shouldReturnRecoveredCount() {
            // Given
            String cooldownLua = "cooldown_recover_lua_script";
            given(luaScriptHolder.cooldownRecoverScript()).willReturn(cooldownLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(cooldownLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class)))
                    .willReturn(3L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.recoverExpiredCooldowns();

            // Then
            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("성공 - 복구 대상 없을 때 0 반환")
        void shouldReturnZeroWhenNothingToRecover() {
            // Given
            String cooldownLua = "cooldown_recover_lua_script";
            given(luaScriptHolder.cooldownRecoverScript()).willReturn(cooldownLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(cooldownLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class)))
                    .willReturn(0L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.recoverExpiredCooldowns();

            // Then
            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - Lua 결과가 null이면 0 반환")
        void shouldReturnZeroWhenScriptReturnsNull() {
            // Given
            String cooldownLua = "cooldown_recover_lua_script";
            given(luaScriptHolder.cooldownRecoverScript()).willReturn(cooldownLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    eq(cooldownLua),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    any(String.class)))
                    .willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int result = adapter.recoverExpiredCooldowns();

            // Then
            assertThat(result).isEqualTo(0);
        }
    }

    // ========================================
    // detectLeakedAgents 테스트
    // ========================================

    @Nested
    @DisplayName("detectLeakedAgents - 누수된 에이전트 감지")
    class DetectLeakedAgentsTests {

        @Test
        @DisplayName("성공 - borrowedAt이 임계값보다 오래된 에이전트 감지")
        void shouldDetectLeakedAgents() {
            // Given
            long leakThresholdMillis = 60_000L; // 60초
            // borrowedAt이 threshold보다 오래 됨
            long oldBorrowedAt = FIXED_NOW.toEpochMilli() - 120_000L; // 2분 전

            RSet borrowedSet = Mockito.mock(RSet.class);
            RMap agentMap = Mockito.mock(RMap.class);

            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of("1"));
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "1"), any(StringCodec.class)))
                    .willReturn(agentMap);
            given(agentMap.get("borrowedAt")).willReturn(String.valueOf(oldBorrowedAt));

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(leakThresholdMillis);

            // Then
            assertThat(leaked).containsExactly(1L);
        }

        @Test
        @DisplayName("성공 - borrowedAt이 임계값 이내이면 감지되지 않음")
        void shouldNotDetectRecentlyBorrowedAgents() {
            // Given
            long leakThresholdMillis = 60_000L;
            long recentBorrowedAt = FIXED_NOW.toEpochMilli() - 10_000L; // 10초 전 (임계값 이내)

            RSet borrowedSet = Mockito.mock(RSet.class);
            RMap agentMap = Mockito.mock(RMap.class);

            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of("2"));
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "2"), any(StringCodec.class)))
                    .willReturn(agentMap);
            given(agentMap.get("borrowedAt")).willReturn(String.valueOf(recentBorrowedAt));

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(leakThresholdMillis);

            // Then
            assertThat(leaked).isEmpty();
        }

        @Test
        @DisplayName("성공 - borrowedAt이 null이면 누수 아님")
        void shouldSkipNullBorrowedAt() {
            // Given
            RSet borrowedSet = Mockito.mock(RSet.class);
            RMap agentMap = Mockito.mock(RMap.class);

            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of("3"));
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "3"), any(StringCodec.class)))
                    .willReturn(agentMap);
            given(agentMap.get("borrowedAt")).willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(60_000L);

            // Then
            assertThat(leaked).isEmpty();
        }

        @Test
        @DisplayName("성공 - borrowedAt이 '0'이면 누수 아님")
        void shouldSkipZeroBorrowedAt() {
            // Given
            RSet borrowedSet = Mockito.mock(RSet.class);
            RMap agentMap = Mockito.mock(RMap.class);

            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of("4"));
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "4"), any(StringCodec.class)))
                    .willReturn(agentMap);
            given(agentMap.get("borrowedAt")).willReturn("0");

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(60_000L);

            // Then
            assertThat(leaked).isEmpty();
        }

        @Test
        @DisplayName("성공 - Borrowed Set이 비어있으면 빈 목록 반환")
        void shouldReturnEmptyListWhenNoBorrowedAgents() {
            // Given
            RSet borrowedSet = Mockito.mock(RSet.class);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of());

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(60_000L);

            // Then
            assertThat(leaked).isEmpty();
        }

        @Test
        @DisplayName("성공 - 여러 에이전트 중 누수된 것만 감지")
        void shouldDetectOnlyLeakedAmongMultiple() {
            // Given
            long leakThresholdMillis = 60_000L;
            long oldBorrowedAt = FIXED_NOW.toEpochMilli() - 120_000L;
            long recentBorrowedAt = FIXED_NOW.toEpochMilli() - 10_000L;

            RSet borrowedSet = Mockito.mock(RSet.class);
            RMap leakedMap = Mockito.mock(RMap.class);
            RMap normalMap = Mockito.mock(RMap.class);

            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(borrowedSet.readAll()).willReturn(Set.of("1", "2"));
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "1"), any(StringCodec.class)))
                    .willReturn(leakedMap);
            given(redissonClient.getMap(eq(POOL_KEY_PREFIX + "2"), any(StringCodec.class)))
                    .willReturn(normalMap);
            given(leakedMap.get("borrowedAt")).willReturn(String.valueOf(oldBorrowedAt));
            given(normalMap.get("borrowedAt")).willReturn(String.valueOf(recentBorrowedAt));

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            List<Long> leaked = adapter.detectLeakedAgents(leakThresholdMillis);

            // Then
            assertThat(leaked).containsExactly(1L);
        }
    }

    // ========================================
    // removeFromPool 테스트
    // ========================================

    @Nested
    @DisplayName("removeFromPool - Pool에서 UserAgent 제거")
    class RemoveFromPoolTests {

        @Test
        @DisplayName("성공 - SUSPENDED 상태로 변경하고 Set 이동")
        void shouldChangeStatusToSuspendedAndMoveSets() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.removeFromPool(userAgentId);

            // Then
            verify(rMap).put("status", UserAgentStatus.SUSPENDED.name());
            verify(rMap).put("suspendedAt", String.valueOf(FIXED_NOW.toEpochMilli()));
            verify(rMap).put("sessionToken", "");
            verify(rMap).put("sessionExpiresAt", "0");
        }
    }

    // ========================================
    // updateSession 테스트
    // ========================================

    @Nested
    @DisplayName("updateSession - 세션 업데이트")
    class UpdateSessionTests {

        @Test
        @DisplayName("성공 - IDLE 상태로 전환 및 세션 정보 저장")
        void shouldUpdateSessionAndChangeToIdleStatus() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String sessionToken = "new-session-token";
            String nid = "nid-test-123";
            String mustitUid = "uid-test-456";
            Instant sessionExpiresAt = FIXED_NOW.plus(Duration.ofHours(2));
            String poolKey = POOL_KEY_PREFIX + "1";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.updateSession(userAgentId, sessionToken, nid, mustitUid, sessionExpiresAt);

            // Then
            verify(rMap).put("sessionToken", sessionToken);
            verify(rMap).put("nid", nid);
            verify(rMap).put("mustitUid", mustitUid);
            verify(rMap).put("sessionExpiresAt", String.valueOf(sessionExpiresAt.toEpochMilli()));
            verify(rMap).put("status", UserAgentStatus.IDLE.name());
        }

        @Test
        @DisplayName("성공 - nid가 null이면 빈 문자열 저장")
        void shouldStoreEmptyStringForNullNid() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            Instant sessionExpiresAt = FIXED_NOW.plus(Duration.ofHours(1));
            String poolKey = POOL_KEY_PREFIX + "1";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.updateSession(userAgentId, "token", null, null, sessionExpiresAt);

            // Then
            verify(rMap).put("nid", "");
            verify(rMap).put("mustitUid", "");
        }
    }

    // ========================================
    // expireSession 테스트
    // ========================================

    @Nested
    @DisplayName("expireSession - 세션 만료 처리")
    class ExpireSessionTests {

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태로 전환")
        void shouldChangeToSessionRequiredStatus() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.expireSession(userAgentId);

            // Then
            verify(rMap).put("sessionToken", "");
            verify(rMap).put("sessionExpiresAt", "0");
            verify(rMap).put("status", UserAgentStatus.SESSION_REQUIRED.name());
        }
    }

    // ========================================
    // suspendForRateLimit 테스트
    // ========================================

    @Nested
    @DisplayName("suspendForRateLimit - Rate Limit으로 인한 SUSPENDED")
    class SuspendForRateLimitTests {

        @Test
        @DisplayName("성공 - rateLimitSuspend Lua 스크립트 실행")
        void shouldExecuteRateLimitSuspendScript() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";
            String rateLimitLua = "rate_limit_suspend_lua";

            given(luaScriptHolder.rateLimitSuspendScript()).willReturn(rateLimitLua);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.suspendForRateLimit(userAgentId);

            // Then
            verify(rScript)
                    .eval(
                            eq(RScript.Mode.READ_WRITE),
                            eq(rateLimitLua),
                            eq(RScript.ReturnType.INTEGER),
                            any(List.class),
                            eq("1"),
                            eq(String.valueOf(FIXED_NOW.toEpochMilli())));
        }
    }

    // ========================================
    // restoreToPool 테스트
    // ========================================

    @Nested
    @DisplayName("restoreToPool - Pool 복구")
    class RestoreToPoolTests {

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태로 복구 (Health=70)")
        void shouldRestoreWithHealth70() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String userAgentValue = "Mozilla/5.0";
            String poolKey = POOL_KEY_PREFIX + "1";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.restoreToPool(userAgentId, userAgentValue);

            // Then
            verify(rMap).put("status", UserAgentStatus.SESSION_REQUIRED.name());
            verify(rMap).put("healthScore", "70");
            verify(rMap).put("remainingTokens", "80");
            verify(rMap).put("suspendedAt", "0");
            verify(rMap).put("borrowedAt", "0");
            verify(rMap).put("cooldownUntil", "0");
            verify(rMap).put("consecutiveRateLimits", "0");
        }
    }

    // ========================================
    // clearPool 테스트
    // ========================================

    @Nested
    @DisplayName("clearPool - Pool 전체 삭제")
    class ClearPoolTests {

        @Test
        @DisplayName("성공 - 모든 Set과 Map 삭제")
        void shouldClearAllSetsAndMaps() {
            // Given
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("1", "2"));
            given(redissonClient.getMap(anyString())).willReturn(rMap);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.clearPool();

            // Then
            verify(rSet, times(5)).delete();
        }

        @Test
        @DisplayName("성공 - 비어있는 Pool도 정상 삭제 처리")
        void shouldClearEmptyPoolWithoutError() {
            // Given
            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class))).willReturn(rSet);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of());

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.clearPool();

            // Then
            verify(rSet, times(5)).delete();
        }
    }

    // ========================================
    // warmUp 테스트
    // ========================================

    @Nested
    @DisplayName("warmUp - Pool WarmUp")
    class WarmUpTests {

        @Test
        @DisplayName("성공 - 여러 UserAgent 추가")
        void shouldAddMultipleUserAgents() {
            // Given
            CachedUserAgent agent1 = buildCachedUserAgent(1L, "Mozilla/5.0");
            CachedUserAgent agent2 = buildCachedUserAgent(2L, "Chrome/120");

            given(redissonClient.getMap(anyString(), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int count = adapter.warmUp(List.of(agent1, agent2));

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 빈 목록으로 warmUp 시 0 반환")
        void shouldReturnZeroForEmptyList() {
            // Given
            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int count = adapter.warmUp(List.of());

            // Then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 단일 UserAgent warmUp")
        void shouldAddSingleUserAgent() {
            // Given
            CachedUserAgent agent = buildCachedUserAgent(5L, "Firefox/120");
            given(redissonClient.getMap(anyString(), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int count = adapter.warmUp(List.of(agent));

            // Then
            assertThat(count).isEqualTo(1);
        }
    }

    // ========================================
    // isPoolInitialized 테스트
    // ========================================

    @Nested
    @DisplayName("isPoolInitialized - Pool 초기화 여부 확인")
    class IsPoolInitializedTests {

        @Test
        @DisplayName("성공 - 초기화 완료 버킷이 존재하면 true 반환")
        void shouldReturnTrueWhenInitialized() {
            // Given
            String initKey = POOL_KEY_PREFIX + "initialized";
            given(redissonClient.getBucket(eq(initKey), any(StringCodec.class)))
                    .willReturn(rBucket);
            given(rBucket.isExists()).willReturn(true);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            boolean result = adapter.isPoolInitialized();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - 초기화 완료 버킷이 없으면 false 반환")
        void shouldReturnFalseWhenNotInitialized() {
            // Given
            String initKey = POOL_KEY_PREFIX + "initialized";
            given(redissonClient.getBucket(eq(initKey), any(StringCodec.class)))
                    .willReturn(rBucket);
            given(rBucket.isExists()).willReturn(false);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            boolean result = adapter.isPoolInitialized();

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========================================
    // markPoolInitialized 테스트
    // ========================================

    @Nested
    @DisplayName("markPoolInitialized - Pool 초기화 플래그 설정")
    class MarkPoolInitializedTests {

        @Test
        @DisplayName("성공 - 초기화 완료 버킷에 'true' 설정")
        void shouldSetInitializedFlag() {
            // Given
            String initKey = POOL_KEY_PREFIX + "initialized";
            given(redissonClient.getBucket(eq(initKey), any(StringCodec.class)))
                    .willReturn(rBucket);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            adapter.markPoolInitialized();

            // Then
            verify(rBucket).set("true");
        }
    }

    // ========================================
    // tryAcquireWarmUpLock 테스트
    // ========================================

    @Nested
    @DisplayName("tryAcquireWarmUpLock - WarmUp 분산 락 획득")
    class TryAcquireWarmUpLockTests {

        @Test
        @DisplayName("성공 - 락 획득 시 true 반환")
        void shouldReturnTrueWhenLockAcquired() {
            // Given
            String lockKey = POOL_KEY_PREFIX + "warmup-lock";
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    anyString(),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    eq("locked"),
                                    eq("300")))
                    .willReturn(1L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            boolean result = adapter.tryAcquireWarmUpLock();

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("실패 - 이미 락이 걸려있으면 false 반환")
        void shouldReturnFalseWhenLockAlreadyAcquired() {
            // Given
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    anyString(),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    eq("locked"),
                                    eq("300")))
                    .willReturn(0L);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            boolean result = adapter.tryAcquireWarmUpLock();

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("실패 - Lua 결과가 null이면 false 반환")
        void shouldReturnFalseWhenScriptReturnsNull() {
            // Given
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    anyString(),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(List.class),
                                    eq("locked"),
                                    eq("300")))
                    .willReturn(null);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            boolean result = adapter.tryAcquireWarmUpLock();

            // Then
            assertThat(result).isFalse();
        }
    }
}
