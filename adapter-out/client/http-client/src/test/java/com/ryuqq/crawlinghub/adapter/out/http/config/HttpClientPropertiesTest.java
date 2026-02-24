package com.ryuqq.crawlinghub.adapter.out.http.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * HttpClientProperties 단위 테스트
 *
 * <p>프로퍼티 기본값 및 setter/getter를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("HttpClientProperties 단위 테스트")
class HttpClientPropertiesTest {

    @Nested
    @DisplayName("기본값 검증 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("connectTimeout 기본값은 10초다")
        void defaultConnectTimeout_is10() {
            HttpClientProperties properties = new HttpClientProperties();
            assertThat(properties.getConnectTimeout()).isEqualTo(10);
        }

        @Test
        @DisplayName("requestTimeout 기본값은 30초다")
        void defaultRequestTimeout_is30() {
            HttpClientProperties properties = new HttpClientProperties();
            assertThat(properties.getRequestTimeout()).isEqualTo(30);
        }

        @Test
        @DisplayName("maxInMemorySize 기본값은 5MB(5242880 bytes)다")
        void defaultMaxInMemorySize_is5MB() {
            HttpClientProperties properties = new HttpClientProperties();
            assertThat(properties.getMaxInMemorySize()).isEqualTo(5 * 1024 * 1024);
        }
    }

    @Nested
    @DisplayName("setter/getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("connectTimeout을 설정하면 getConnectTimeout으로 조회된다")
        void setConnectTimeout_thenGetConnectTimeoutReturnsSetValue() {
            HttpClientProperties properties = new HttpClientProperties();
            properties.setConnectTimeout(30);
            assertThat(properties.getConnectTimeout()).isEqualTo(30);
        }

        @Test
        @DisplayName("requestTimeout을 설정하면 getRequestTimeout으로 조회된다")
        void setRequestTimeout_thenGetRequestTimeoutReturnsSetValue() {
            HttpClientProperties properties = new HttpClientProperties();
            properties.setRequestTimeout(60);
            assertThat(properties.getRequestTimeout()).isEqualTo(60);
        }

        @Test
        @DisplayName("maxInMemorySize를 설정하면 getMaxInMemorySize로 조회된다")
        void setMaxInMemorySize_thenGetMaxInMemorySizeReturnsSetValue() {
            HttpClientProperties properties = new HttpClientProperties();
            properties.setMaxInMemorySize(10 * 1024 * 1024);
            assertThat(properties.getMaxInMemorySize()).isEqualTo(10 * 1024 * 1024);
        }
    }
}
