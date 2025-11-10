package com.ryuqq.crawlinghub.domain.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RequestUrl Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@DisplayName("RequestUrl Value Object 단위 테스트")
class RequestUrlTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "https://example.com",
            "http://test.com/api",
            "https://mustit.co.kr/product/123",
            "http://localhost:8080/test"
        })
        @DisplayName("유효한 URL로 RequestUrl 생성 성공")
        void shouldCreateWithValidUrl(String validUrl) {
            // When
            RequestUrl requestUrl = RequestUrl.of(validUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(validUrl);
        }

        @Test
        @DisplayName("HTTPS URL로 생성 성공")
        void shouldCreateWithHttpsUrl() {
            // Given
            String httpsUrl = "https://secure.example.com/api/v1/products";

            // When
            RequestUrl requestUrl = RequestUrl.of(httpsUrl);

            // Then
            assertThat(requestUrl.getValue()).isEqualTo(httpsUrl);
        }

        @Test
        @DisplayName("HTTP URL로 생성 성공")
        void shouldCreateWithHttpUrl() {
            // Given
            String httpUrl = "http://example.com/test";

            // When
            RequestUrl requestUrl = RequestUrl.of(httpUrl);

            // Then
            assertThat(requestUrl.getValue()).isEqualTo(httpUrl);
        }

        @Test
        @DisplayName("쿼리 파라미터가 있는 URL로 생성 성공")
        void shouldCreateWithQueryParameters() {
            // Given
            String urlWithQuery = "https://example.com/api?page=1&size=20";

            // When
            RequestUrl requestUrl = RequestUrl.of(urlWithQuery);

            // Then
            assertThat(requestUrl.getValue()).isEqualTo(urlWithQuery);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t", "\n"})
        @DisplayName("빈 문자열이나 공백 문자열 입력 시 예외 발생")
        void shouldThrowExceptionWhenBlankString(String blankStr) {
            // When & Then
            assertThatThrownBy(() -> RequestUrl.of(blankStr))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null URL로 생성 시 예외 발생")
        void shouldThrowExceptionWhenNullUrl(String nullUrl) {
            // When & Then
            assertThatThrownBy(() -> RequestUrl.of(nullUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-url",
            "htp://wrong-protocol.com",
            "://no-protocol.com",
            "example.com"
        })
        @DisplayName("유효하지 않은 URL 형식으로 생성 시 예외 발생")
        void shouldThrowExceptionWhenInvalidUrlFormat(String invalidUrl) {
            // When & Then
            assertThatThrownBy(() -> RequestUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유효하지 않은 URL 형식입니다");
        }
    }

    @Nested
    @DisplayName("동등성 비교 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 URL을 가진 두 RequestUrl는 같다")
        void shouldBeEqualForSameUrl() {
            // Given
            String url = "https://example.com/api";
            RequestUrl requestUrl1 = RequestUrl.of(url);
            RequestUrl requestUrl2 = RequestUrl.of(url);

            // When & Then
            assertThat(requestUrl1).isEqualTo(requestUrl2);
        }

        @Test
        @DisplayName("다른 URL을 가진 두 RequestUrl는 다르다")
        void shouldNotBeEqualForDifferentUrl() {
            // Given
            RequestUrl requestUrl1 = RequestUrl.of("https://example.com/api1");
            RequestUrl requestUrl2 = RequestUrl.of("https://example.com/api2");

            // When & Then
            assertThat(requestUrl1).isNotEqualTo(requestUrl2);
        }

        @Test
        @DisplayName("같은 URL을 가진 두 RequestUrl는 같은 hashCode를 반환한다")
        void shouldReturnSameHashCodeForSameUrl() {
            // Given
            String url = "https://example.com/api";
            RequestUrl requestUrl1 = RequestUrl.of(url);
            RequestUrl requestUrl2 = RequestUrl.of(url);

            // When & Then
            assertThat(requestUrl1.hashCode()).isEqualTo(requestUrl2.hashCode());
        }
    }

    @Nested
    @DisplayName("isSameAs() 테스트")
    class IsSameAsTests {

        @Test
        @DisplayName("isSameAs() - 같은 URL이면 true 반환")
        void shouldReturnTrueWhenSameUrl() {
            // Given
            String url = "https://example.com/api";
            RequestUrl requestUrl1 = RequestUrl.of(url);
            RequestUrl requestUrl2 = RequestUrl.of(url);

            // When
            boolean isSame = requestUrl1.isSameAs(requestUrl2);

            // Then
            assertThat(isSame).isTrue();
        }

        @Test
        @DisplayName("isSameAs() - 다른 URL이면 false 반환")
        void shouldReturnFalseWhenDifferentUrl() {
            // Given
            RequestUrl requestUrl1 = RequestUrl.of("https://example.com/api1");
            RequestUrl requestUrl2 = RequestUrl.of("https://example.com/api2");

            // When
            boolean isSame = requestUrl1.isSameAs(requestUrl2);

            // Then
            assertThat(isSame).isFalse();
        }

        @Test
        @DisplayName("isSameAs() - null과 비교 시 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            RequestUrl requestUrl = RequestUrl.of("https://example.com/api");

            // When
            boolean isSame = requestUrl.isSameAs(null);

            // Then
            assertThat(isSame).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 URL 문자열을 반환한다")
        void shouldReturnUrlStringFromToString() {
            // Given
            String url = "https://example.com/api";
            RequestUrl requestUrl = RequestUrl.of(url);

            // When
            String result = requestUrl.toString();

            // Then
            assertThat(result).isEqualTo(url);
        }
    }
}
