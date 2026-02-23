package com.ryuqq.crawlinghub.application.task.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxProcessor 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxProcessor 테스트")
class CrawlTaskOutboxProcessorTest {

    @Mock private CrawlTaskCommandManager commandManager;

    @Mock private CrawlTaskOutboxCommandManager outboxCommandManager;

    @Mock private CrawlTaskReadManager readManager;

    @Mock private CrawlTaskMessageManager messageManager;

    @InjectMocks private CrawlTaskOutboxProcessor processor;

    private CrawlTaskOutbox createPendingOutbox(long taskId) {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(taskId),
                "outbox-" + taskId,
                "{\"taskId\": " + taskId + "}",
                OutboxStatus.PENDING,
                0,
                Instant.now().minusSeconds(300),
                null);
    }

    @Nested
    @DisplayName("processOutbox() 아웃박스 처리 테스트")
    class ProcessOutbox {

        @Test
        @DisplayName("[성공] 정상 처리 시 true 반환")
        void shouldReturnTrueOnSuccess() {
            // Given
            CrawlTaskOutbox outbox = createPendingOutbox(1L);

            // When
            boolean result = processor.processOutbox(outbox);

            // Then
            assertThat(result).isTrue();
            then(outboxCommandManager).should(times(2)).persist(outbox);
            then(readManager).should().findById(any(CrawlTaskId.class));
            then(messageManager).should().publishFromOutbox(outbox);
        }

        @Test
        @DisplayName("[실패] SQS 발행 실패 시 FAILED 처리 후 false 반환")
        void shouldReturnFalseAndMarkAsFailedOnSqsFailure() {
            // Given
            CrawlTaskOutbox outbox = createPendingOutbox(2L);
            willThrow(new RuntimeException("SQS 발행 실패"))
                    .given(messageManager)
                    .publishFromOutbox(any(CrawlTaskOutbox.class));

            // When
            boolean result = processor.processOutbox(outbox);

            // Then
            assertThat(result).isFalse();
            then(outboxCommandManager).should(times(2)).persist(outbox);
            then(messageManager).should().publishFromOutbox(outbox);
        }

        @Test
        @DisplayName("[실패] CrawlTask 조회 실패 시 FAILED 처리 후 false 반환")
        void shouldReturnFalseAndMarkAsFailedOnTaskReadFailure() {
            // Given
            CrawlTaskOutbox outbox = createPendingOutbox(3L);
            given(readManager.findById(any(CrawlTaskId.class)))
                    .willThrow(new RuntimeException("조회 실패"));

            // When
            boolean result = processor.processOutbox(outbox);

            // Then
            assertThat(result).isFalse();
            then(outboxCommandManager).should(times(2)).persist(outbox);
            then(messageManager).should(never()).publishFromOutbox(any(CrawlTaskOutbox.class));
        }
    }
}
