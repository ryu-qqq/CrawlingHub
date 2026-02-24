package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * EventBridgeClientProperties 단위 테스트
 *
 * <p>프로퍼티 기본값 및 setter/getter를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("EventBridgeClientProperties 단위 테스트")
class EventBridgeClientPropertiesTest {

    @Nested
    @DisplayName("기본값 검증 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("region 기본값은 ap-northeast-2다")
        void defaultRegion_isApNortheast2() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            assertThat(properties.getRegion()).isEqualTo("ap-northeast-2");
        }

        @Test
        @DisplayName("scheduleGroupName 기본값은 crawlinghub-schedules다")
        void defaultScheduleGroupName_isCrawlinghubSchedules() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            assertThat(properties.getScheduleGroupName()).isEqualTo("crawlinghub-schedules");
        }

        @Test
        @DisplayName("scheduleNamePrefix 기본값은 crawler-다")
        void defaultScheduleNamePrefix_isCrawlerDash() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            assertThat(properties.getScheduleNamePrefix()).isEqualTo("crawler-");
        }

        @Test
        @DisplayName("endpoint 기본값은 null이다")
        void defaultEndpoint_isNull() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            assertThat(properties.getEndpoint()).isNull();
        }
    }

    @Nested
    @DisplayName("setter/getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("endpoint를 설정하면 getEndpoint로 조회된다")
        void setEndpoint_thenGetEndpointReturnsSetValue() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            properties.setEndpoint("http://localhost:4566");
            assertThat(properties.getEndpoint()).isEqualTo("http://localhost:4566");
        }

        @Test
        @DisplayName("targetArn을 설정하면 getTargetArn으로 조회된다")
        void setTargetArn_thenGetTargetArnReturnsSetValue() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            properties.setTargetArn("arn:aws:lambda:ap-northeast-2:123:function:test");
            assertThat(properties.getTargetArn())
                    .isEqualTo("arn:aws:lambda:ap-northeast-2:123:function:test");
        }

        @Test
        @DisplayName("roleArn을 설정하면 getRoleArn으로 조회된다")
        void setRoleArn_thenGetRoleArnReturnsSetValue() {
            EventBridgeClientProperties properties = new EventBridgeClientProperties();
            properties.setRoleArn("arn:aws:iam::123:role/test-role");
            assertThat(properties.getRoleArn()).isEqualTo("arn:aws:iam::123:role/test-role");
        }
    }
}
