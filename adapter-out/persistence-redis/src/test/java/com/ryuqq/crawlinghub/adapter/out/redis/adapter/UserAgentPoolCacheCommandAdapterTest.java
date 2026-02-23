package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Nested
    @DisplayName("addToPool - Pool에 UserAgent 추가")
    class AddToPoolTests {

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태로 Pool에 추가")
        void shouldAddUserAgentWithSessionRequiredStatus() {
            // Given
            CachedUserAgent cachedUserAgent =
                    new CachedUserAgent(
                            1L,
                            "Mozilla/5.0",
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
            verify(rSet).add("1");
        }
    }

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

    @Nested
    @DisplayName("updateSession - 세션 업데이트")
    class UpdateSessionTests {

        @Test
        @DisplayName("성공 - IDLE 상태로 전환 및 세션 정보 저장")
        void shouldUpdateSessionAndChangeToReadyStatus() {
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
    }

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
        }
    }

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
            verify(rSet, org.mockito.Mockito.times(5)).delete();
        }
    }

    @Nested
    @DisplayName("warmUp - Pool WarmUp")
    class WarmUpTests {

        @Test
        @DisplayName("성공 - 여러 UserAgent 추가")
        void shouldAddMultipleUserAgents() {
            // Given
            CachedUserAgent agent1 =
                    new CachedUserAgent(
                            1L,
                            "Mozilla/5.0",
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
            CachedUserAgent agent2 =
                    new CachedUserAgent(
                            2L,
                            "Chrome/120",
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

            given(redissonClient.getMap(anyString(), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheCommandAdapter adapter = createAdapter();

            // When
            int count = adapter.warmUp(List.of(agent1, agent2));

            // Then
            assertThat(count).isEqualTo(2);
        }
    }
}
