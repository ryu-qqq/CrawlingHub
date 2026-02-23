package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.ProcessPendingCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskOutboxProcessor;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProcessPendingCrawlTaskOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPendingCrawlTaskOutboxService 테스트")
class ProcessPendingCrawlTaskOutboxServiceTest {

    @Mock private CrawlTaskOutboxReadManager outboxReadManager;

    @Mock private CrawlTaskOutboxProcessor processor;

    @InjectMocks private ProcessPendingCrawlTaskOutboxService service;

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
    @DisplayName("execute() PENDING 아웃박스 처리 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] PENDING 아웃박스가 없으면 빈 결과 반환")
        void shouldReturnEmptyResultWhenNoPendingOutboxes() {
            // Given
            ProcessPendingCrawlTaskOutboxCommand command =
                    ProcessPendingCrawlTaskOutboxCommand.of(100, 30);

            given(outboxReadManager.findPendingOlderThan(100, 30)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(processor).should(never()).processOutbox(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[성공] 모든 아웃박스 처리 성공")
        void shouldProcessAllOutboxesSuccessfully() {
            // Given
            ProcessPendingCrawlTaskOutboxCommand command =
                    ProcessPendingCrawlTaskOutboxCommand.of(100, 30);

            CrawlTaskOutbox outbox1 = createPendingOutbox(1L);
            CrawlTaskOutbox outbox2 = createPendingOutbox(2L);
            CrawlTaskOutbox outbox3 = createPendingOutbox(3L);
            List<CrawlTaskOutbox> outboxes = List.of(outbox1, outbox2, outbox3);

            given(outboxReadManager.findPendingOlderThan(100, 30)).willReturn(outboxes);
            given(processor.processOutbox(any(CrawlTaskOutbox.class))).willReturn(true);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(3);
            assertThat(result.success()).isEqualTo(3);
            assertThat(result.failed()).isZero();
            then(processor).should(times(3)).processOutbox(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[부분 실패] 일부 아웃박스 처리 실패 시 실패 카운트 반영")
        void shouldCountFailedOutboxes() {
            // Given
            ProcessPendingCrawlTaskOutboxCommand command =
                    ProcessPendingCrawlTaskOutboxCommand.of(100, 30);

            CrawlTaskOutbox outbox1 = createPendingOutbox(1L);
            CrawlTaskOutbox outbox2 = createPendingOutbox(2L);
            List<CrawlTaskOutbox> outboxes = List.of(outbox1, outbox2);

            given(outboxReadManager.findPendingOlderThan(100, 30)).willReturn(outboxes);
            given(processor.processOutbox(outbox1)).willReturn(true);
            given(processor.processOutbox(outbox2)).willReturn(false);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
