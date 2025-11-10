package com.ryuqq.crawlinghub.adapter.in.event.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

/**
 * AWS SQS FIFO Queue Configuration.
 * <p>
 * EventBridge에서 전송한 메시지를 SQS FIFO Queue로 수신합니다.
 * FIFO Queue는 메시지 순서를 보장하고 중복 처리를 방지합니다.
 * </p>
 *
 * <p>FIFO 특징:
 * <ul>
 *   <li>Message Group ID: sellerId 기반 그룹핑 (셀러별 순서 보장)</li>
 *   <li>Message Deduplication: 5분 내 동일 메시지 자동 제거</li>
 *   <li>Exactly-Once Processing: 중복 없이 정확히 한 번 처리</li>
 * </ul>
 * </p>
 *
 * @author Sang-won Ryu
 * @since 1.0
 */
@Configuration
public class SqsConfig {

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    /**
     * SQS Async Client Bean.
     * <p>
     * 비동기 메시지 수신을 위한 SQS 클라이언트입니다.
     * </p>
     *
     * @return SQS Async Client
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build();
    }

    /**
     * SQS Message Listener Container Factory.
     * <p>
     * {@code @SqsListener} 메서드가 메시지를 수신할 때 사용하는 팩토리입니다.
     * </p>
     *
     * @param sqsAsyncClient SQS Async Client
     * @return Listener Container Factory
     */
    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
        SqsAsyncClient sqsAsyncClient
    ) {
        return SqsMessageListenerContainerFactory
            .builder()
            .sqsAsyncClient(sqsAsyncClient)
            .build();
    }

    /**
     * SQS Template Bean (메시지 전송용).
     * <p>
     * 필요 시 메시지를 SQS로 전송할 때 사용합니다.
     * 현재는 수신 전용이므로 옵션입니다.
     * </p>
     *
     * @param sqsAsyncClient SQS Async Client
     * @return SQS Template
     */
    @Bean
    public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsTemplate.newTemplate(sqsAsyncClient);
    }
}
