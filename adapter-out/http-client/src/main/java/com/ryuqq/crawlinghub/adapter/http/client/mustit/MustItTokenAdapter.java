package com.ryuqq.crawlinghub.adapter.http.client.mustit;

import com.ryuqq.crawlinghub.application.token.port.MustItTokenPort;
import com.ryuqq.crawlinghub.domain.token.Token;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * MustIt Token Issuance HTTP Client Adapter
 * <p>
 * ⭐ Domain 중심 설계:
 * - Token VO 반환
 * - 외부 API 호출을 추상화
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - 외부 API 호출은 트랜잭션 밖에서 (TokenAcquisitionManager에서 호출)
 * - 이 Adapter는 @Transactional 없음
 * </p>
 * <p>
 * 역할:
 * - MustIt 웹사이트 방문하여 쿠키에서 토큰 발급
 * - HTTP 응답 쿠키 → Token VO 변환
 * </p>
 * <p>
 * 구현 방식 (PRD 11번 요구사항):
 * - URL: https://m.web.mustit.co.kr (메인 페이지)
 * - Method: GET (단순 페이지 방문)
 * - User-Agent 헤더 설정 필수
 * - 응답의 Set-Cookie 헤더에서 "token" 추출
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class MustItTokenAdapter implements MustItTokenPort {

    private static final String MUSTIT_WEB_URL = "https://m.web.mustit.co.kr";
    private static final String COOKIE_TOKEN_PREFIX = "token=";

    private final RestTemplate restTemplate;

    /**
     * Adapter 생성자
     *
     * @param restTemplate Spring RestTemplate
     */
    public MustItTokenAdapter(RestTemplate restTemplate) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
    }

    /**
     * MustIt 웹사이트 방문하여 쿠키에서 토큰 발급
     * <p>
     * ⚠️ 트랜잭션 밖에서 호출 (TokenAcquisitionManager에서 호출)
     * </p>
     * <p>
     * PRD 11번 요구사항:
     * "머스트잇 사이트는 https://m.web.mustit.co.kr 사이트를
     * 유저 에이전트를 붙여 호출해야지만 쿠키에 token 키값을 받을 수 있다."
     * </p>
     *
     * @param userAgentString User-Agent 문자열
     * @return Token VO
     * @throws RuntimeException 토큰 발급 실패 시
     */
    @Override
    public Token issueToken(String userAgentString) {
        Objects.requireNonNull(userAgentString, "userAgentString must not be null");

        try {
            // HTTP 요청 헤더 설정 (User-Agent 필수)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", userAgentString);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            // MustIt 웹사이트 방문 (GET 요청)
            ResponseEntity<String> response = restTemplate.exchange(
                MUSTIT_WEB_URL,
                HttpMethod.GET,
                request,
                String.class
            );

            // 응답 검증
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to visit MustIt website: " + response.getStatusCode());
            }

            // Set-Cookie 헤더에서 token 추출
            String tokenValue = extractTokenFromCookies(response.getHeaders());

            if (tokenValue == null || tokenValue.isEmpty()) {
                throw new RuntimeException("Token not found in Set-Cookie header");
            }

            // ⭐ Token VO 생성 (Domain 타입 반환)
            LocalDateTime now = LocalDateTime.now();
            return Token.of(
                tokenValue,
                now,
                now.plusHours(24)  // 24시간 유효 (PRD 요구사항)
            );

        } catch (Exception e) {
            throw new RuntimeException("MustIt token issuance failed: " + e.getMessage(), e);
        }
    }

    /**
     * Set-Cookie 헤더에서 token 값 추출
     * <p>
     * Set-Cookie 헤더 예시:
     * "token=abc123; Path=/; HttpOnly"
     * </p>
     *
     * @param headers HTTP 응답 헤더
     * @return 추출된 token 값 (없으면 null)
     */
    private String extractTokenFromCookies(HttpHeaders headers) {
        List<String> setCookieHeaders = headers.get(HttpHeaders.SET_COOKIE);

        if (setCookieHeaders == null || setCookieHeaders.isEmpty()) {
            return null;
        }

        // Set-Cookie 헤더 리스트를 순회하며 "token=" 찾기
        for (String cookie : setCookieHeaders) {
            if (cookie.startsWith(COOKIE_TOKEN_PREFIX)) {
                // "token=abc123; Path=/; HttpOnly" → "abc123"
                String tokenPart = cookie.substring(COOKIE_TOKEN_PREFIX.length());
                int semicolonIndex = tokenPart.indexOf(';');

                if (semicolonIndex > 0) {
                    return tokenPart.substring(0, semicolonIndex).trim();
                } else {
                    return tokenPart.trim();
                }
            }
        }

        return null;
    }
}
