package com.ryuqq.crawlinghub.adapter.out.redis.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * UserAgentPoolProperties 단위 테스트
 *
 * <p>UserAgent Pool 설정 Properties의 기본값과 동작을 검증합니다.
 *
 * <p>테스트 범위:
 *
 * <ul>
 *   <li>RateLimit 설정 - maxTokens, windowDuration
 *   <li>Session 설정 - renewalBufferMinutes
 *   <li>Health 설정 - suspensionThreshold
 *   <li>keyPrefix 설정
 *   <li>Setter/Getter 동작
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("redis")
@Tag("config")
@DisplayName("UserAgentPoolProperties 단위 테스트")
class UserAgentPoolPropertiesTest {

    private UserAgentPoolProperties properties;

    @BeforeEach
    void setUp() {
        properties = new UserAgentPoolProperties();
    }

    @Nested
    @DisplayName("RateLimit 기본값")
    class RateLimitDefaultsTests {

        @Test
        @DisplayName("성공 - 기본 maxTokens 80")
        void shouldHaveDefaultMaxTokensOf80() {
            assertThat(properties.getRateLimit().getMaxTokens()).isEqualTo(80);
        }

        @Test
        @DisplayName("성공 - 기본 windowDuration 1시간")
        void shouldHaveDefaultWindowDurationOf1Hour() {
            assertThat(properties.getRateLimit().getWindowDuration())
                    .isEqualTo(Duration.ofHours(1));
        }

        @Test
        @DisplayName("성공 - RateLimit 설정 변경")
        void shouldAllowRateLimitSettingsChange() {
            // Given
            UserAgentPoolProperties.RateLimit rateLimit = new UserAgentPoolProperties.RateLimit();
            rateLimit.setMaxTokens(100);
            rateLimit.setWindowDuration(Duration.ofMinutes(30));

            // When
            properties.setRateLimit(rateLimit);

            // Then
            assertThat(properties.getRateLimit().getMaxTokens()).isEqualTo(100);
            assertThat(properties.getRateLimit().getWindowDuration())
                    .isEqualTo(Duration.ofMinutes(30));
        }
    }

    @Nested
    @DisplayName("Session 기본값")
    class SessionDefaultsTests {

        @Test
        @DisplayName("성공 - 기본 renewalBufferMinutes 5분")
        void shouldHaveDefaultRenewalBufferOf5Minutes() {
            assertThat(properties.getSession().getRenewalBufferMinutes()).isEqualTo(5);
        }

        @Test
        @DisplayName("성공 - Session 설정 변경")
        void shouldAllowSessionSettingsChange() {
            // Given
            UserAgentPoolProperties.Session session = new UserAgentPoolProperties.Session();
            session.setRenewalBufferMinutes(10);

            // When
            properties.setSession(session);

            // Then
            assertThat(properties.getSession().getRenewalBufferMinutes()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Health 기본값")
    class HealthDefaultsTests {

        @Test
        @DisplayName("성공 - 기본 suspensionThreshold 30")
        void shouldHaveDefaultSuspensionThresholdOf30() {
            assertThat(properties.getHealth().getSuspensionThreshold()).isEqualTo(30);
        }

        @Test
        @DisplayName("성공 - Health 설정 변경")
        void shouldAllowHealthSettingsChange() {
            // Given
            UserAgentPoolProperties.Health health = new UserAgentPoolProperties.Health();
            health.setSuspensionThreshold(20);

            // When
            properties.setHealth(health);

            // Then
            assertThat(properties.getHealth().getSuspensionThreshold()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("keyPrefix 설정")
    class KeyPrefixTests {

        @Test
        @DisplayName("성공 - 기본 keyPrefix 'useragent:'")
        void shouldHaveDefaultKeyPrefix() {
            assertThat(properties.getKeyPrefix()).isEqualTo("useragent:");
        }

        @Test
        @DisplayName("성공 - keyPrefix 설정 변경")
        void shouldAllowKeyPrefixChange() {
            // When
            properties.setKeyPrefix("custom:ua:");

            // Then
            assertThat(properties.getKeyPrefix()).isEqualTo("custom:ua:");
        }
    }

    @Nested
    @DisplayName("RateLimit Setter/Getter")
    class RateLimitSetterGetterTests {

        @Test
        @DisplayName("성공 - maxTokens 개별 설정")
        void shouldSetMaxTokensIndividually() {
            // When
            properties.getRateLimit().setMaxTokens(150);

            // Then
            assertThat(properties.getRateLimit().getMaxTokens()).isEqualTo(150);
        }

        @Test
        @DisplayName("성공 - windowDuration 개별 설정")
        void shouldSetWindowDurationIndividually() {
            // When
            properties.getRateLimit().setWindowDuration(Duration.ofMinutes(45));

            // Then
            assertThat(properties.getRateLimit().getWindowDuration())
                    .isEqualTo(Duration.ofMinutes(45));
        }
    }

    @Nested
    @DisplayName("Session Setter/Getter")
    class SessionSetterGetterTests {

        @Test
        @DisplayName("성공 - renewalBufferMinutes 개별 설정")
        void shouldSetRenewalBufferMinutesIndividually() {
            // When
            properties.getSession().setRenewalBufferMinutes(15);

            // Then
            assertThat(properties.getSession().getRenewalBufferMinutes()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Health Setter/Getter")
    class HealthSetterGetterTests {

        @Test
        @DisplayName("성공 - suspensionThreshold 개별 설정")
        void shouldSetSuspensionThresholdIndividually() {
            // When
            properties.getHealth().setSuspensionThreshold(40);

            // Then
            assertThat(properties.getHealth().getSuspensionThreshold()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("통합 설정 검증")
    class IntegrationTests {

        @Test
        @DisplayName("성공 - 모든 설정 동시 변경")
        void shouldAllowAllSettingsChangeSimultaneously() {
            // Given
            UserAgentPoolProperties.RateLimit rateLimit = new UserAgentPoolProperties.RateLimit();
            rateLimit.setMaxTokens(120);
            rateLimit.setWindowDuration(Duration.ofMinutes(90));

            UserAgentPoolProperties.Session session = new UserAgentPoolProperties.Session();
            session.setRenewalBufferMinutes(8);

            UserAgentPoolProperties.Health health = new UserAgentPoolProperties.Health();
            health.setSuspensionThreshold(25);

            // When
            properties.setRateLimit(rateLimit);
            properties.setSession(session);
            properties.setHealth(health);
            properties.setKeyPrefix("test:prefix:");

            // Then
            assertThat(properties.getRateLimit().getMaxTokens()).isEqualTo(120);
            assertThat(properties.getRateLimit().getWindowDuration())
                    .isEqualTo(Duration.ofMinutes(90));
            assertThat(properties.getSession().getRenewalBufferMinutes()).isEqualTo(8);
            assertThat(properties.getHealth().getSuspensionThreshold()).isEqualTo(25);
            assertThat(properties.getKeyPrefix()).isEqualTo("test:prefix:");
        }
    }
}
