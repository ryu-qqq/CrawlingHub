package com.ryuqq.crawlinghub.adapter.in.sqs.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * SqsListenerProperties 단위 테스트
 *
 * <p>Properties 클래스의 getter/setter 및 기본값을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("SqsListenerProperties 단위 테스트")
class SqsListenerPropertiesTest {

    private SqsListenerProperties sut;

    @BeforeEach
    void setUp() {
        sut = new SqsListenerProperties();
    }

    @Nested
    @DisplayName("기본값 테스트")
    class DefaultValueTest {

        @Test
        @DisplayName("[기본값] 리스너 활성화 여부의 기본값은 true이다")
        void shouldHaveTrueAsDefaultForListenerEnabled() {
            // When & Then
            assertThat(sut.isCrawlTaskListenerEnabled()).isTrue();
            assertThat(sut.isEventBridgeTriggerListenerEnabled()).isTrue();
            assertThat(sut.isProductSyncListenerEnabled()).isTrue();
            assertThat(sut.isCrawlTaskDlqListenerEnabled()).isTrue();
        }

        @Test
        @DisplayName("[기본값] URL 필드는 초기에 null이다")
        void shouldHaveNullAsDefaultForUrls() {
            // When & Then
            assertThat(sut.getCrawlTaskQueueUrl()).isNull();
            assertThat(sut.getEventBridgeTriggerQueueUrl()).isNull();
            assertThat(sut.getProductSyncQueueUrl()).isNull();
            assertThat(sut.getCrawlTaskDlqUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Setter/Getter 테스트")
    class SetterGetterTest {

        @Test
        @DisplayName("[성공] CrawlTask 큐 URL을 설정하고 조회한다")
        void shouldSetAndGetCrawlTaskQueueUrl() {
            // Given
            String expected = "https://sqs.ap-northeast-2.amazonaws.com/123456789/crawl-task-queue";

            // When
            sut.setCrawlTaskQueueUrl(expected);

            // Then
            assertThat(sut.getCrawlTaskQueueUrl()).isEqualTo(expected);
        }

        @Test
        @DisplayName("[성공] EventBridge 트리거 큐 URL을 설정하고 조회한다")
        void shouldSetAndGetEventBridgeTriggerQueueUrl() {
            // Given
            String expected =
                    "https://sqs.ap-northeast-2.amazonaws.com/123456789/eventbridge-trigger-queue";

            // When
            sut.setEventBridgeTriggerQueueUrl(expected);

            // Then
            assertThat(sut.getEventBridgeTriggerQueueUrl()).isEqualTo(expected);
        }

        @Test
        @DisplayName("[성공] ProductSync 큐 URL을 설정하고 조회한다")
        void shouldSetAndGetProductSyncQueueUrl() {
            // Given
            String expected =
                    "https://sqs.ap-northeast-2.amazonaws.com/123456789/product-sync-queue";

            // When
            sut.setProductSyncQueueUrl(expected);

            // Then
            assertThat(sut.getProductSyncQueueUrl()).isEqualTo(expected);
        }

        @Test
        @DisplayName("[성공] CrawlTask DLQ URL을 설정하고 조회한다")
        void shouldSetAndGetCrawlTaskDlqUrl() {
            // Given
            String expected = "https://sqs.ap-northeast-2.amazonaws.com/123456789/crawl-task-dlq";

            // When
            sut.setCrawlTaskDlqUrl(expected);

            // Then
            assertThat(sut.getCrawlTaskDlqUrl()).isEqualTo(expected);
        }

        @Test
        @DisplayName("[성공] CrawlTask 리스너를 비활성화하고 조회한다")
        void shouldSetCrawlTaskListenerDisabled() {
            // When
            sut.setCrawlTaskListenerEnabled(false);

            // Then
            assertThat(sut.isCrawlTaskListenerEnabled()).isFalse();
        }

        @Test
        @DisplayName("[성공] EventBridge 트리거 리스너를 비활성화하고 조회한다")
        void shouldSetEventBridgeTriggerListenerDisabled() {
            // When
            sut.setEventBridgeTriggerListenerEnabled(false);

            // Then
            assertThat(sut.isEventBridgeTriggerListenerEnabled()).isFalse();
        }

        @Test
        @DisplayName("[성공] ProductSync 리스너를 비활성화하고 조회한다")
        void shouldSetProductSyncListenerDisabled() {
            // When
            sut.setProductSyncListenerEnabled(false);

            // Then
            assertThat(sut.isProductSyncListenerEnabled()).isFalse();
        }

        @Test
        @DisplayName("[성공] CrawlTask DLQ 리스너를 비활성화하고 조회한다")
        void shouldSetCrawlTaskDlqListenerDisabled() {
            // When
            sut.setCrawlTaskDlqListenerEnabled(false);

            // Then
            assertThat(sut.isCrawlTaskDlqListenerEnabled()).isFalse();
        }
    }
}
