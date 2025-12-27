package com.ryuqq.crawlinghub.adapter.out.fileflow.config;

import com.ryuqq.fileflow.sdk.client.FileFlowClient;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fileflow Client 설정
 *
 * <p>FileFlow SDK를 사용한 클라이언트 Bean을 제공합니다.
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
     * FileFlow SDK 동기 클라이언트 Bean 생성
     *
     * <p>Service Token을 사용하여 서버 간 인증을 수행합니다.
     *
     * @return FileFlowClient instance
     */
    @Bean
    public FileFlowClient fileFlowClient() {
        return FileFlowClient.builder()
                .baseUrl(properties.getBaseUrl())
                .serviceToken(properties.getServiceToken())
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeout()))
                .readTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .build();
    }
}
