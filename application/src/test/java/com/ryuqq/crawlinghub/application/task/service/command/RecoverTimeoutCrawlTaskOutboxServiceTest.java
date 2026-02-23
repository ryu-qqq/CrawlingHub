package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverTimeoutCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
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
 * RecoverTimeoutCrawlTaskOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverTimeoutCrawlTaskOutboxService 테스트")
class RecoverTimeoutCrawlTaskOutboxServiceTest {

    @Mock private CrawlTaskOutboxReadManager outboxReadManager;

    @Mock private CrawlTaskOutboxCommandManager outboxCommandManager;

    @InjectMocks private RecoverTimeoutCrawlTaskOutboxService service;

    private CrawlTaskOutbox createProcessingOutbox(long taskId) {
        return CrawlTaskOutbox.reconstitute(
                CrawlTaskId.of(taskId),
                "outbox-" + taskId,
                "{\"taskId\": " + taskId + "}",
                OutboxStatus.PROCESSING,
                0,
                Instant.now().minusSeconds(600),
                Instant.now().minusSeconds(400));
    }

    @Nested
    @DisplayName("execute() 타임아웃 아웃박스 복구 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 좀비 아웃박스가 없으면 빈 결과 반환")
        void shouldReturnEmptyResultWhenNoStaleOutboxes() {
            // Given
            RecoverTimeoutCrawlTaskOutboxCommand command =
                    RecoverTimeoutCrawlTaskOutboxCommand.of(50, 300);

            given(outboxReadManager.findStaleProcessing(50, 300L)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(outboxCommandManager).should(never()).persist(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[성공] 모든 좀비 아웃박스 복구 성공")
        void shouldRecoverAllStaleOutboxes() {
            // Given
            RecoverTimeoutCrawlTaskOutboxCommand command =
                    RecoverTimeoutCrawlTaskOutboxCommand.of(50, 300);

            CrawlTaskOutbox outbox1 = createProcessingOutbox(1L);
            CrawlTaskOutbox outbox2 = createProcessingOutbox(2L);
            CrawlTaskOutbox outbox3 = createProcessingOutbox(3L);
            List<CrawlTaskOutbox> staleOutboxes = List.of(outbox1, outbox2, outbox3);

            given(outboxReadManager.findStaleProcessing(50, 300L)).willReturn(staleOutboxes);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(3);
            assertThat(result.success()).isEqualTo(3);
            assertThat(result.failed()).isZero();
            assertThat(outbox1.isPending()).isTrue();
            assertThat(outbox2.isPending()).isTrue();
            assertThat(outbox3.isPending()).isTrue();
            then(outboxCommandManager).should(times(3)).persist(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[부분 실패] persist 실패 시 실패 카운트 반영")
        void shouldCountFailedRecoveries() {
            // Given
            RecoverTimeoutCrawlTaskOutboxCommand command =
                    RecoverTimeoutCrawlTaskOutboxCommand.of(50, 300);

            CrawlTaskOutbox outbox1 = createProcessingOutbox(1L);
            CrawlTaskOutbox outbox2 = createProcessingOutbox(2L);
            List<CrawlTaskOutbox> staleOutboxes = List.of(outbox1, outbox2);

            given(outboxReadManager.findStaleProcessing(50, 300L)).willReturn(staleOutboxes);
            willThrow(new RuntimeException("persist failed"))
                    .willDoNothing()
                    .given(outboxCommandManager)
                    .persist(any(CrawlTaskOutbox.class));

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
