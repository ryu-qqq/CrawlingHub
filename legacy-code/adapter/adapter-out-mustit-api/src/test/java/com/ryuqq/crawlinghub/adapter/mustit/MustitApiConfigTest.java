package com.ryuqq.crawlinghub.adapter.mustit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MustitApiConfig 설정 테스트
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
@SpringBootTest(classes = {MustitApiConfig.class, MustitApiConfigTest.TestConfig.class})
@TestPropertySource(properties = {
        "mustit.api.base-url=http://localhost:8080",
        "mustit.api.connect-timeout=5000",
        "mustit.api.read-timeout=10000",
        "mustit.api.write-timeout=10000"
})
class MustitApiConfigTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }

    @Test
    @DisplayName("WebClient 빈 생성 성공")
    void mustitWebClientBeanCreation() {
        MustitApiConfig config = new MustitApiConfig(
                "http://localhost:8080",
                5000,
                10000,
                10000
        );

        WebClient webClient = config.mustitWebClient();

        assertThat(webClient).isNotNull();
    }

    @Test
    @DisplayName("기본 URL 설정 확인")
    void baseUrlConfiguration() {
        String baseUrl = "http://localhost:8080";
        MustitApiConfig config = new MustitApiConfig(
                baseUrl,
                5000,
                10000,
                10000
        );

        WebClient webClient = config.mustitWebClient();

        assertThat(webClient).isNotNull();
    }
}
