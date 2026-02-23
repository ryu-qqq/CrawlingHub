package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.scheduler.SchedulerClient;

/**
 * EventBridgeConfig 단위 테스트
 *
 * <p>SchedulerClient 빈 생성 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("EventBridgeConfig 단위 테스트")
class EventBridgeConfigTest {

    private final EventBridgeConfig config = new EventBridgeConfig();

    private EventBridgeClientProperties createProperties(String endpoint) {
        EventBridgeClientProperties properties = new EventBridgeClientProperties();
        properties.setRegion("ap-northeast-2");
        properties.setTargetArn("arn:aws:lambda:ap-northeast-2:123456789:function:test");
        properties.setRoleArn("arn:aws:iam::123456789:role/test-role");
        properties.setScheduleGroupName("test-group");
        properties.setScheduleNamePrefix("crawler-");
        properties.setEndpoint(endpoint);
        return properties;
    }

    @Nested
    @DisplayName("schedulerClient 빈 생성 테스트")
    class SchedulerClientBeanTest {

        @Test
        @DisplayName("endpoint가 null이면 기본 SchedulerClient를 생성한다")
        void schedulerClient_withNullEndpoint_createsDefaultClient() {
            // given
            EventBridgeClientProperties properties = createProperties(null);

            // when
            SchedulerClient client = config.schedulerClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("endpoint가 빈 문자열이면 기본 SchedulerClient를 생성한다")
        void schedulerClient_withBlankEndpoint_createsDefaultClient() {
            // given
            EventBridgeClientProperties properties = createProperties("  ");

            // when
            SchedulerClient client = config.schedulerClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("endpoint가 설정되면 endpointOverride가 적용된 SchedulerClient를 생성한다")
        void schedulerClient_withEndpoint_createsClientWithEndpointOverride() {
            // given
            EventBridgeClientProperties properties = createProperties("http://localhost:4566");

            // when
            SchedulerClient client = config.schedulerClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }
    }
}
