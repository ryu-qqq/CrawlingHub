package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

/**
 * AWS EventBridge Scheduler 클라이언트 설정
 *
 * <p><strong>AWS SDK v2 기반 EventBridge Scheduler 클라이언트를 생성합니다</strong>
 *
 * @author development-team
 * @since 1.0.0
 */
@Configuration
public class EventBridgeConfig {

    private final EventBridgeProperties eventBridgeProperties;

    public EventBridgeConfig(EventBridgeProperties eventBridgeProperties) {
        this.eventBridgeProperties = eventBridgeProperties;
    }

    /**
     * AWS EventBridge Scheduler 클라이언트 Bean
     *
     * <p>AWS 자격 증명은 기본 자격 증명 공급자 체인을 사용합니다:
     *
     * <ul>
     *   <li>환경 변수 (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
     *   <li>Java 시스템 속성
     *   <li>AWS 자격 증명 프로필 파일
     *   <li>EC2 인스턴스 프로필 (IAM Role)
     * </ul>
     *
     * @return SchedulerClient
     */
    @Bean
    public SchedulerClient schedulerClient() {
        return SchedulerClient.builder()
                .region(Region.of(eventBridgeProperties.getRegion()))
                .build();
    }
}
