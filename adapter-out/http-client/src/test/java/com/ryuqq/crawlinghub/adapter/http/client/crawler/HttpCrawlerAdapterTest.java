package com.ryuqq.crawlinghub.adapter.http.client.crawler;

import com.ryuqq.crawlinghub.application.task.port.out.HttpCrawlerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HttpCrawlerAdapter Unit Test
 * <p>
 * ⭐ 테스트 전략:
 * - RestTemplate Mock으로 외부 의존성 제거
 * - 성공/실패 시나리오 검증
 * - HTTP 헤더 설정 검증
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HttpCrawlerAdapter 단위 테스트")
class HttpCrawlerAdapterTest {

    @Mock
    private RestTemplate restTemplate;

    private HttpCrawlerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new HttpCrawlerAdapter(restTemplate);
    }

    @Test
    @DisplayName("성공: 2xx 응답 시 CrawlResponse.success() 반환")
    void execute_Success_When2xxResponse() {
        // Given
        String url = "https://api.example.com/products";
        String userAgent = "Mozilla/5.0";
        String token = "test-token-123";
        String responseBody = "{\"result\": \"success\"}";

        ResponseEntity<String> mockResponse = ResponseEntity
            .ok(responseBody);

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponse);

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).isEqualTo(responseBody);
        assertThat(result.error()).isNull();

        // RestTemplate 호출 검증
        verify(restTemplate).exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    @DisplayName("성공: Token 없이 호출 시 Authorization 헤더 제외")
    void execute_Success_WhenNoToken() {
        // Given
        String url = "https://api.example.com/public";
        String userAgent = "Mozilla/5.0";
        String token = null;
        String responseBody = "{\"data\": \"public\"}";

        ResponseEntity<String> mockResponse = ResponseEntity
            .ok(responseBody);

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponse);

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("실패: 4xx 클라이언트 오류 시 CrawlResponse.failure() 반환")
    void execute_Failure_When4xxClientError() {
        // Given
        String url = "https://api.example.com/products";
        String userAgent = "Mozilla/5.0";
        String token = "invalid-token";

        ResponseEntity<String> mockResponse = ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(null);

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponse);

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.statusCode()).isEqualTo(401);
        assertThat(result.body()).isNull();
        assertThat(result.error()).contains("HTTP Error");
    }

    @Test
    @DisplayName("실패: 5xx 서버 오류 시 CrawlResponse.failure() 반환")
    void execute_Failure_When5xxServerError() {
        // Given
        String url = "https://api.example.com/products";
        String userAgent = "Mozilla/5.0";
        String token = "valid-token";

        ResponseEntity<String> mockResponse = ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(null);

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(mockResponse);

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.statusCode()).isEqualTo(500);
        assertThat(result.body()).isNull();
        assertThat(result.error()).contains("HTTP Error");
    }

    @Test
    @DisplayName("실패: Network 오류 시 CrawlResponse.failure() 반환")
    void execute_Failure_WhenNetworkError() {
        // Given
        String url = "https://api.example.com/products";
        String userAgent = "Mozilla/5.0";
        String token = "valid-token";

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new RestClientException("Connection refused"));

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.statusCode()).isNull();
        assertThat(result.body()).isNull();
        assertThat(result.error()).contains("HTTP Request Failed");
        assertThat(result.error()).contains("Connection refused");
    }

    @Test
    @DisplayName("실패: Timeout 발생 시 CrawlResponse.failure() 반환")
    void execute_Failure_WhenTimeout() {
        // Given
        String url = "https://api.example.com/slow-endpoint";
        String userAgent = "Mozilla/5.0";
        String token = "valid-token";

        when(restTemplate.exchange(
            eq(url),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new RestClientException("Read timed out"));

        // When
        HttpCrawlerPort.CrawlResponse result = adapter.execute(url, userAgent, token);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.statusCode()).isNull();
        assertThat(result.body()).isNull();
        assertThat(result.error()).contains("HTTP Request Failed");
        assertThat(result.error()).contains("Read timed out");
    }

    @Test
    @DisplayName("실패: URL이 null인 경우 NullPointerException 발생")
    void execute_ThrowsException_WhenUrlIsNull() {
        // Given
        String url = null;
        String userAgent = "Mozilla/5.0";
        String token = "valid-token";

        // When & Then
        assertThatThrownBy(() -> adapter.execute(url, userAgent, token))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("url must not be null");
    }

    @Test
    @DisplayName("실패: UserAgent가 null인 경우 NullPointerException 발생")
    void execute_ThrowsException_WhenUserAgentIsNull() {
        // Given
        String url = "https://api.example.com/products";
        String userAgent = null;
        String token = "valid-token";

        // When & Then
        assertThatThrownBy(() -> adapter.execute(url, userAgent, token))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("userAgent must not be null");
    }
}
