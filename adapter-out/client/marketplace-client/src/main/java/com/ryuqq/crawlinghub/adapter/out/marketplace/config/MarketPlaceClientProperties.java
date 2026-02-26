package com.ryuqq.crawlinghub.adapter.out.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 외부몰 클라이언트 설정
 *
 * @param baseUrl 외부몰 API 기본 URL
 * @param connectTimeout 연결 타임아웃 (초)
 * @param requestTimeout 요청 타임아웃 (초)
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "marketplace.client")
public record MarketPlaceClientProperties(String baseUrl, int connectTimeout, int requestTimeout) {}
