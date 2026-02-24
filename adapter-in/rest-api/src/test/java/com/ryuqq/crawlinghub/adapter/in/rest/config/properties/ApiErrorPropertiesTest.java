package com.ryuqq.crawlinghub.adapter.in.rest.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ApiErrorProperties 단위 테스트
 *
 * <p>에러 타입 URI 생성 로직 및 기본값을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ApiErrorProperties 단위 테스트")
class ApiErrorPropertiesTest {

    @Nested
    @DisplayName("기본값 검증")
    class DefaultValuesTest {

        @Test
        @DisplayName("기본 baseUrl은 about:blank이다")
        void shouldHaveDefaultBaseUrl() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();

            // Then
            assertThat(properties.getBaseUrl()).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("기본 useAboutBlank는 true이다")
        void shouldHaveDefaultUseAboutBlankAsTrue() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();

            // Then
            assertThat(properties.isUseAboutBlank()).isTrue();
        }
    }

    @Nested
    @DisplayName("buildTypeUri() 메서드는")
    class BuildTypeUriTest {

        @Test
        @DisplayName("useAboutBlank가 true이면 항상 about:blank를 반환한다")
        void shouldReturnAboutBlankWhenUseAboutBlankIsTrue() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();
            properties.setUseAboutBlank(true);
            properties.setBaseUrl("https://api.example.com/problems");

            // When
            String result = properties.buildTypeUri("test-error");

            // Then
            assertThat(result).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("baseUrl이 about:blank이면 about:blank를 반환한다")
        void shouldReturnAboutBlankWhenBaseUrlIsAboutBlank() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("about:blank");

            // When
            String result = properties.buildTypeUri("test-error");

            // Then
            assertThat(result).isEqualTo("about:blank");
        }

        @Test
        @DisplayName("useAboutBlank가 false이고 baseUrl이 설정된 경우 전체 URI를 반환한다")
        void shouldReturnFullUriWhenConfigured() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems");

            // When
            String result = properties.buildTypeUri("seller-not-found");

            // Then
            assertThat(result).isEqualTo("https://api.example.com/problems/seller-not-found");
        }

        @Test
        @DisplayName("baseUrl이 슬래시로 끝나면 중복 슬래시 없이 URI를 반환한다")
        void shouldHandleTrailingSlashInBaseUrl() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();
            properties.setUseAboutBlank(false);
            properties.setBaseUrl("https://api.example.com/problems/");

            // When
            String result = properties.buildTypeUri("seller-not-found");

            // Then
            assertThat(result).isEqualTo("https://api.example.com/problems/seller-not-found");
            assertThat(result).doesNotContain("//seller-not-found");
        }

        @Test
        @DisplayName("기본 설정(useAboutBlank=true)으로 about:blank를 반환한다")
        void shouldReturnAboutBlankWithDefaultSettings() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();

            // When
            String result = properties.buildTypeUri("any-error");

            // Then
            assertThat(result).isEqualTo("about:blank");
        }
    }

    @Nested
    @DisplayName("Setter/Getter 검증")
    class SetterGetterTest {

        @Test
        @DisplayName("baseUrl을 설정하고 가져올 수 있다")
        void shouldSetAndGetBaseUrl() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();
            String newUrl = "https://new.api.com/errors";

            // When
            properties.setBaseUrl(newUrl);

            // Then
            assertThat(properties.getBaseUrl()).isEqualTo(newUrl);
        }

        @Test
        @DisplayName("useAboutBlank를 false로 설정할 수 있다")
        void shouldSetUseAboutBlankToFalse() {
            // Given
            ApiErrorProperties properties = new ApiErrorProperties();

            // When
            properties.setUseAboutBlank(false);

            // Then
            assertThat(properties.isUseAboutBlank()).isFalse();
        }
    }
}
