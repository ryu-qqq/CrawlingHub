package com.ryuqq.crawlinghub.adapter.out.marketplace.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

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

        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                properties.connectTimeout() * 1000)
                        .responseTimeout(Duration.ofSeconds(properties.requestTimeout()));

        return builder.baseUrl(properties.baseUrl())
                .defaultHeader("X-Service-Token", properties.serviceToken())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
