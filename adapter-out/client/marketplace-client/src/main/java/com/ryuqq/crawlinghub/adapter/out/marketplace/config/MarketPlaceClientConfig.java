package com.ryuqq.crawlinghub.adapter.out.marketplace.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 외부몰 클라이언트 WebClient 설정
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MarketPlaceClientProperties.class)
public class MarketPlaceClientConfig {

    @Bean
    public WebClient marketPlaceWebClient(
            WebClient.Builder builder, MarketPlaceClientProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
