package com.ryuqq.crawlinghub.adapter.mustit;

import com.ryuqq.crawlinghub.application.token.port.MustitTokenPort;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * 머스트잇 웹사이트 쿠키 기반 토큰 관리 어댑터
 * 
 * 머스트잇은 웹사이트 접속 시 쿠키로 토큰을 발급합니다:
 * - GET https://m.web.mustit.co.kr/
 * - 응답 쿠키: token, token_type, access_type
 * - refresh token 없음 (재발급 필요 시 새로 접속)
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 * @since 2025-10-14
 */
@Component
public class MustitTokenAdapter implements MustitTokenPort {

    private static final Logger LOG = LoggerFactory.getLogger(MustitTokenAdapter.class);

    private static final String MUSTIT_WEB_URL = "https://m.web.mustit.co.kr";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String COOKIE_TOKEN = "token";
    private static final String COOKIE_TOKEN_TYPE = "token_type";
    private static final String COOKIE_ACCESS_TYPE = "access_type";
    private static final long DEFAULT_COOKIE_MAX_AGE_SECONDS = 1800; // Default 30 minutes

    private final WebClient webClient;

    /**
     * MustitTokenAdapter 생성자
     *
     * @param mustitWebClient 머스트잇 API WebClient
     */
    public MustitTokenAdapter(WebClient mustitWebClient) {
        this.webClient = mustitWebClient;
    }

    /**
     * 토큰 발급 (웹사이트 접속으로 쿠키 획득)
     * 최대 3회 재시도 (2초, 4초 지수 백오프)
     */
    @Override
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2.0),
            retryFor = {WebClientResponseException.class}
    )
    public TokenResponse issueToken(String userAgent) {
        LOG.info("Issuing token by visiting Mustit website with User-Agent: {}", userAgent);

        try {
            ClientResponse response = WebClient.builder()
                    .baseUrl(MUSTIT_WEB_URL)
                    .build()
                    .get()
                    .uri("/")
                    .header(HttpHeaders.USER_AGENT, userAgent)
                    .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9")
                    .exchange()
                    .timeout(TIMEOUT)
                    .block();

            if (response == null) {
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.NETWORK_ERROR
                );
            }

            HttpStatus status = (HttpStatus) response.statusCode();
            if (!status.is2xxSuccessful()) {
                LOG.error("Failed to access Mustit website, Status: {}", status);
                throw new TokenAcquisitionException(
                        status == HttpStatus.TOO_MANY_REQUESTS 
                            ? TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED
                            : TokenAcquisitionException.Reason.API_ERROR
                );
            }

            // Extract cookies
            String token = extractCookie(response, COOKIE_TOKEN);
            String tokenType = extractCookie(response, COOKIE_TOKEN_TYPE);
            String accessType = extractCookie(response, COOKIE_ACCESS_TYPE);

            if (token == null || token.isBlank()) {
                LOG.error("Token cookie not found in response");
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.TOKEN_EXPIRED
                );
            }

            // Extract Max-Age from Set-Cookie header
            long expiresIn = extractMaxAge(response, COOKIE_TOKEN);

            LOG.info("Token issued successfully - tokenType: {}, accessType: {}, expiresIn: {}s", 
                    tokenType, accessType, expiresIn);

            return TokenResponse.withoutRefresh(
                    token,
                    expiresIn,
                    tokenType != null ? tokenType : "Bearer"
            );

        } catch (TokenAcquisitionException e) {
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof java.util.concurrent.TimeoutException) {
                LOG.error("Timeout while accessing Mustit website with User-Agent: {}", userAgent, e);
                throw new TokenAcquisitionException(
                        TokenAcquisitionException.Reason.NETWORK_ERROR,
                        e
                );
            }
            LOG.error("Unexpected error while issuing token for User-Agent: {}", userAgent, e);
            throw new TokenAcquisitionException(
                    TokenAcquisitionException.Reason.NETWORK_ERROR,
                    e
            );
        }
    }

    /**
     * 쿠키 추출
     */
    private String extractCookie(ClientResponse response, String cookieName) {
        List<ResponseCookie> cookies = response.cookies().get(cookieName);
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.getFirst().getValue();
        }
        return null;
    }

    /**
     * Set-Cookie 헤더에서 Max-Age 추출
     */
    private long extractMaxAge(ClientResponse response, String cookieName) {
        List<String> setCookieHeaders = response.headers().header(HttpHeaders.SET_COOKIE);
        for (String header : setCookieHeaders) {
            if (header.startsWith(cookieName + "=")) {
                // Max-Age=1800 형식 파싱
                String[] parts = header.split(";");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.startsWith("Max-Age=")) {
                        try {
                            return Long.parseLong(trimmed.substring(8));
                        } catch (NumberFormatException e) {
                            LOG.warn("Failed to parse Max-Age: {}", trimmed);
                        }
                    }
                }
            }
        }
        return DEFAULT_COOKIE_MAX_AGE_SECONDS;
    }

    /**
     * 토큰 갱신 (Mustit은 refresh token 미지원)
     * 
     * Mustit은 refresh token을 제공하지 않으므로,
     * 토큰 만료 시 issueToken()으로 새로 발급받아야 합니다.
     * 
     * @throws UnsupportedOperationException refresh token 미지원
     */
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        LOG.warn("Mustit does not support refresh token - use issueToken() instead");
        throw new UnsupportedOperationException(
                "Mustit does not provide refresh token. " +
                "Please use issueToken() to obtain a new token."
        );
    }

    /**
     * JWT 형식 검증 (구조적 검증만 수행)
     *
     * 주의: 이 메서드는 토큰의 서명, 만료시간, 발급자 등을 검증하지 않습니다.
     * JWT의 구조(header.payload.signature)와 Base64 URL-safe 인코딩만 확인합니다.
     * 실제 토큰의 유효성은 Mustit API 호출 시 검증되며,
     * 만료 여부는 데이터베이스에 저장된 issuedAt과 expiresIn을 통해 확인해야 합니다.
     */
    @Override
    public boolean isJwtFormat(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            LOG.debug("Token validation failed: token is null or empty");
            return false;
        }

        // JWT 형식 검증 (header.payload.signature)
        String[] parts = accessToken.split("\\.");
        if (parts.length != 3) {
            LOG.debug("Token validation failed: invalid JWT format");
            return false;
        }

        // 각 부분이 Base64 URL-safe 형식인지 확인
        for (String part : parts) {
            if (!part.matches("[A-Za-z0-9_-]+")) {
                LOG.debug("Token validation failed: invalid Base64 encoding");
                return false;
            }
        }

        LOG.debug("Token validation passed (structural check)");
        return true;
    }
}
