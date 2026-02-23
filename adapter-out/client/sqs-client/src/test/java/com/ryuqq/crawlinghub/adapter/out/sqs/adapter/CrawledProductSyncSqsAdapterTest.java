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
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductSyncOutboxId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.id.SellerId;
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
 * CrawledProductSyncSqsAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawledProductSyncSqsAdapter 테스트")
class CrawledProductSyncSqsAdapterTest {

    @Mock private SqsClient sqsClient;

    @Mock private ObjectMapper mockObjectMapper;

    private CrawledProductSyncSqsAdapter adapter;
    private CrawledProductSyncSqsAdapter adapterWithFifo;

    @BeforeEach
    void setUp() {
        // 일반 큐 어댑터
        SqsClientProperties properties =
                createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
        adapter = new CrawledProductSyncSqsAdapter(sqsClient, properties, new ObjectMapper());

        // FIFO 큐 어댑터
        SqsClientProperties fifoProperties =
                createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync.fifo");
        adapterWithFifo =
                new CrawledProductSyncSqsAdapter(sqsClient, fifoProperties, new ObjectMapper());
    }

    private SqsClientProperties createProperties(String productSyncUrl) {
        SqsClientProperties properties = new SqsClientProperties();
        properties.setRegion("ap-northeast-2");
        SqsClientProperties.Queues queues = new SqsClientProperties.Queues();
        queues.setCrawlTask("https://sqs.ap-northeast-2.amazonaws.com/123/crawl-task");
        queues.setProductSync(productSyncUrl);
        properties.setQueues(queues);
        return properties;
    }

    private CrawledProductSyncOutbox createCreateOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(1L),
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                SyncType.CREATE,
                "test-idem-key",
                null,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    private CrawledProductSyncOutbox createUpdatePriceOutbox() {
        return CrawledProductSyncOutbox.reconstitute(
                CrawledProductSyncOutboxId.of(2L),
                CrawledProductId.of(100L),
                SellerId.of(200L),
                12345L,
                SyncType.UPDATE_PRICE,
                "test-idem-key-price",
                99999L,
                ProductOutboxStatus.PENDING,
                0,
                null,
                Instant.now(),
                null);
    }

    @Nested
    @DisplayName("publish - CREATE 타입 발행 테스트")
    class PublishCreateTest {

        @Test
        @DisplayName("CREATE 타입 Outbox를 SQS에 성공적으로 발행한다")
        void publish_withCreateOutbox_sendsMessageToSqs() {
            // given
            CrawledProductSyncOutbox outbox = createCreateOutbox();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-001").build());

            // when
            adapter.publish(outbox);

            // then
            verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
        }

        @Test
        @DisplayName("메시지 바디에 syncType 정보가 포함된다")
        void publish_withCreateOutbox_messageBodyContainsSyncType() {
            // given
            CrawledProductSyncOutbox outbox = createCreateOutbox();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-001").build());

            ArgumentCaptor<SendMessageRequest> requestCaptor =
                    ArgumentCaptor.forClass(SendMessageRequest.class);

            // when
            adapter.publish(outbox);

            // then
            verify(sqsClient).sendMessage(requestCaptor.capture());
            SendMessageRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.messageBody()).contains("CREATE");
            assertThat(capturedRequest.messageBody()).contains("crawledProductId");
        }
    }

    @Nested
    @DisplayName("publish - FIFO 큐 발행 테스트")
    class PublishFifoTest {

        @Test
        @DisplayName("FIFO 큐로 발행 시 messageDeduplicationId와 messageGroupId가 설정된다")
        void publish_withFifoQueue_setsDeduplicationAndGroupId() {
            // given
            CrawledProductSyncOutbox outbox = createUpdatePriceOutbox();

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(
                            SendMessageResponse.builder()
                                    .messageId("msg-002")
                                    .sequenceNumber("1")
                                    .build());

            ArgumentCaptor<SendMessageRequest> requestCaptor =
                    ArgumentCaptor.forClass(SendMessageRequest.class);

            // when
            adapterWithFifo.publish(outbox);

            // then
            verify(sqsClient).sendMessage(requestCaptor.capture());
            SendMessageRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.messageDeduplicationId()).isEqualTo("test-idem-key-price");
            assertThat(capturedRequest.messageGroupId()).isEqualTo("product-sync-100");
        }
    }

    @Nested
    @DisplayName("publish - 예외 처리 테스트")
    class PublishExceptionTest {

        @Test
        @DisplayName("SqsException 발생 시 SqsPublishException으로 래핑된다")
        void publish_whenSqsException_throwsSqsPublishException() {
            // given
            CrawledProductSyncOutbox outbox = createCreateOutbox();

            SqsException sqsException =
                    (SqsException) SqsException.builder().message("SQS 오류").build();
            when(sqsClient.sendMessage(any(SendMessageRequest.class))).thenThrow(sqsException);

            // when & then
            assertThatThrownBy(() -> adapter.publish(outbox))
                    .isInstanceOf(SqsPublishException.class)
                    .hasMessageContaining("SQS 메시지 발행 실패");
        }

        @Test
        @DisplayName("JsonProcessingException 발생 시 SqsPublishException으로 래핑된다")
        void publish_whenJsonProcessingException_throwsSqsPublishException()
                throws JsonProcessingException {
            // given
            CrawledProductSyncOutbox outbox = createCreateOutbox();
            SqsClientProperties properties =
                    createProperties("https://sqs.ap-northeast-2.amazonaws.com/123/product-sync");
            CrawledProductSyncSqsAdapter adapterWithMockMapper =
                    new CrawledProductSyncSqsAdapter(sqsClient, properties, mockObjectMapper);

            when(mockObjectMapper.writeValueAsString(any()))
                    .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "직렬화 실패"));

            // when & then
            assertThatThrownBy(() -> adapterWithMockMapper.publish(outbox))
                    .isInstanceOf(SqsPublishException.class)
                    .hasMessageContaining("페이로드 직렬화 실패");
        }
    }
}
