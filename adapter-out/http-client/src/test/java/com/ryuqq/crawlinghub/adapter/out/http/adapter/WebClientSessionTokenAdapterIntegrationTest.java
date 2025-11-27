package com.ryuqq.crawlinghub.adapter.out.http.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.http.config.HttpClientProperties;
import com.ryuqq.crawlinghub.adapter.out.http.config.WebClientConfig;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientSessionTokenAdapter 통합 테스트
 *
 * <p>실제 외부 사이트에 요청하여 세션 토큰 발급을 검증합니다.
 *
 * <p>네트워크 의존성이 있어 CI에서는 제외될 수 있습니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("integration-test")
@DisplayName("WebClientSessionTokenAdapter 통합 테스트")
class WebClientSessionTokenAdapterIntegrationTest {

    private WebClientSessionTokenAdapter adapter;

    @BeforeEach
    void setUp() {
        HttpClientProperties properties = new HttpClientProperties();
        properties.setTargetUrl("https://m.web.mustit.co.kr/");
        properties.setSessionCookieName("token"); // mustit uses JWT token cookie
        properties.setConnectTimeout(10);
        properties.setRequestTimeout(30);
        properties.setDefaultSessionDurationHours(2);

        WebClientConfig config = new WebClientConfig(properties);
        WebClient webClient = config.webClient();

        adapter = new WebClientSessionTokenAdapter(webClient, properties, Clock.systemUTC());
    }

    @Test
    @DisplayName("실제 사이트에서 세션 토큰 발급 성공")
    void issueSessionToken_withRealSite_shouldReturnToken() {
        // given
        String userAgent =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) "
                        + "AppleWebKit/605.1.15 (KHTML, like Gecko) "
                        + "Version/17.0 Mobile/15E148 Safari/604.1";

        // when
        Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

        // then
        assertThat(result).isPresent();

        SessionToken token = result.get();
        assertThat(token.token()).isNotBlank();
        assertThat(token.expiresAt()).isAfter(Instant.now());

        System.out.println("=== 세션 토큰 발급 결과 ===");
        System.out.println("Token: " + token.token());
        System.out.println("Expires At: " + token.expiresAt());
        System.out.println("Token Length: " + token.token().length());
    }

    @Test
    @DisplayName("다양한 User-Agent로 세션 토큰 발급")
    void issueSessionToken_withVariousUserAgents_shouldReturnTokens() {
        // given
        String[] userAgents = {
            // iPhone Safari
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) "
                    + "AppleWebKit/605.1.15 (KHTML, like Gecko) "
                    + "Version/17.0 Mobile/15E148 Safari/604.1",

            // Android Chrome
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Mobile Safari/537.36",

            // iPad Safari
            "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) "
                    + "AppleWebKit/605.1.15 (KHTML, like Gecko) "
                    + "Version/17.0 Mobile/15E148 Safari/604.1"
        };

        for (String userAgent : userAgents) {
            // when
            Optional<SessionToken> result = adapter.issueSessionToken(userAgent);

            // then
            assertThat(result).as("UserAgent: %s", userAgent.substring(0, 50)).isPresent();

            SessionToken token = result.get();
            assertThat(token.token()).isNotBlank();

            System.out.println("UserAgent: " + userAgent.substring(0, 50) + "...");
            System.out.println("Token: " + token.token());
            System.out.println("---");

            // 사이트 부하 방지
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
