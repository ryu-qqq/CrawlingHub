package com.ryuqq.crawlinghub.adapter.out.sqs.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * SqsConfig 단위 테스트
 *
 * <p>SqsClient 빈 생성 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("SqsConfig 단위 테스트")
class SqsConfigTest {

    private final SqsConfig config = new SqsConfig();

    private SqsClientProperties createProperties(String endpoint) {
        SqsClientProperties properties = new SqsClientProperties();
        properties.setRegion("ap-northeast-2");
        properties.setEndpoint(endpoint);
        SqsClientProperties.Queues queues = new SqsClientProperties.Queues();
        queues.setCrawlTask("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
        queues.setProductSync("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
        properties.setQueues(queues);
        return properties;
    }

    @Nested
    @DisplayName("sqsClient 빈 생성 테스트")
    class SqsClientBeanTest {

        @Test
        @DisplayName("endpoint가 null이면 기본 SqsClient를 생성한다")
        void sqsClient_withNullEndpoint_createsDefaultClient() {
            // given
            SqsClientProperties properties = createProperties(null);

            // when
            SqsClient client = config.sqsClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("endpoint가 빈 문자열이면 기본 SqsClient를 생성한다")
        void sqsClient_withBlankEndpoint_createsDefaultClient() {
            // given
            SqsClientProperties properties = createProperties("  ");

            // when
            SqsClient client = config.sqsClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }

        @Test
        @DisplayName("endpoint가 설정되면 endpointOverride가 적용된 SqsClient를 생성한다")
        void sqsClient_withEndpoint_createsClientWithEndpointOverride() {
            // given
            SqsClientProperties properties = createProperties("http://localhost:4566");

            // when
            SqsClient client = config.sqsClient(properties);

            // then
            assertThat(client).isNotNull();
            client.close();
        }
    }
}
