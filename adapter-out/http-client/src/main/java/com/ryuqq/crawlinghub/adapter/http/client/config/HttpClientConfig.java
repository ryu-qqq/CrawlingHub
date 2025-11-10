package com.ryuqq.crawlinghub.adapter.http.client.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP Client Configuration
 * <p>
 * RestTemplate Bean 설정 및 타임아웃 관리
 * </p>
 * <p>
 * ⚠️ Zero-Tolerance 규칙:
 * - 외부 API 호출은 반드시 타임아웃 설정
 * - Connection Pool 관리 (기본값 사용)
 * </p>
 * <p>
 * 타임아웃 정책:
 * - Connect Timeout: 3초 (연결 수립)
 * - Read Timeout: 10초 (응답 대기)
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Configuration
public class HttpClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    /**
     * RestTemplate Bean 생성
     * <p>
     * ⭐ 타임아웃 설정:
     * - Connect Timeout: 3초 (외부 서버 연결)
     * - Read Timeout: 10초 (응답 대기)
     * </p>
     * <p>
     * ⚠️ 주의사항:
     * - 이 Bean은 @Transactional 밖에서만 사용
     * - MustitTokenAdapter, 기타 HTTP Client Adapter에서 주입
     * </p>
     *
     * @param builder RestTemplateBuilder (Spring Boot 자동 제공)
     * @return 설정된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(CONNECT_TIMEOUT)
            .setReadTimeout(READ_TIMEOUT)
            .build();
    }
}
