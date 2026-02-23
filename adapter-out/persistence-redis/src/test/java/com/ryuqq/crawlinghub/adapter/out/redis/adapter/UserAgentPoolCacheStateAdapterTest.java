package com.ryuqq.crawlinghub.adapter.out.redis.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.adapter.out.redis.config.UserAgentPoolProperties;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolKeyResolver;
import com.ryuqq.crawlinghub.adapter.out.redis.support.UserAgentPoolLuaScriptHolder;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

/**
 * UserAgentPoolCacheStateAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("cache")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserAgentPoolCacheStateAdapter 단위 테스트")
@SuppressWarnings({"unchecked", "rawtypes"})
class UserAgentPoolCacheStateAdapterTest {

    @Mock private RedissonClient redissonClient;
    @Mock private RMap rMap;
    @Mock private RScript rScript;
    @Mock private UserAgentPoolLuaScriptHolder luaScriptHolder;

    private TimeProvider timeProvider;
    private UserAgentPoolKeyResolver keyResolver;
    private UserAgentPoolProperties properties;

    private static final Instant FIXED_NOW = Instant.parse("2024-01-15T10:00:00Z");
    private static final String KEY_PREFIX = "useragent:";
    private static final String POOL_KEY_PREFIX = KEY_PREFIX + "pool:";

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        timeProvider = new TimeProvider(fixedClock);
        properties = createDefaultProperties();
        keyResolver = new UserAgentPoolKeyResolver(properties);
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

    private UserAgentPoolCacheStateAdapter createAdapter() {
        return new UserAgentPoolCacheStateAdapter(
                redissonClient, timeProvider, keyResolver, properties, luaScriptHolder);
    }

    @Nested
    @DisplayName("applyHealthDelta - 양수 (증가)")
    class ApplyPositiveDelta {

        @Test
        @DisplayName("성공 - recordSuccess Lua 스크립트 실행, false 반환")
        void shouldExecuteSuccessScriptAndReturnFalse() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(luaScriptHolder.recordSuccessScript()).willReturn("mock_record_success");
            given(redissonClient.getScript(any(StringCodec.class))).willReturn(rScript);

            UserAgentPoolCacheStateAdapter adapter = createAdapter();

            // When
            boolean result = adapter.applyHealthDelta(userAgentId, 5);

            // Then
            assertThat(result).isFalse();
            verify(rScript)
                    .eval(
                            eq(RScript.Mode.READ_WRITE),
                            anyString(),
                            eq(RScript.ReturnType.INTEGER),
                            any(),
                            eq("5"));
        }
    }

    @Nested
    @DisplayName("applyHealthDelta - 음수 (감소)")
    class ApplyNegativeDelta {

        @Test
        @DisplayName("SUSPENDED 전환 시 true 반환")
        void shouldReturnTrueWhenSuspended() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(luaScriptHolder.recordFailureScript()).willReturn("mock_record_failure");
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

            UserAgentPoolCacheStateAdapter adapter = createAdapter();

            // When
            boolean result = adapter.applyHealthDelta(userAgentId, -10);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 미전환 시 false 반환")
        void shouldReturnFalseWhenNotSuspended() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            given(luaScriptHolder.recordFailureScript()).willReturn("mock_record_failure");
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

            UserAgentPoolCacheStateAdapter adapter = createAdapter();

            // When
            boolean result = adapter.applyHealthDelta(userAgentId, -5);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("applyHealthDelta - 0 delta")
    class ApplyZeroDelta {

        @Test
        @DisplayName("delta 0이면 false 반환, 스크립트 미실행")
        void shouldReturnFalseForZeroDelta() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            UserAgentPoolCacheStateAdapter adapter = createAdapter();

            // When
            boolean result = adapter.applyHealthDelta(userAgentId, 0);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("setHealthScore - Health Score 직접 설정")
    class SetHealthScore {

        @Test
        @DisplayName("성공 - Redis Hash에 healthScore 설정")
        void shouldSetHealthScoreInRedis() {
            // Given
            UserAgentId userAgentId = UserAgentId.of(1L);
            String poolKey = POOL_KEY_PREFIX + "1";
            given(redissonClient.getMap(eq(poolKey), any(StringCodec.class))).willReturn(rMap);

            UserAgentPoolCacheStateAdapter adapter = createAdapter();

            // When
            adapter.setHealthScore(userAgentId, 100);

            // Then
            verify(rMap).put("healthScore", "100");
        }
    }
}
