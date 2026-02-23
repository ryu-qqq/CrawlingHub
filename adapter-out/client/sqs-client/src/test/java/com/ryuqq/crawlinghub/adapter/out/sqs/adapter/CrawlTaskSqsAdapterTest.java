package com.ryuqq.crawlinghub.adapter.out.sqs.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.crawlinghub.adapter.out.sqs.config.SqsClientProperties;
import com.ryuqq.crawlinghub.adapter.out.sqs.exception.SqsPublishException;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import com.ryuqq.crawlinghub.domain.task.vo.RetryCount;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;

/**
 * CrawlTaskSqsAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskSqsAdapter 테스트")
class CrawlTaskSqsAdapterTest {

    @Mock private SqsClient sqsClient;

    @Mock private ObjectMapper mockObjectMapper;

    private CrawlTaskSqsAdapter adapter;
    private CrawlTaskSqsAdapter adapterWithFifo;

    @BeforeEach
    void setUp() {
        // 일반 큐 어댑터
        SqsClientProperties properties =
                createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
        adapter = new CrawlTaskSqsAdapter(sqsClient, properties, new ObjectMapper());

        // FIFO 큐 어댑터
        SqsClientProperties fifoProperties =
                createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task.fifo");
        adapterWithFifo = new CrawlTaskSqsAdapter(sqsClient, fifoProperties, new ObjectMapper());
    }

    private SqsClientProperties createProperties(String queueUrl) {
        SqsClientProperties properties = new SqsClientProperties();
        properties.setRegion("ap-northeast-2");
        properties.setMessageGroupIdPrefix("crawl-task-");
        SqsClientProperties.Queues queues = new SqsClientProperties.Queues();
        queues.setCrawlTask(queueUrl);
        queues.setProductSync("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
        properties.setQueues(queues);
        return properties;
    }

    private CrawlTask createCrawlTask() {
        return CrawlTask.reconstitute(
                CrawlTaskId.of(1L),
                CrawlSchedulerId.of(10L),
                SellerId.of(100L),
                CrawlTaskType.MINI_SHOP,
                CrawlEndpoint.forMiniShopList("mustit-seller", 1, 20),
                CrawlTaskStatus.WAITING,
                RetryCount.zero(),
                null,
                Instant.now(),
                Instant.now());
    }

    private CrawlTaskOutbox createCrawlTaskOutbox() {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(1L),
                "outbox-1",
                "{\"taskId\":1,\"schedulerId\":10}",
                OutboxStatus.PENDING,
                0,
                Instant.now(),
                null);
    }

    @Nested
    @DisplayName("publish - CrawlTask 발행 테스트")
    class PublishCrawlTaskTest {

        @Test
        @DisplayName("CrawlTask를 SQS에 성공적으로 발행한다")
        void publish_withValidCrawlTask_sendsMessageToSqs() {
            // given
            CrawlTask crawlTask = createCrawlTask();
            String idempotencyKey = "test-idem-key";

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-001").build());

            // when
            adapter.publish(crawlTask, idempotencyKey);

            // then
            verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
        }

        @Test
        @DisplayName("FIFO 큐로 발행 시 messageDeduplicationId와 messageGroupId가 설정된다")
        void publish_withFifoQueue_setsDeduplicationAndGroupId() {
            // given
            CrawlTask crawlTask = createCrawlTask();
            String idempotencyKey = "test-idem-key";

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(
                            SendMessageResponse.builder()
                                    .messageId("msg-001")
                                    .sequenceNumber("1")
                                    .build());

            ArgumentCaptor<SendMessageRequest> requestCaptor =
                    ArgumentCaptor.forClass(SendMessageRequest.class);

            // when
            adapterWithFifo.publish(crawlTask, idempotencyKey);

            // then
            verify(sqsClient).sendMessage(requestCaptor.capture());
            SendMessageRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.messageDeduplicationId()).isEqualTo(idempotencyKey);
            assertThat(capturedRequest.messageGroupId()).isEqualTo("crawl-task-10");
        }

        @Test
        @DisplayName("SqsException 발생 시 SqsPublishException으로 래핑된다")
        void publish_whenSqsException_throwsSqsPublishException() {
            // given
            CrawlTask crawlTask = createCrawlTask();

            SqsException sqsException =
                    (SqsException) SqsException.builder().message("SQS 오류").build();
            when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(sqsException);

            // when & then
            assertThatThrownBy(() -> adapter.publish(crawlTask, "idem-key"))
                    .isInstanceOf(SqsPublishException.class)
                    .hasMessageContaining("SQS 메시지 발행 실패");
        }

        @Test
        @DisplayName("JsonProcessingException 발생 시 SqsPublishException으로 래핑된다")
        void publish_whenJsonProcessingException_throwsSqsPublishException()
                throws JsonProcessingException {
            // given
            CrawlTask crawlTask = createCrawlTask();
            SqsClientProperties properties =
                    createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
            CrawlTaskSqsAdapter adapterWithMockMapper =
                    new CrawlTaskSqsAdapter(sqsClient, properties, mockObjectMapper);

            when(mockObjectMapper.writeValueAsString(any()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "직렬화 실패"));

            // when & then
            assertThatThrownBy(() -> adapterWithMockMapper.publish(crawlTask, "idem-key"))
                    .isInstanceOf(SqsPublishException.class)
                    .hasMessageContaining("페이로드 직렬화 실패");
        }

        @Test
        @DisplayName("메시지 바디에 CrawlTask 정보가 포함된다")
        void publish_withValidCrawlTask_messageBodyContainsTaskInfo() {
            // given
            CrawlTask crawlTask = createCrawlTask();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-001").build());

            ArgumentCaptor<SendMessageRequest> requestCaptor =
                    ArgumentCaptor.forClass(SendMessageRequest.class);

            // when
            adapter.publish(crawlTask, "idem-key");

            // then
            verify(sqsClient).sendMessage(requestCaptor.capture());
            SendMessageRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.messageBody()).contains("taskId");
        }
    }

    @Nested
    @DisplayName("publishFromOutbox - Outbox 기반 발행 테스트")
    class PublishFromOutboxTest {

        @Test
        @DisplayName("CrawlTaskOutbox 정보로 SQS에 메시지를 발행한다")
        void publishFromOutbox_withValidOutbox_sendsMessageToSqs() {
            // given
            CrawlTaskOutbox outbox = createCrawlTaskOutbox();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-002").build());

            // when
            adapter.publishFromOutbox(outbox);

            // then
            verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
        }

        @Test
        @DisplayName("FIFO 큐로 Outbox 발행 시 messageGroupId에 taskId가 포함된다")
        void publishFromOutbox_withFifoQueue_setsMessageGroupIdFromTaskId() {
            // given
            CrawlTaskOutbox outbox = createCrawlTaskOutbox();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(
                            SendMessageResponse.builder()
                                    .messageId("msg-002")
                                    .sequenceNumber("2")
                                    .build());

            ArgumentCaptor<SendMessageRequest> requestCaptor =
                    ArgumentCaptor.forClass(SendMessageRequest.class);

            // when
            adapterWithFifo.publishFromOutbox(outbox);

            // then
            verify(sqsClient).sendMessage(requestCaptor.capture());
            SendMessageRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.messageDeduplicationId()).isEqualTo("outbox-1");
            assertThat(capturedRequest.messageGroupId()).isEqualTo("crawl-task-1");
        }

        @Test
        @DisplayName("SqsException 발생 시 SqsPublishException으로 래핑된다")
        void publishFromOutbox_whenSqsException_throwsSqsPublishException() {
            // given
            CrawlTaskOutbox outbox = createCrawlTaskOutbox();

            SqsException sqsException =
                    (SqsException) SqsException.builder().message("SQS 오류").build();
            when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(sqsException);

            // when & then
            assertThatThrownBy(() -> adapter.publishFromOutbox(outbox))
                    .isInstanceOf(SqsPublishException.class)
                    .hasMessageContaining("SQS 메시지 발행 실패");
        }
    }
}
