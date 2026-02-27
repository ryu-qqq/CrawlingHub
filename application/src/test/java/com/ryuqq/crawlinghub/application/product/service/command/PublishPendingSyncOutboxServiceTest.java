package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.product.CrawledProductSyncOutboxFixture;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.product.dto.command.PublishPendingSyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.internal.CrawledProductSyncOutboxProcessor;
import com.ryuqq.crawlinghub.application.product.manager.CrawledProductSyncOutboxReadManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * PublishPendingSyncOutboxService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, Processor 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("PublishPendingSyncOutboxService 단위 테스트")
class PublishPendingSyncOutboxServiceTest {

    @Mock private CrawledProductSyncOutboxReadManager readManager;
    @Mock private CrawledProductSyncOutboxProcessor processor;

    @InjectMocks private PublishPendingSyncOutboxService sut;

    @Nested
    @DisplayName("execute() 메서드 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("[성공] PENDING Outbox가 없으면 empty 결과 반환")
        void shouldReturnEmptyResultWhenNoPendingOutboxes() {
            // Given
            PublishPendingSyncOutboxCommand command = PublishPendingSyncOutboxCommand.of(100, 3);
            given(readManager.findPendingOutboxes(100)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(processor).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("[성공] PENDING Outbox 처리 성공 시 success 카운트 반환")
        void shouldReturnSuccessCountWhenAllProcessed() {
            // Given
            PublishPendingSyncOutboxCommand command = PublishPendingSyncOutboxCommand.of(100, 3);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            given(readManager.findPendingOutboxes(100)).willReturn(List.of(outbox));
            given(processor.processOutbox(outbox)).willReturn(true);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(processor).should().processOutbox(outbox);
        }

        @Test
        @DisplayName("[실패] Outbox 처리 실패 시 failed 카운트 반영")
        void shouldCountFailedWhenProcessorReturnsFalse() {
            // Given
            PublishPendingSyncOutboxCommand command = PublishPendingSyncOutboxCommand.of(50, 5);
            CrawledProductSyncOutbox outbox =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            given(readManager.findPendingOutboxes(50)).willReturn(List.of(outbox));
            given(processor.processOutbox(outbox)).willReturn(false);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] 복수 Outbox 중 일부 성공, 일부 실패 시 부분 성공 결과")
        void shouldReturnPartialResultWhenMixedOutcomes() {
            // Given
            PublishPendingSyncOutboxCommand command = PublishPendingSyncOutboxCommand.of(100, 3);
            CrawledProductSyncOutbox outbox1 =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            CrawledProductSyncOutbox outbox2 =
                    CrawledProductSyncOutboxFixture.aReconstitutedPending();
            given(readManager.findPendingOutboxes(100)).willReturn(List.of(outbox1, outbox2));
            given(processor.processOutbox(outbox1)).willReturn(true);
            given(processor.processOutbox(outbox2)).willReturn(false);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
