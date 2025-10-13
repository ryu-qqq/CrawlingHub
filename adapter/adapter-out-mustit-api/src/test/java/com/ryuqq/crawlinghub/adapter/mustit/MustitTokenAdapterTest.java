package com.ryuqq.crawlinghub.adapter.mustit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.domain.token.TokenAcquisitionException;
import com.ryuqq.crawlinghub.domain.token.TokenResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MustitTokenAdapter 단위 테스트
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
class MustitTokenAdapterTest {

    private MockWebServer mockWebServer;
    private MustitTokenAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        adapter = new MustitTokenAdapter(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("토큰 발급 성공")
    void issueTokenSuccess() throws JsonProcessingException, InterruptedException {
        // given
        String userAgent = "test-user-agent";
        TokenApiResponse apiResponse = new TokenApiResponse(
                "access-token-123",
                "refresh-token-456",
                3600L,
                "Bearer"
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(apiResponse)));

        // when
        TokenResponse response = adapter.issueToken(userAgent);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token-123");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-456");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.isExpired()).isFalse();

        // verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/auth/token");
        assertThat(request.getHeader("User-Agent")).isEqualTo(userAgent);
    }

    @Test
    @DisplayName("토큰 발급 실패 - Rate Limit 초과")
    void issueTokenRateLimitExceeded() {
        // given
        String userAgent = "test-user-agent";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("Too Many Requests"));

        // when & then
        assertThatThrownBy(() -> adapter.issueToken(userAgent))
                .isInstanceOf(TokenAcquisitionException.class)
                .hasFieldOrPropertyWithValue("reason",
                        TokenAcquisitionException.Reason.RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("토큰 발급 실패 - 유효하지 않은 User-Agent")
    void issueTokenInvalidUserAgent() {
        // given
        String userAgent = "invalid-user-agent";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // when & then
        assertThatThrownBy(() -> adapter.issueToken(userAgent))
                .isInstanceOf(TokenAcquisitionException.class)
                .hasFieldOrPropertyWithValue("reason",
                        TokenAcquisitionException.Reason.INVALID_USER_AGENT);
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshTokenSuccess() throws JsonProcessingException, InterruptedException {
        // given
        String refreshToken = "refresh-token-456";
        TokenApiResponse apiResponse = new TokenApiResponse(
                "new-access-token-789",
                "new-refresh-token-012",
                3600L,
                "Bearer"
        );

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(apiResponse)));

        // when
        TokenResponse response = adapter.refreshToken(refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token-789");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token-012");

        // verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/auth/token");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 만료된 Refresh Token")
    void refreshTokenExpired() {
        // given
        String refreshToken = "expired-refresh-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // when & then
        assertThatThrownBy(() -> adapter.refreshToken(refreshToken))
                .isInstanceOf(TokenAcquisitionException.class)
                .hasFieldOrPropertyWithValue("reason",
                        TokenAcquisitionException.Reason.TOKEN_EXPIRED);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰")
    void validateTokenValidToken() throws InterruptedException {
        // given
        String accessToken = "valid-access-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("true"));

        // when
        boolean isValid = adapter.validateToken(accessToken);

        // then
        assertThat(isValid).isTrue();

        // verify request
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v1/auth/validate");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + accessToken);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효하지 않은 토큰")
    void validateTokenInvalidToken() {
        // given
        String accessToken = "invalid-access-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("Unauthorized"));

        // when
        boolean isValid = adapter.validateToken(accessToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 네트워크 에러 시 false 반환")
    void validateTokenNetworkError() {
        // given
        String accessToken = "test-token";
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // when
        boolean isValid = adapter.validateToken(accessToken);

        // then
        assertThat(isValid).isFalse();
    }
}
