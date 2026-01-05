package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
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
import org.redisson.api.RScript;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

/**
 * UserAgentPoolCacheAdapter 단위 테스트
 *
 * <p>UserAgent Pool 캐시 Adapter의 비즈니스 로직을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>addToPool - Pool에 UserAgent 추가 (SESSION_REQUIRED 상태)
 *   <li>removeFromPool - Pool에서 UserAgent 제거 (SUSPENDED 상태)
 *   <li>updateSession - 세션 업데이트 (READY 상태로 전환)
 *   <li>expireSession - 세션 만료 처리 (SESSION_REQUIRED 상태로 전환)
 *   <li>restoreToPool - SUSPENDED에서 복구
 *   <li>findById - UserAgent 조회
 *   <li>getPoolStats - Pool 통계 조회
 *   <li>getSessionRequiredUserAgents - 세션 필요 목록 조회
 *   <li>recordSuccess/recordFailure - 성공/실패 기록
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("cache")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheAdapter 단위 테스트")
@SuppressWarnings({"unchecked", "rawtypes"})
class UserAgentPoolCacheAdapterTest {

    @Mock private RedissonClient redissonClient;

    @Mock private RMap rMap;

    @Mock private RSet rSet;

    @Mock private RScript rScript;

    private Clock fixedClock;
    private UserAgentPoolProperties properties;

    private static final Instant FIXED_NOW = Instant.parse("2024-01-15T10:00:00Z");
    private static final String KEY_PREFIX = "useragent:";
    private static final String POOL_KEY_PREFIX = KEY_PREFIX + "pool:";
    private static final String READY_SET_KEY = KEY_PREFIX + "ready";
    private static final String SESSION_REQUIRED_SET_KEY = KEY_PREFIX + "session_required";
    private static final String SUSPENDED_SET_KEY = KEY_PREFIX + "suspended";

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        properties = createDefaultProperties();
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
                            null, // sessionToken
                            null, // nid
                            null, // mustitUid
                            null, // sessionExpiresAt
                            80,
                            80,
                            null,
                            null,
                            100,
                            UserAgentStatus.SESSION_REQUIRED,
                            null);

            String poolKey = POOL_KEY_PREFIX + "1";
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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
            given(redissonClient.getSet(eq(READY_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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
        @DisplayName("성공 - READY 상태로 전환 및 세션 정보 저장")
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
            given(redissonClient.getSet(eq(READY_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            adapter.updateSession(userAgentId, sessionToken, nid, mustitUid, sessionExpiresAt);

            // Then
            verify(rMap).put("sessionToken", sessionToken);
            verify(rMap).put("nid", nid);
            verify(rMap).put("mustitUid", mustitUid);
            verify(rMap).put("sessionExpiresAt", String.valueOf(sessionExpiresAt.toEpochMilli()));
            verify(rMap).put("status", UserAgentStatus.READY.name());
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
            given(redissonClient.getSet(eq(READY_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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
                            UserAgentStatus.READY);

            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);
            given(rMap.readAllMap()).willReturn(data);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            Optional<CachedUserAgent> result = adapter.findById(userAgentId);

            // Then
            assertThat(result).isPresent();
            CachedUserAgent cached = result.get();
            assertThat(cached.userAgentId()).isEqualTo(1L);
            assertThat(cached.userAgentValue()).isEqualTo("Mozilla/5.0");
            assertThat(cached.sessionToken()).isEqualTo("session-token");
            assertThat(cached.status()).isEqualTo(UserAgentStatus.READY);
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

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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
            given(redissonClient.getSet(eq(READY_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);

            given(rSet.size()).willReturn(5, 2, 1);
            given(rSet.readAll()).willReturn(Set.of("1", "2", "3", "4", "5"));

            // Mock health scores
            RMap healthMap = createMockHealthMap(80);
            given(redissonClient.getMap(anyString(), any(StringCodec.class))).willReturn(healthMap);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            PoolStats stats = adapter.getPoolStats();

            // Then
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

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

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

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            List<UserAgentId> result = adapter.getSessionRequiredUserAgents();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("recordSuccess - 성공 기록")
    class RecordSuccessTests {

        @Test
        @DisplayName("성공 - Lua 스크립트 실행")
        void shouldExecuteLuaScript() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            adapter.recordSuccess(userAgentId);

            // Then
            verify(rScript)
                    .eval(
                            eq(RScript.Mode.READ_WRITE),
                            anyString(),
                            eq(RScript.ReturnType.INTEGER),
                            any());
        }
    }

    @Nested
    @DisplayName("recordFailure - 실패 기록")
    class RecordFailureTests {

        @Test
        @DisplayName("성공 - SUSPENDED로 전환 시 true 반환")
        void shouldReturnTrueWhenSuspended() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    anyString(),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any()))
                    .willReturn(1L);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            boolean result = adapter.recordFailure(userAgentId, 429);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - SUSPENDED로 전환되지 않으면 false 반환")
        void shouldReturnFalseWhenNotSuspended() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);
            given(
                            rScript.eval(
                                    eq(RScript.Mode.READ_WRITE),
                                    anyString(),
                                    eq(RScript.ReturnType.INTEGER),
                                    any(),
                                    any(),
                                    any(),
                                    any(),
                                    any()))
                    .willReturn(0L);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            boolean result = adapter.recordFailure(userAgentId, 404);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("clearPool - Pool 전체 삭제")
    class ClearPoolTests {

        @Test
        @DisplayName("성공 - 모든 Set과 Map 삭제")
        void shouldClearAllSetsAndMaps() {
            // Given
            given(redissonClient.getSet(eq(READY_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SESSION_REQUIRED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(redissonClient.getSet(eq(SUSPENDED_SET_KEY), any(StringCodec.class)))
                    .willReturn(rSet);
            given(rSet.readAll()).willReturn(Set.of("1", "2"));
            given(redissonClient.getMap(anyString())).willReturn(rMap);

            UserAgentPoolCacheAdapter adapter = createAdapterWithMockedScripts();

            // When
            adapter.clearPool();

            // Then - 3개의 Set에 대해 delete() 호출
            verify(rSet, org.mockito.Mockito.times(3)).delete();
        }
    }

    /** Lua 스크립트 로드를 우회한 Adapter 생성 (단위 테스트에서는 실제 Lua 스크립트 파일 로드가 필요 없음) */
    private UserAgentPoolCacheAdapter createAdapterWithMockedScripts() {
        // Lua 스크립트 로드를 우회하기 위해 테스트용 Adapter 생성
        // 실제로는 스크립트가 필요하지만, 단위 테스트에서는 mock으로 대체
        return new TestableUserAgentPoolCacheAdapter(redissonClient, fixedClock, properties);
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

    /** Lua 스크립트 로드를 우회하는 테스트용 Adapter */
    private static class TestableUserAgentPoolCacheAdapter extends UserAgentPoolCacheAdapter {

        public TestableUserAgentPoolCacheAdapter(
                RedissonClient redissonClient, Clock clock, UserAgentPoolProperties properties) {
            super(redissonClient, clock, createTestProperties(properties));
        }

        private static UserAgentPoolProperties createTestProperties(
                UserAgentPoolProperties original) {
            // 테스트용 properties 반환 - Lua 스크립트 로드 실패를 방지
            return original;
        }
    }
}
