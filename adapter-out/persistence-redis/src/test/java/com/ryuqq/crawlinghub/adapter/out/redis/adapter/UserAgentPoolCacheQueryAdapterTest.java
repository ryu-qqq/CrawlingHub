package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.CachedUserAgentRedisMapper;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

/**
 * UserAgentPoolCacheQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("cache")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheQueryAdapter 단위 테스트")
@SuppressWarnings({"unchecked", "rawtypes"})
class UserAgentPoolCacheQueryAdapterTest {

    @Mock private RedissonClient redissonClient;
    @Mock private RMap rMap;
    @Mock private RSet rSet;

    private TimeProvider timeProvider;
    private UserAgentPoolKeyResolver keyResolver;
    private CachedUserAgentRedisMapper mapper;
    private UserAgentPoolProperties properties;
    private UserAgentPoolCacheQueryAdapter adapter;

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
        adapter =
                new UserAgentPoolCacheQueryAdapter(
                        redissonClient, timeProvider, keyResolver, mapper, properties);
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

    @Nested
    @DisplayName("findById - UserAgent 조회")
    class FindByIdTests {

        @Test
        @DisplayName("성공 - 존재하는 UserAgent 조회")
        void shouldReturnCachedUserAgentWhenExists() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";

            Map<String, String> data =
                    createUserAgentData(
                            1L,
                            "Mozilla/5.0",
                            "session-token",
                            FIXED_NOW.plus(Duration.ofHours(1)).toEpochMilli(),
                            80,
                            80,
                            100,
                            UserAgentStatus.IDLE);

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(data);

            // When
            Optional<CachedUserAgent> result = adapter.findById(userAgentId);

            // Then
            assertThat(result).isPresent();
            CachedUserAgent cached = result.get();
            assertThat(cached.userAgentId()).isEqualTo(1L);
            assertThat(cached.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(cached.sessionToken()).isEqualTo("session-token");
            assertThat(cached.status()).isEqualTo(UserAgentStatus.IDLE);
            assertThat(cached.healthScore()).isEqualTo(100);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 UserAgent 조회 시 빈 Optional 반환")
        void shouldReturnEmptyWhenNotExists() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(999L);
            String poolKey = POOL_KEY_PREFIX + "999";

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(Map.of());

            // When
            Optional<CachedUserAgent> result = adapter.findById(userAgentId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 빈 sessionToken은 null로 변환")
        void shouldConvertEmptySessionTokenToNull() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";

            Map<String, String> data =
                    createUserAgentData(
                            1L,
                            "Mozilla/5.0",
                            "",
                            0L,
                            80,
                            80,
                            100,
                            UserAgentStatus.SESSION_REQUIRED);

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(data);

            // When
            Optional<CachedUserAgent> result = adapter.findById(userAgentId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().sessionToken()).isNull();
        }
    }

    @Nested
    @DisplayName("getPoolStats - Pool 통계 조회")
    class GetPoolStatsTests {

        @Test
        @DisplayName("성공 - Pool 통계 정보 반환")
        void shouldReturnPoolStats() {
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

            // idle=5, borrowed=0, cooldown=0, session_required=2, suspended=1
            given(rSet.size()).willReturn(5, 0, 0, 2, 1);
            given(rSet.readAll()).willReturn(Set.of("1", "2", "3", "4", "5"));

            RMap healthMap = createMockHealthMap(80);
            given(redissonClient.getMap(anyString(), any(StringCodec.class))).willReturn(healthMap);

            // When
            PoolStats stats = adapter.getPoolStats();

            // Then
            // total = 5 + 0 + 0 + 2 + 1 = 8, available = 5 + 0 + 0 = 5
            assertThat(stats.total()).isEqualTo(8);
            assertThat(stats.available()).isEqualTo(5);
            assertThat(stats.suspended()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getSessionRequiredUserAgents - 세션 필요 목록 조회")
    class GetSessionRequiredUserAgentsTests {

        @Test
        @DisplayName("성공 - SESSION_REQUIRED 상태 UserAgent ID 목록 반환")
        void shouldReturnSessionRequiredUserAgentIds() {
            // Given
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("1", "2", "3"));

            // When
            List<UserAgentId> result = adapter.getSessionRequiredUserAgents();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).extracting(UserAgentId::value).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("성공 - 빈 목록 반환 (없는 경우)")
        void shouldReturnEmptyListWhenNone() {
            // Given
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of());

            // When
            List<UserAgentId> result = adapter.getSessionRequiredUserAgents();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllSuspendedUserAgents - SUSPENDED 목록 조회")
    class GetAllSuspendedUserAgentsTests {

        @Test
        @DisplayName("성공 - SUSPENDED 상태 UserAgent ID 목록 반환")
        void shouldReturnSuspendedUserAgentIds() {
            // Given
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("10", "20"));

            // When
            List<UserAgentId> result = adapter.getAllSuspendedUserAgents();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserAgentId::value).containsExactlyInAnyOrder(10L, 20L);
        }
    }

    @Nested
    @DisplayName("getCooldownUserAgents - COOLDOWN 목록 조회")
    class GetCooldownUserAgentsTests {

        @Test
        @DisplayName("성공 - COOLDOWN 상태 UserAgent ID 목록 반환")
        void shouldReturnCooldownUserAgentIds() {
            // Given
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("5", "10"));

            // When
            List<UserAgentId> result = adapter.getCooldownUserAgents();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserAgentId::value).containsExactlyInAnyOrder(5L, 10L);
        }
    }

    @Nested
    @DisplayName("getBorrowedUserAgents - BORROWED 목록 조회")
    class GetBorrowedUserAgentsTests {

        @Test
        @DisplayName("성공 - BORROWED 상태 UserAgent ID 목록 반환")
        void shouldReturnBorrowedUserAgentIds() {
            // Given
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("3", "7"));

            // When
            List<UserAgentId> result = adapter.getBorrowedUserAgents();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserAgentId::value).containsExactlyInAnyOrder(3L, 7L);
        }
    }

    @Nested
    @DisplayName("getAllUserAgentIds - 전체 UserAgent ID 조회")
    class GetAllUserAgentIdsTests {

        @Test
        @DisplayName(
                "성공 - 모든 Set의 UserAgent ID 반환 (idle, borrowed, cooldown, session_required,"
                        + " suspended)")
        void shouldReturnAllUserAgentIdsFromAllSets() {
            // Given
            RSet idleSet = org.mockito.Mockito.mock(RSet.class);
            RSet borrowedSet = org.mockito.Mockito.mock(RSet.class);
            RSet cooldownSet = org.mockito.Mockito.mock(RSet.class);
            RSet sessionRequiredSet = org.mockito.Mockito.mock(RSet.class);
            RSet suspendedSet = org.mockito.Mockito.mock(RSet.class);

            given(redissonClient.getSet(eq(IDLE_SET_KEY), any(StringCodec.class)))
                    .willReturn(idleSet);
            given(redissonClient.getSet(eq(BORROWED_SET_KEY), any(StringCodec.class)))
                    .willReturn(borrowedSet);
            given(redissonClient.getSet(eq(COOLDOWN_SET_KEY), any(StringCodec.class)))
                    .willReturn(cooldownSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(sessionRequiredSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(suspendedSet);

            given(idleSet.readAll()).willReturn(Set.of("1", "2"));
            given(borrowedSet.readAll()).willReturn(Set.of("3"));
            given(cooldownSet.readAll()).willReturn(Set.of("4"));
            given(sessionRequiredSet.readAll()).willReturn(Set.of("5"));
            given(suspendedSet.readAll()).willReturn(Set.of("6"));

            // When
            List<UserAgentId> result = adapter.getAllUserAgentIds();

            // Then
            assertThat(result).hasSize(6);
            assertThat(result)
                    .extracting(UserAgentId::value)
                    .containsExactlyInAnyOrder(1L, 2L, 3L, 4L, 5L, 6L);
        }
    }

    private Map<String, String> createUserAgentData(
            Long id,
            String value,
            String sessionToken,
            long sessionExpiresAt,
            int remainingTokens,
            int maxTokens,
            int healthScore,
            UserAgentStatus status) {

        Map<String, String> data = new HashMap<>();
        data.put("userAgentId", String.valueOf(id));
        data.put("userAgentValue", value);
        data.put("sessionToken", sessionToken);
        data.put("sessionExpiresAt", String.valueOf(sessionExpiresAt));
        data.put("remainingTokens", String.valueOf(remainingTokens));
        data.put("maxTokens", String.valueOf(maxTokens));
        data.put("healthScore", String.valueOf(healthScore));
        data.put("status", status.name());
        data.put("windowStart", "0");
        data.put("windowEnd", "0");
        data.put("suspendedAt", "0");
        return data;
    }

    private RMap createMockHealthMap(int healthScore) {
        RMap mockMap = org.mockito.Mockito.mock(RMap.class);
        given(mockMap.get("healthScore")).willReturn(String.valueOf(healthScore));
        return mockMap;
    }
}
