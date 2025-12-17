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

    private static final String HEADER_SERVICE_TOKEN = "X-Service-Token";
    private static final String HEADER_SERVICE_NAME = "X-Service-Name";

    /**
     * Fileflow 전용 WebClient Bean 생성
     *
     * <p>Service Token 및 Service Name 헤더를 기본으로 포함합니다. 서버 간 내부 통신 인증에 사용됩니다.
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

        WebClient.Builder builder =
                WebClient.builder()
                        .baseUrl(properties.getBaseUrl())
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .defaultHeader(HEADER_SERVICE_NAME, properties.getServiceName());

        if (properties.hasServiceToken()) {
            builder.defaultHeader(HEADER_SERVICE_TOKEN, properties.getServiceToken());
        }

        return builder.build();
    }
}
