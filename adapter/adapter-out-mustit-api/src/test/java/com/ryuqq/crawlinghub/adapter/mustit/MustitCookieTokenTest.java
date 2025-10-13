package com.ryuqq.crawlinghub.adapter.mustit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mustit 웹사이트 쿠키 기반 토큰 발급 테스트
 * 실제 Mustit 웹사이트에 접속하여 쿠키로 발급되는 토큰을 확인
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 * @since 2025-10-14
 */
class MustitCookieTokenTest {

    private static final Logger LOG = LoggerFactory.getLogger(MustitCookieTokenTest.class);
    private static final String MUSTIT_URL = "https://m.web.mustit.co.kr/";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Test
    @DisplayName("Mustit 웹사이트 접속 시 쿠키에서 토큰 추출 성공")
    void shouldExtractTokenFromCookies() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        WebClient webClient = WebClient.builder()
                .baseUrl(MUSTIT_URL)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .defaultHeader("Accept-Language", "ko-KR,ko;q=0.9")
                .build();

        // when
        LOG.info("Requesting Mustit website with User-Agent: {}", userAgent);
        
        ClientResponse response = webClient.get()
                .uri("/")
                .exchange()
                .timeout(TIMEOUT)
                .block();

        // then
        assertThat(response).isNotNull();
        LOG.info("Response Status: {}", response.statusCode());

        // Extract cookies
        List<ResponseCookie> cookies = response.cookies().values().stream()
                .flatMap(List::stream)
                .toList();

        LOG.info("Total cookies received: {}", cookies.size());
        cookies.forEach(cookie -> {
            LOG.info("Cookie: name={}, value={}, domain={}, path={}, maxAge={}, secure={}, httpOnly={}",
                    cookie.getName(),
                    cookie.getValue().length() > 20 ? cookie.getValue().substring(0, 20) + "..." : cookie.getValue(),
                    cookie.getDomain(),
                    cookie.getPath(),
                    cookie.getMaxAge(),
                    cookie.isSecure(),
                    cookie.isHttpOnly()
            );
        });

        // Check for token-related cookies
        List<ResponseCookie> tokenCookies = cookies.stream()
                .filter(cookie -> {
                    String name = cookie.getName().toLowerCase();
                    return name.contains("token") || 
                           name.contains("auth") || 
                           name.contains("session") ||
                           name.contains("access") ||
                           name.equals("jwt");
                })
                .toList();

        LOG.info("Token-related cookies found: {}", tokenCookies.size());
        tokenCookies.forEach(cookie -> {
            LOG.info("Token Cookie: name={}, value length={}", 
                    cookie.getName(), 
                    cookie.getValue().length());
        });

        // Verify response
        assertThat(response.statusCode().is2xxSuccessful()).isTrue();
        
        // Log Set-Cookie headers
        List<String> setCookieHeaders = response.headers().header(HttpHeaders.SET_COOKIE);
        LOG.info("Set-Cookie headers: {}", setCookieHeaders.size());
        setCookieHeaders.forEach(header -> {
            LOG.info("Set-Cookie: {}", header.length() > 100 ? header.substring(0, 100) + "..." : header);
        });
    }

    @Test
    @DisplayName("다양한 User-Agent로 접속하여 쿠키 비교")
    void shouldCompareTokensFromDifferentUserAgents() {
        // given
        String[] userAgents = {
                // iOS Safari
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                        "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
                // Android Chrome
                "Mozilla/5.0 (Linux; Android 13; SM-S908B) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36",
                // Desktop Chrome
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"
        };

        WebClient webClient = WebClient.builder()
                .baseUrl(MUSTIT_URL)
                .build();

        // when & then
        for (int i = 0; i < userAgents.length; i++) {
            String ua = userAgents[i];
            LOG.info("\n========== Test #{} ==========", i + 1);
            LOG.info("User-Agent: {}", ua);

            ClientResponse response = webClient.get()
                    .uri("/")
                    .header("User-Agent", ua)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .exchange()
                    .timeout(TIMEOUT)
                    .block();

            assertThat(response).isNotNull();
            LOG.info("Status: {}", response.statusCode());

            List<String> cookieNames = response.cookies().keySet().stream().toList();
            LOG.info("Cookies: {}", cookieNames);
        }
    }

    @Test
    @DisplayName("쿠키 값 파싱 및 JWT 토큰 형식 검증")
    void shouldValidateJwtTokenFormat() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        WebClient webClient = WebClient.builder()
                .baseUrl(MUSTIT_URL)
                .defaultHeader("User-Agent", userAgent)
                .build();

        // when
        ClientResponse response = webClient.get()
                .uri("/")
                .exchange()
                .timeout(TIMEOUT)
                .block();

        // then
        assertThat(response).isNotNull();

        // Find JWT-like tokens (format: xxx.yyy.zzz)
        response.cookies().forEach((name, cookieList) -> {
            cookieList.forEach(cookie -> {
                String value = cookie.getValue();
                if (value != null && value.split("\\.").length == 3) {
                    LOG.info("Potential JWT token found in cookie '{}': {}", 
                            name, 
                            value.substring(0, Math.min(50, value.length())) + "...");
                    
                    // JWT 토큰 형식 검증 (Base64 URL-safe)
                    String[] parts = value.split("\\.");
                    assertThat(parts).hasSize(3);
                    assertThat(parts[0]).matches("[A-Za-z0-9_-]+"); // Header
                    assertThat(parts[1]).matches("[A-Za-z0-9_-]+"); // Payload
                    assertThat(parts[2]).matches("[A-Za-z0-9_-]+"); // Signature
                }
            });
        });
    }

    @Test
    @DisplayName("쿠키 없이 접속한 경우와 비교")
    void shouldCompareWithAndWithoutCookies() {
        // given
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
                "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

        WebClient webClient = WebClient.builder()
                .baseUrl(MUSTIT_URL)
                .defaultHeader("User-Agent", userAgent)
                .build();

        // when - 첫 번째 요청 (쿠키 없음)
        LOG.info("========== First Request (No Cookies) ==========");
        ClientResponse firstResponse = webClient.get()
                .uri("/")
                .exchange()
                .timeout(TIMEOUT)
                .block();

        assertThat(firstResponse).isNotNull();
        List<ResponseCookie> firstCookies = firstResponse.cookies().values().stream()
                .flatMap(List::stream)
                .toList();
        
        LOG.info("Cookies from first request: {}", firstCookies.size());
        firstCookies.forEach(cookie -> 
                LOG.info("  - {}: {} bytes", cookie.getName(), cookie.getValue().length()));

        // when - 두 번째 요청 (쿠키 포함)
        LOG.info("\n========== Second Request (With Cookies) ==========");
        String cookieHeader = firstCookies.stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("");

        ClientResponse secondResponse = webClient.get()
                .uri("/")
                .header("Cookie", cookieHeader)
                .exchange()
                .timeout(TIMEOUT)
                .block();

        assertThat(secondResponse).isNotNull();
        List<ResponseCookie> secondCookies = secondResponse.cookies().values().stream()
                .flatMap(List::stream)
                .toList();

        LOG.info("Cookies from second request: {}", secondCookies.size());
        secondCookies.forEach(cookie -> 
                LOG.info("  - {}: {} bytes", cookie.getName(), cookie.getValue().length()));

        // Compare
        LOG.info("\n========== Comparison ==========");
        LOG.info("First request cookies: {}", firstCookies.size());
        LOG.info("Second request cookies: {}", secondCookies.size());
    }
}
