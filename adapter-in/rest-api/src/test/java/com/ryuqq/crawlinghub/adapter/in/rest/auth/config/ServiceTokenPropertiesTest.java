package com.ryuqq.crawlinghub.adapter.in.rest.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ServiceTokenProperties 단위 테스트
 *
 * <p>Service Token 인증 설정 Record의 기본 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ServiceTokenProperties 단위 테스트")
class ServiceTokenPropertiesTest {

    @Nested
    @DisplayName("생성 검증")
    class ConstructionTest {

        @Test
        @DisplayName("enabled와 secret으로 생성할 수 있다")
        void shouldCreateWithEnabledAndSecret() {
            // When
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "my-secret");

            // Then
            assertThat(properties.enabled()).isTrue();
            assertThat(properties.secret()).isEqualTo("my-secret");
        }

        @Test
        @DisplayName("disabled 상태로 생성할 수 있다")
        void shouldCreateWithDisabledState() {
            // When
            ServiceTokenProperties properties = new ServiceTokenProperties(false, "any-secret");

            // Then
            assertThat(properties.enabled()).isFalse();
        }

        @Test
        @DisplayName("secret이 null이면 빈 문자열로 대체된다")
        void shouldReplaceNullSecretWithEmptyString() {
            // When
            ServiceTokenProperties properties = new ServiceTokenProperties(true, null);

            // Then
            assertThat(properties.secret()).isEqualTo("");
        }

        @Test
        @DisplayName("secret이 빈 문자열이면 그대로 유지된다")
        void shouldKeepEmptyStringSecret() {
            // When
            ServiceTokenProperties properties = new ServiceTokenProperties(false, "");

            // Then
            assertThat(properties.secret()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("record 동작 검증")
    class RecordBehaviorTest {

        @Test
        @DisplayName("동일한 값으로 생성된 두 인스턴스는 동등하다")
        void shouldBeEqualWhenSameValues() {
            // Given
            ServiceTokenProperties properties1 = new ServiceTokenProperties(true, "secret");
            ServiceTokenProperties properties2 = new ServiceTokenProperties(true, "secret");

            // Then
            assertThat(properties1).isEqualTo(properties2);
        }

        @Test
        @DisplayName("다른 secret을 가진 두 인스턴스는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentSecret() {
            // Given
            ServiceTokenProperties properties1 = new ServiceTokenProperties(true, "secret1");
            ServiceTokenProperties properties2 = new ServiceTokenProperties(true, "secret2");

            // Then
            assertThat(properties1).isNotEqualTo(properties2);
        }

        @Test
        @DisplayName("enabled 값이 다른 두 인스턴스는 동등하지 않다")
        void shouldNotBeEqualWhenDifferentEnabled() {
            // Given
            ServiceTokenProperties properties1 = new ServiceTokenProperties(true, "secret");
            ServiceTokenProperties properties2 = new ServiceTokenProperties(false, "secret");

            // Then
            assertThat(properties1).isNotEqualTo(properties2);
        }

        @Test
        @DisplayName("toString()이 정상적으로 동작한다")
        void shouldHaveToString() {
            // Given
            ServiceTokenProperties properties = new ServiceTokenProperties(true, "test-secret");

            // When
            String result = properties.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("ServiceTokenProperties");
        }
    }
}
