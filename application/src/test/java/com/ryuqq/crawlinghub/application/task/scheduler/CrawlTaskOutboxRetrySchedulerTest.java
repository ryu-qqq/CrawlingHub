package com.ryuqq.crawlinghub.application.task.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxRetryScheduler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port, Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxRetryScheduler 테스트")
class CrawlTaskOutboxRetrySchedulerTest {

    @Mock
    private CrawlTaskOutboxQueryPort outboxQueryPort;

    @Mock
    private CrawlTaskOutboxTransactionManager outboxTransactionManager;

    @Mock
    private CrawlTaskMessageManager crawlTaskMessageManager;

    @InjectMocks
    private CrawlTaskOutboxRetryScheduler scheduler;

    @Nested
    @DisplayName("processOutbox() 테스트")
    class ProcessOutbox {

        @Test
        @DisplayName("[성공] PENDING/FAILED Outbox 재처리 → 성공 시 SENT 상태로 변경")
        void shouldProcessOutboxAndMarkSent() {
            // Given
            CrawlTaskOutbox outbox1 = CrawlTaskOutboxFixture.aPendingOutbox();
            CrawlTaskOutbox outbox2 = CrawlTaskOutboxFixture.aFailedOutbox();
            List<CrawlTaskOutbox> outboxes = List.of(outbox1, outbox2);

            given(outboxQueryPort.findByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(outboxes);

            // When
            scheduler.processOutbox();

            // Then
            verify(outboxQueryPort).findByCriteria(any(CrawlTaskOutboxCriteria.class));
            verify(crawlTaskMessageManager, times(2)).publishFromOutbox(any());
            verify(outboxTransactionManager, times(2)).markAsSent(any());
            verify(outboxTransactionManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("[성공] 처리할 Outbox가 없는 경우 → 아무 작업 안함")
        void shouldDoNothingWhenNoOutboxFound() {
            // Given
            given(outboxQueryPort.findByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(Collections.emptyList());

            // When
            scheduler.processOutbox();

            // Then
            verify(outboxQueryPort).findByCriteria(any(CrawlTaskOutboxCriteria.class));
            verify(crawlTaskMessageManager, never()).publishFromOutbox(any());
            verify(outboxTransactionManager, never()).markAsSent(any());
            verify(outboxTransactionManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("[실패] SQS 발행 예외 → Outbox FAILED 상태로 변경")
        void shouldMarkAsFailedWhenSqsPublishFails() {
            // Given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();
            List<CrawlTaskOutbox> outboxes = List.of(outbox);

            given(outboxQueryPort.findByCriteria(any(CrawlTaskOutboxCriteria.class)))
                    .willReturn(outboxes);
            doThrow(new RuntimeException("SQS publish error"))
                    .when(crawlTaskMessageManager).publishFromOutbox(outbox);

            // When
            scheduler.processOutbox();

            // Then
            verify(outboxQueryPort).findByCriteria(any(CrawlTaskOutboxCriteria.class));
            verify(crawlTaskMessageManager).publishFromOutbox(outbox);
            verify(outboxTransactionManager, never()).markAsSent(any());
            verify(outboxTransactionManager).markAsFailed(outbox);
        }
    }
}
