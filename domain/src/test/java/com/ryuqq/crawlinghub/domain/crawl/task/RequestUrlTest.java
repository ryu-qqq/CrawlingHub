package com.ryuqq.crawlinghub.domain.crawl.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * RequestUrl Value Object 단위 테스트
 *
 * @author ryu-qqq
 * @since 2025-01-30
 */
@DisplayName("RequestUrl Value Object 단위 테스트")
class RequestUrlTest {

    @Nested
    @DisplayName("생성 테스트 (Happy Path)")
    class CreateTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "https://example.com",
            "https://example.com/path",
            "https://example.com/path?query=value",
            "https://example.com:8080/path",
            "http://example.com",
            "http://localhost:3000",
            "https://subdomain.example.com",
            "https://example.com/path/to/resource",
            "https://example.com/path?key1=value1&key2=value2",
            "https://example.com/path#fragment",
            "ftp://ftp.example.com/file.txt",
            "https://example.com/한글경로",
            "https://example.com/path?한글=값"
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
        @DisplayName("HTTPS URL로 RequestUrl 생성 성공")
        void shouldCreateWithHttpsUrl() {
            // Given
            String httpsUrl = "https://secure.example.com/api/v1/data";

            // When
            RequestUrl requestUrl = RequestUrl.of(httpsUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(httpsUrl);
        }

        @Test
        @DisplayName("HTTP URL로 RequestUrl 생성 성공")
        void shouldCreateWithHttpUrl() {
            // Given
            String httpUrl = "http://example.com/data";

            // When
            RequestUrl requestUrl = RequestUrl.of(httpUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(httpUrl);
        }

        @Test
        @DisplayName("쿼리 파라미터가 있는 URL로 생성 성공")
        void shouldCreateWithQueryParameters() {
            // Given
            String urlWithQuery = "https://api.example.com/search?q=test&page=1&size=10";

            // When
            RequestUrl requestUrl = RequestUrl.of(urlWithQuery);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(urlWithQuery);
        }

        @Test
        @DisplayName("포트 번호가 있는 URL로 생성 성공")
        void shouldCreateWithPort() {
            // Given
            String urlWithPort = "https://example.com:8443/secure";

            // When
            RequestUrl requestUrl = RequestUrl.of(urlWithPort);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(urlWithPort);
        }
    }

    @Nested
    @DisplayName("예외 케이스 테스트")
    class ExceptionTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("URL이 null 또는 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenUrlIsNullOrBlank(String invalidUrl) {
            // When & Then
            assertThatThrownBy(() -> RequestUrl.of(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not-a-url",
            "htp://wrong-protocol.com",
            "://missing-protocol.com",
            "example.com",           // 프로토콜 없음
            "www.example.com",       // 프로토콜 없음
            "http://invalid space.com",
            "http://[invalid bracket"
        })
        @DisplayName("잘못된 URL 형식이면 예외 발생")
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
        @DisplayName("같은 URL을 가진 두 RequestUrl은 isSameAs() 가 true 반환")
        void shouldReturnTrueForSameUrl() {
            // Given
            String url = "https://example.com/path";
            RequestUrl url1 = RequestUrl.of(url);
            RequestUrl url2 = RequestUrl.of(url);

            // When
            boolean result = url1.isSameAs(url2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 URL을 가진 두 RequestUrl은 isSameAs() 가 false 반환")
        void shouldReturnFalseForDifferentUrl() {
            // Given
            RequestUrl url1 = RequestUrl.of("https://example.com/path1");
            RequestUrl url2 = RequestUrl.of("https://example.com/path2");

            // When
            boolean result = url1.isSameAs(url2);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("null과 비교하면 isSameAs() 가 false 반환")
        void shouldReturnFalseWhenComparedWithNull() {
            // Given
            RequestUrl requestUrl = RequestUrl.of("https://example.com");

            // When
            boolean result = requestUrl.isSameAs(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("같은 URL을 가진 두 RequestUrl은 equals() 가 true 반환")
        void shouldReturnTrueForEquals() {
            // Given
            String url = "https://example.com/path";
            RequestUrl url1 = RequestUrl.of(url);
            RequestUrl url2 = RequestUrl.of(url);

            // When & Then
            assertThat(url1).isEqualTo(url2);
        }

        @Test
        @DisplayName("같은 URL을 가진 두 RequestUrl은 같은 hashCode 반환")
        void shouldReturnSameHashCode() {
            // Given
            String url = "https://example.com/path";
            RequestUrl url1 = RequestUrl.of(url);
            RequestUrl url2 = RequestUrl.of(url);

            // When & Then
            assertThat(url1.hashCode()).isEqualTo(url2.hashCode());
        }

        @Test
        @DisplayName("다른 URL을 가진 두 RequestUrl은 다른 hashCode 반환")
        void shouldReturnDifferentHashCode() {
            // Given
            RequestUrl url1 = RequestUrl.of("https://example.com/path1");
            RequestUrl url2 = RequestUrl.of("https://example.com/path2");

            // When & Then
            assertThat(url1.hashCode()).isNotEqualTo(url2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 URL을 포함한 문자열 반환")
        void shouldReturnStringWithUrl() {
            // Given
            String url = "https://example.com/path";
            RequestUrl requestUrl = RequestUrl.of(url);

            // When
            String result = requestUrl.toString();

            // Then
            assertThat(result).contains(url);
        }
    }

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("매우 긴 URL도 정상 생성")
        void shouldCreateVeryLongUrl() {
            // Given
            String longUrl = "https://example.com/very/long/path/" + "segment/".repeat(100) + "end";

            // When
            RequestUrl requestUrl = RequestUrl.of(longUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(longUrl);
        }

        @Test
        @DisplayName("IPv4 주소 URL도 정상 생성")
        void shouldCreateWithIpv4Address() {
            // Given
            String ipUrl = "http://192.168.1.1:8080/api";

            // When
            RequestUrl requestUrl = RequestUrl.of(ipUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(ipUrl);
        }

        @Test
        @DisplayName("localhost URL도 정상 생성")
        void shouldCreateWithLocalhost() {
            // Given
            String localhostUrl = "http://localhost:3000/api/v1/data";

            // When
            RequestUrl requestUrl = RequestUrl.of(localhostUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(localhostUrl);
        }

        @Test
        @DisplayName("Fragment가 있는 URL도 정상 생성")
        void shouldCreateWithFragment() {
            // Given
            String urlWithFragment = "https://example.com/page#section-1";

            // When
            RequestUrl requestUrl = RequestUrl.of(urlWithFragment);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(urlWithFragment);
        }

        @Test
        @DisplayName("복잡한 쿼리 파라미터가 있는 URL도 정상 생성")
        void shouldCreateWithComplexQueryParameters() {
            // Given
            String complexUrl = "https://api.example.com/search?q=test+query&filter=type:article,date:2024&sort=relevance&page=1&size=20";

            // When
            RequestUrl requestUrl = RequestUrl.of(complexUrl);

            // Then
            assertThat(requestUrl).isNotNull();
            assertThat(requestUrl.getValue()).isEqualTo(complexUrl);
        }
    }
}
