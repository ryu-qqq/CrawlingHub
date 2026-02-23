package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

/**
 * AWS EventBridge Scheduler Client Configuration.
 *
 * <p>SchedulerClient 빈을 생성합니다.
 *
 * <p>eventbridge.target-arn 설정이 있을 때만 활성화됩니다.
 */
@Configuration
@EnableConfigurationProperties(EventBridgeClientProperties.class)
@ConditionalOnProperty(prefix = "eventbridge", name = "target-arn")
public class EventBridgeConfig {

    @Bean
    public SchedulerClient schedulerClient(EventBridgeClientProperties properties) {
        var builder = SchedulerClient.builder().region(Region.of(properties.getRegion()));

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }

        return builder.build();
    }
}
