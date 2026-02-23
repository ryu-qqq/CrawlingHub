package com.ryuqq.crawlinghub.adapter.out.sqs.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS SQS Client Configuration.
 *
 * <p>SqsClient 빈을 생성합니다.
 *
 * <p>sqs.queues.crawl-task 설정이 있을 때만 활성화됩니다.
 */
@Configuration
@EnableConfigurationProperties(SqsClientProperties.class)
@ConditionalOnProperty(prefix = "sqs.queues", name = "crawl-task")
public class SqsConfig {

    @Bean
    public SqsClient sqsClient(SqsClientProperties properties) {
        var builder = SqsClient.builder().region(Region.of(properties.getRegion()));

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }
}
