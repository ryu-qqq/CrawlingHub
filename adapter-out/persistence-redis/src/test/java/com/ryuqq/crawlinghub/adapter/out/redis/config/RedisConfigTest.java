package com.ryuqq.crawlinghub.adapter.out.redis.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

/**
 * RedisConfig 단위 테스트
 *
 * <p>Redis 설정 클래스의 생성 및 의존성 주입을 검증합니다.
 *
 * <p>참고: Redisson Spring Boot Starter가 RedissonClient Bean을 자동 생성하므로 RedisConfig는 설정
 * 활성화(@EnableConfigurationProperties) 역할만 합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("config")
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig 단위 테스트")
class RedisConfigTest {

    @Mock private RedissonClient redissonClient;

    @Nested
    @DisplayName("생성자 주입")
    class ConstructorInjectionTests {

        @Test
        @DisplayName("성공 - RedissonClient와 RedisProperties로 정상 생성")
        void shouldCreateRedisConfigSuccessfully() {
            // Given
            RedisProperties redisProperties = new RedisProperties();

            // When & Then
            assertThatNoException()
                    .isThrownBy(() -> new RedisConfig(redissonClient, redisProperties));
        }

        @Test
        @DisplayName("성공 - 생성된 RedisConfig 인스턴스가 null이 아님")
        void shouldNotBeNull() {
            // Given
            RedisProperties redisProperties = new RedisProperties();

            // When
            RedisConfig redisConfig = new RedisConfig(redissonClient, redisProperties);

            // Then
            assertThat(redisConfig).isNotNull();
        }

        @Test
        @DisplayName("성공 - 기본 keyPrefix를 가진 RedisProperties로 생성")
        void shouldCreateWithDefaultKeyPrefix() {
            // Given
            RedisProperties redisProperties = new RedisProperties();
            assertThat(redisProperties.getKeyPrefix()).isEqualTo("crawlinghub:lock:");

            // When & Then
            assertThatNoException()
                    .isThrownBy(() -> new RedisConfig(redissonClient, redisProperties));
        }

        @Test
        @DisplayName("성공 - 커스텀 설정을 가진 RedisProperties로 생성")
        void shouldCreateWithCustomProperties() {
            // Given
            RedisProperties redisProperties = new RedisProperties();
            redisProperties.setKeyPrefix("custom:prefix:");
            redisProperties.setDefaultWaitTimeMs(10_000L);
            redisProperties.setDefaultLeaseTimeMs(60_000L);

            // When & Then
            assertThatNoException()
                    .isThrownBy(() -> new RedisConfig(redissonClient, redisProperties));
        }

        @Test
        @DisplayName("성공 - RedissonClient에 별도 설정 호출 없음 (자동 설정 위임)")
        void shouldNotCallRedissonClientDuringConstruction() {
            // Given
            RedisProperties redisProperties = new RedisProperties();

            // When
            new RedisConfig(redissonClient, redisProperties);

            // Then
            then(redissonClient).shouldHaveNoInteractions();
        }
    }
}
