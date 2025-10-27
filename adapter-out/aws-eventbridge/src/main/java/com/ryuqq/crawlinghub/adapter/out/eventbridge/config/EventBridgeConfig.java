package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

import java.net.URI;

/**
 * AWS EventBridge 클라이언트 설정
 * <p>
 * AWS SDK v2 EventBridge 클라이언트를 Spring Bean으로 등록합니다.
 * </p>
 * <p>
 * 환경별 설정:
 * <ul>
 *   <li>Local: LocalStack 사용 (aws.eventbridge.endpoint 설정)</li>
 *   <li>Dev/Prod: 실제 AWS EventBridge 사용</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Configuration
public class EventBridgeConfig {

    /**
     * AWS Region (기본값: ap-northeast-2)
     */
    @Value("${aws.region:ap-northeast-2}")
    private String region;

    /**
     * AWS Access Key (선택사항, LocalStack에서 사용)
     */
    @Value("${aws.accessKeyId:test}")
    private String accessKeyId;

    /**
     * AWS Secret Key (선택사항, LocalStack에서 사용)
     */
    @Value("${aws.secretAccessKey:test}")
    private String secretAccessKey;

    /**
     * EventBridge Endpoint (LocalStack용, 선택사항)
     * <p>
     * 예: http://localhost:4566 (LocalStack)
     * </p>
     */
    @Value("${aws.eventbridge.endpoint:#{null}}")
    private String eventBridgeEndpoint;

    /**
     * EventBridge 클라이언트 Bean
     * <p>
     * LocalStack 환경인 경우 endpoint를 설정하고,
     * 실제 AWS 환경인 경우 Default Credential Provider를 사용합니다.
     * </p>
     *
     * @return EventBridgeClient
     */
    @Bean
    public EventBridgeClient eventBridgeClient() {
        var builder = EventBridgeClient.builder()
                .region(Region.of(region));

        // LocalStack 환경 설정
        if (eventBridgeEndpoint != null && !eventBridgeEndpoint.isBlank()) {
            builder.endpointOverride(URI.create(eventBridgeEndpoint))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                            )
                    );
        }

        return builder.build();
    }
}
