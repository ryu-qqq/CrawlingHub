package com.ryuqq.crawlinghub.adapter.http.client.crawler;

import com.ryuqq.crawlinghub.application.task.port.out.HttpCrawlerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * HTTP Crawler Adapter
 * <p>
 * ⭐ HTTP API 호출 Adapter:
 * - RestTemplate 기반 외부 API 호출
 * - User-Agent 및 Token 헤더 설정
 * - 성공/실패 응답 처리
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - 이 Adapter는 @Transactional 없음 (외부 API 호출)
 * - 타임아웃 설정 필수 (HttpClientConfig에서 설정)
 * - Connection Pool 관리 (RestTemplate이 자동 처리)
 * </p>
 * <p>
 * 역할:
 * - HTTP GET 요청 실행
 * - User-Agent, Token 헤더 추가
 * - HTTP 응답 → CrawlResponse 변환
 * - 에러 처리 (4xx, 5xx, Network 오류)
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class HttpCrawlerAdapter implements HttpCrawlerPort {

    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RestTemplate restTemplate;

    /**
     * Adapter 생성자
     *
     * @param restTemplate Spring RestTemplate (HttpClientConfig에서 주입)
     */
    public HttpCrawlerAdapter(RestTemplate restTemplate) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
    }

    /**
     * HTTP API 호출 실행
     * <p>
     * ⚠️ 트랜잭션 밖에서 호출 (CrawlerFacade에서 호출)
     * </p>
     * <p>
     * 호출 흐름:
     * 1. HTTP 헤더 설정 (User-Agent, Token)
     * 2. GET 요청 실행 (RestTemplate)
     * 3. 성공 시: body 추출 → CrawlResponse.success()
     * 4. 실패 시: 에러 메시지 → CrawlResponse.failure()
     * </p>
     *
     * @param url       호출할 URL
     * @param userAgent User-Agent 문자열
     * @param token     인증 토큰 (nullable)
     * @return CrawlResponse (성공/실패 + 응답 데이터)
     */
    @Override
    public CrawlResponse execute(String url, String userAgent, String token) {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(userAgent, "userAgent must not be null");

        try {
            // 1. HTTP 헤더 설정
            HttpHeaders headers = buildHeaders(userAgent, token);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            // 2. GET 요청 실행
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class
            );

            // 3. 응답 검증 및 변환
            if (response.getStatusCode().is2xxSuccessful()) {
                return CrawlResponse.success(
                    response.getStatusCode().value(),
                    response.getBody()
                );
            } else {
                // 4xx, 5xx 응답
                return CrawlResponse.failure(
                    response.getStatusCode().value(),
                    "HTTP Error: " + response.getStatusCode()
                );
            }

        } catch (RestClientException e) {
            // Network 오류, Timeout 등 (상태 코드 없음)
            return CrawlResponse.failure("HTTP Request Failed: " + e.getMessage());
        } catch (Exception e) {
            // 기타 예외 (상태 코드 없음)
            return CrawlResponse.failure("Unexpected Error: " + e.getMessage());
        }
    }

    /**
     * HTTP 헤더 생성
     * <p>
     * 필수 헤더:
     * - User-Agent: 외부 API 요구사항
     * </p>
     * <p>
     * 선택 헤더:
     * - Authorization: Bearer {token} (token이 있는 경우)
     * </p>
     *
     * @param userAgent User-Agent 문자열
     * @param token     인증 토큰 (nullable)
     * @return HttpHeaders 인스턴스
     */
    private HttpHeaders buildHeaders(String userAgent, String token) {
        HttpHeaders headers = new HttpHeaders();

        // User-Agent 헤더 (필수)
        headers.set(USER_AGENT_HEADER, userAgent);

        // Authorization 헤더 (선택)
        if (token != null && !token.isEmpty()) {
            headers.set(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
        }

        return headers;
    }
}
