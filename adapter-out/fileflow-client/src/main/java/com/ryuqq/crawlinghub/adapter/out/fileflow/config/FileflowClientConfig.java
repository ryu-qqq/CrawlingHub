package com.ryuqq.crawlinghub.adapter.out.fileflow.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Fileflow Client 설정
 *
 * <p>WebClient Bean 및 관련 설정을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(FileflowClientProperties.class)
public class FileflowClientConfig {

    private final FileflowClientProperties properties;

    public FileflowClientConfig(FileflowClientProperties properties) {
        this.properties = properties;
    }

    /**
     * Fileflow 전용 WebClient Bean 생성
     *
     * @return WebClient instance
     */
    @Bean
    public WebClient fileflowWebClient() {
        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                properties.getConnectTimeout())
                        .responseTimeout(Duration.ofMillis(properties.getReadTimeout()));

        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
