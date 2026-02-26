package com.ryuqq.crawlinghub.adapter.out.redis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;

/**
 * RedisConfig 단위 테스트
 *
 * <p>Redis 설정 클래스의 Redisson 커스터마이저 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("config")
@DisplayName("RedisConfig 단위 테스트")
class RedisConfigTest {

    @Nested
    @DisplayName("redissonCustomizer 빈")
    class RedissonCustomizerTests {

        @Test
        @DisplayName("성공 - 커스터마이저가 null이 아님")
        void shouldReturnNonNullCustomizer() {
            // Given
            RedisConfig redisConfig = new RedisConfig();

            // When
            RedissonAutoConfigurationCustomizer customizer = redisConfig.redissonCustomizer();

            // Then
            assertThat(customizer).isNotNull();
        }

        @Test
        @DisplayName("성공 - 커스터마이저가 Config에 정상 적용됨")
        void shouldApplyCustomizerToConfig() {
            // Given
            RedisConfig redisConfig = new RedisConfig();
            RedissonAutoConfigurationCustomizer customizer = redisConfig.redissonCustomizer();
            Config config = new Config();
            config.useSingleServer().setAddress("redis://localhost:6379");

            // When & Then
            assertThatNoException().isThrownBy(() -> customizer.customize(config));
        }

        @Test
        @DisplayName("성공 - keepAlive와 pingConnectionInterval이 설정됨")
        void shouldSetKeepAliveAndPingInterval() {
            // Given
            RedisConfig redisConfig = new RedisConfig();
            RedissonAutoConfigurationCustomizer customizer = redisConfig.redissonCustomizer();
            Config config = new Config();
            config.useSingleServer().setAddress("redis://localhost:6379");

            // When
            customizer.customize(config);

            // Then
            assertThat(config.useSingleServer().isKeepAlive()).isTrue();
            assertThat(config.useSingleServer().getPingConnectionInterval()).isEqualTo(30_000);
        }

        @Test
        @DisplayName("성공 - 연결 풀과 재시도 설정이 적용됨")
        void shouldSetConnectionPoolAndRetrySettings() {
            // Given
            RedisConfig redisConfig = new RedisConfig();
            RedissonAutoConfigurationCustomizer customizer = redisConfig.redissonCustomizer();
            Config config = new Config();
            config.useSingleServer().setAddress("redis://localhost:6379");

            // When
            customizer.customize(config);

            // Then
            assertThat(config.useSingleServer().getConnectionMinimumIdleSize()).isEqualTo(4);
            assertThat(config.useSingleServer().getConnectionPoolSize()).isEqualTo(16);
            assertThat(config.useSingleServer().getRetryAttempts()).isEqualTo(3);
            assertThat(config.useSingleServer().getRetryInterval()).isEqualTo(1500);
        }
    }
}
