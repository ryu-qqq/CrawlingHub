package com.ryuqq.crawlinghub.adapter.out.http.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient 설정
 *
 * <p>Spring WebFlux WebClient Bean 등록
 *
 * <p><strong>설정</strong>:
 *
 * <ul>
 *   <li>Connection Timeout: 설정값 (기본 10초)
 *   <li>Read/Write Timeout: 설정값 (기본 30초)
 *   <li>Redirect 자동 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class WebClientConfig {

    private final HttpClientProperties properties;

    public WebClientConfig(HttpClientProperties properties) {
        this.properties = properties;
    }

    /**
     * WebClient Bean 등록
     *
     * @return WebClient
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                properties.getConnectTimeout() * 1000)
                        .responseTimeout(Duration.ofSeconds(properties.getRequestTimeout()))
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                properties.getRequestTimeout(),
                                                                TimeUnit.SECONDS))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                properties.getRequestTimeout(),
                                                                TimeUnit.SECONDS)))
                        .followRedirect(true);

        ExchangeStrategies strategies =
                ExchangeStrategies.builder()
                        .codecs(
                                (ClientCodecConfigurer configurer) ->
                                        configurer
                                                .defaultCodecs()
                                                .maxInMemorySize(properties.getMaxInMemorySize()))
                        .build();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
