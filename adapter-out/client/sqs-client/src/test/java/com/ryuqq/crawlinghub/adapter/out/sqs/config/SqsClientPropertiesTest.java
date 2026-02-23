package com.ryuqq.crawlinghub.adapter.out.sqs.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * SqsClientProperties 단위 테스트
 *
 * <p>프로퍼티 기본값 및 setter/getter를 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SqsClientProperties 단위 테스트")
class SqsClientPropertiesTest {

    @Nested
    @DisplayName("기본값 검증 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("region 기본값은 ap-northeast-2다")
        void defaultRegion_isApNortheast2() {
            SqsClientProperties properties = new SqsClientProperties();
            assertThat(properties.getRegion()).isEqualTo("ap-northeast-2");
        }

        @Test
        @DisplayName("messageGroupIdPrefix 기본값은 crawl-task-다")
        void defaultMessageGroupIdPrefix_isCrawlTaskDash() {
            SqsClientProperties properties = new SqsClientProperties();
            assertThat(properties.getMessageGroupIdPrefix()).isEqualTo("crawl-task-");
        }

        @Test
        @DisplayName("endpoint 기본값은 null이다")
        void defaultEndpoint_isNull() {
            SqsClientProperties properties = new SqsClientProperties();
            assertThat(properties.getEndpoint()).isNull();
        }

        @Test
        @DisplayName("queues는 초기화된 상태로 생성된다")
        void defaultQueues_isNotNull() {
            SqsClientProperties properties = new SqsClientProperties();
            assertThat(properties.getQueues()).isNotNull();
        }
    }

    @Nested
    @DisplayName("setter/getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("region을 설정하면 getRegion으로 조회된다")
        void setRegion_thenGetRegionReturnsSetValue() {
            SqsClientProperties properties = new SqsClientProperties();
            properties.setRegion("us-east-1");
            assertThat(properties.getRegion()).isEqualTo("us-east-1");
        }

        @Test
        @DisplayName("endpoint를 설정하면 getEndpoint로 조회된다")
        void setEndpoint_thenGetEndpointReturnsSetValue() {
            SqsClientProperties properties = new SqsClientProperties();
            properties.setEndpoint("http://localhost:4566");
            assertThat(properties.getEndpoint()).isEqualTo("http://localhost:4566");
        }

        @Test
        @DisplayName("messageGroupIdPrefix를 설정하면 getMessageGroupIdPrefix로 조회된다")
        void setMessageGroupIdPrefix_thenGetMessageGroupIdPrefixReturnsSetValue() {
            SqsClientProperties properties = new SqsClientProperties();
            properties.setMessageGroupIdPrefix("task-");
            assertThat(properties.getMessageGroupIdPrefix()).isEqualTo("task-");
        }

        @Test
        @DisplayName("Queues.crawlTask를 설정하면 getCrawlTask로 조회된다")
        void setQueues_crawlTask_thenGetCrawlTaskReturnsSetValue() {
            SqsClientProperties.Queues queues = new SqsClientProperties.Queues();
            queues.setCrawlTask("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
            assertThat(queues.getCrawlTask())
                    .isEqualTo("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
        }

        @Test
        @DisplayName("Queues.productSync를 설정하면 getProductSync로 조회된다")
        void setQueues_productSync_thenGetProductSyncReturnsSetValue() {
            SqsClientProperties.Queues queues = new SqsClientProperties.Queues();
            queues.setProductSync("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
            assertThat(queues.getProductSync())
                    .isEqualTo("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
        }
    }
}
