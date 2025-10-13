package com.ryuqq.crawlinghub.adapter.mustit;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * 머스트잇 API HTTP 클라이언트 설정
 *
 * @author CrawlingHub Team (crawlinghub@ryuqq.com)
 */
@Configuration
public class MustitApiConfig {

    private final String baseUrl;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;

    /**
     * 머스트잇 API 설정 생성자
     *
     * @param baseUrl API 기본 URL
     * @param connectTimeout 연결 타임아웃 (ms)
     * @param readTimeout 읽기 타임아웃 (ms)
     * @param writeTimeout 쓰기 타임아웃 (ms)
     */
    public MustitApiConfig(
            @Value("${mustit.api.base-url:https://api.mustit.co.kr}") String baseUrl,
            @Value("${mustit.api.connect-timeout:5000}") int connectTimeout,
            @Value("${mustit.api.read-timeout:10000}") int readTimeout,
            @Value("${mustit.api.write-timeout:10000}") int writeTimeout
    ) {
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
    }

    /**
     * 머스트잇 API WebClient 빈 생성
     */
    @Bean
    public WebClient mustitWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }
}
