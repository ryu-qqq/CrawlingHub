package com.ryuqq.crawlinghub.application.outbox.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.ryuqq.crawlinghub.application.task.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.manager.messaging.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.query.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.service.command.RepublishOutboxService;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RepublishOutboxService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RepublishOutboxService 테스트")
class RepublishOutboxServiceTest {

    private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private CrawlTaskOutboxReadManager outboxReadManager;
    @Mock private CrawlTaskOutboxTransactionManager outboxTransactionManager;
    @Mock private CrawlTaskMessageManager messageManager;

    private RepublishOutboxService service;

    @BeforeEach
    void setUp() {
        service =
                new RepublishOutboxService(
                        outboxReadManager, outboxTransactionManager, messageManager);
    }

    @Nested
    @DisplayName("republish() 테스트")
    class Republish {

        @Test
        @DisplayName("[성공] Outbox 재발행 성공")
        void shouldRepublishSuccessfully() {
            // Given
            Long crawlTaskId = 1L;
            CrawlTaskOutbox outbox = createPendingOutbox(crawlTaskId);
            given(outboxReadManager.findByCrawlTaskId(CrawlTaskId.of(crawlTaskId)))
                    .willReturn(Optional.of(outbox));

            // When
            RepublishResultResponse result = service.republish(crawlTaskId);

            // Then
            assertThat(result.success()).isTrue();
            assertThat(result.crawlTaskId()).isEqualTo(crawlTaskId);
            assertThat(result.message()).contains("재발행이 완료");
            verify(messageManager).publishFromOutbox(outbox);
            verify(outboxTransactionManager).markAsSent(outbox);
        }

        @Test
        @DisplayName("[실패] Outbox가 존재하지 않음")
        void shouldFailWhenOutboxNotFound() {
            // Given
            Long crawlTaskId = 1L;
            given(outboxReadManager.findByCrawlTaskId(CrawlTaskId.of(crawlTaskId)))
                    .willReturn(Optional.empty());

            // When
            RepublishResultResponse result = service.republish(crawlTaskId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.crawlTaskId()).isEqualTo(crawlTaskId);
            assertThat(result.message()).contains("Outbox를 찾을 수 없습니다");
            verifyNoInteractions(messageManager);
            verifyNoInteractions(outboxTransactionManager);
        }

        @Test
        @DisplayName("[실패] 이미 발행 완료된 Outbox")
        void shouldFailWhenAlreadySent() {
            // Given
            Long crawlTaskId = 1L;
            CrawlTaskOutbox outbox = createSentOutbox(crawlTaskId);
            given(outboxReadManager.findByCrawlTaskId(CrawlTaskId.of(crawlTaskId)))
                    .willReturn(Optional.of(outbox));

            // When
            RepublishResultResponse result = service.republish(crawlTaskId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.crawlTaskId()).isEqualTo(crawlTaskId);
            assertThat(result.message()).contains("이미 발행 완료된");
            verifyNoInteractions(messageManager);
            verifyNoInteractions(outboxTransactionManager);
        }

        @Test
        @DisplayName("[실패] SQS 발행 중 예외 발생")
        void shouldFailWhenPublishingFails() {
            // Given
            Long crawlTaskId = 1L;
            CrawlTaskOutbox outbox = createPendingOutbox(crawlTaskId);
            given(outboxReadManager.findByCrawlTaskId(CrawlTaskId.of(crawlTaskId)))
                    .willReturn(Optional.of(outbox));
            doThrow(new RuntimeException("SQS 연결 실패"))
                    .when(messageManager)
                    .publishFromOutbox(any(CrawlTaskOutbox.class));

            // When
            RepublishResultResponse result = service.republish(crawlTaskId);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.crawlTaskId()).isEqualTo(crawlTaskId);
            assertThat(result.message()).contains("SQS 발행 실패");
            verify(outboxTransactionManager).markAsFailed(outbox);
        }
    }

    // === Helper Methods ===

    private CrawlTaskOutbox createPendingOutbox(Long crawlTaskId) {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(crawlTaskId),
                "idempotency-key-" + crawlTaskId,
                "{\"taskId\":" + crawlTaskId + "}",
                OutboxStatus.PENDING,
                0,
                FIXED_TIME,
                null);
    }

    private CrawlTaskOutbox createSentOutbox(Long crawlTaskId) {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(crawlTaskId),
                "idempotency-key-" + crawlTaskId,
                "{\"taskId\":" + crawlTaskId + "}",
                OutboxStatus.SENT,
                0,
                FIXED_TIME,
                FIXED_TIME);
    }
}
