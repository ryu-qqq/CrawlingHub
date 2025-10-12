package com.ryuqq.crawlinghub.adapter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

/**
 * AWS EventBridge Configuration.
 * Provides EventBridge client bean with credential provider chain
 *
 * @author crawlinghub (noreply@crawlinghub.com)
 */
@Configuration
@EnableConfigurationProperties(EventBridgeProperties.class)
public class AwsEventBridgeConfig {

    private final EventBridgeProperties properties;

    public AwsEventBridgeConfig(EventBridgeProperties properties) {
        this.properties = properties;
    }

    /**
     * AWS credentials provider bean
     * Uses explicit credentials if configured, otherwise falls back to default credential provider chain
     */
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (properties.getCredentials().hasExplicitCredentials()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            properties.getCredentials().getAccessKey(),
                            properties.getCredentials().getSecretKey()
                    )
            );
        }
        // Falls back to default AWS credential provider chain:
        // 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
        // 2. System properties (aws.accessKeyId, aws.secretKey)
        // 3. Web identity token from AWS STS
        // 4. Shared credentials file (~/.aws/credentials)
        // 5. EC2 instance profile credentials
        return DefaultCredentialsProvider.create();
    }

    /**
     * EventBridge client bean
     */
    @Bean
    public EventBridgeClient eventBridgeClient(AwsCredentialsProvider credentialsProvider) {
        return EventBridgeClient.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
