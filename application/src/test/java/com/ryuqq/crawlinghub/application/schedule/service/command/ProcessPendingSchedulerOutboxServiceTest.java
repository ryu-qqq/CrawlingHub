package com.ryuqq.crawlinghub.application.schedule.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import com.ryuqq.crawlinghub.application.schedule.internal.CrawlSchedulerOutBoxProcessor;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxReadManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProcessPendingSchedulerOutboxService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ReadManager, Processor Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessPendingSchedulerOutboxService 테스트")
class ProcessPendingSchedulerOutboxServiceTest {

    @Mock private CrawlSchedulerOutBoxReadManager outBoxReadManager;

    @Mock private CrawlSchedulerOutBoxProcessor processor;

    @InjectMocks private ProcessPendingSchedulerOutboxService service;

    @Nested
    @DisplayName("execute() PENDING 아웃박스 처리 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] PENDING 아웃박스 없으면 빈 결과 반환")
        void shouldReturnEmptyResultWhenNoPendingOutboxes() {
            // Given
            ProcessPendingSchedulerOutboxCommand command =
                    ProcessPendingSchedulerOutboxCommand.of(10, 30);

            given(outBoxReadManager.findPendingOlderThan(10, 30)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isZero();
            then(processor).should(never()).processOutbox(any());
        }

        @Test
        @DisplayName("[성공] 모든 아웃박스 처리 성공 시 성공 카운트 반영")
        void shouldCountSuccessesWhenAllProcessed() {
            // Given
            ProcessPendingSchedulerOutboxCommand command =
                    ProcessPendingSchedulerOutboxCommand.of(10, 30);

            // CrawlSchedulerOutBox.equals()가 outBoxId 기반이므로 서로 다른 ID 사용
            CrawlSchedulerOutBox outBox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox(); // ID 1L
            CrawlSchedulerOutBox outBox2 = CrawlSchedulerOutBoxFixture.aCompletedOutBox(); // ID 2L
            List<CrawlSchedulerOutBox> outBoxes = List.of(outBox1, outBox2);

            given(outBoxReadManager.findPendingOlderThan(10, 30)).willReturn(outBoxes);
            given(processor.processOutbox(outBox1)).willReturn(true);
            given(processor.processOutbox(outBox2)).willReturn(true);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(2);
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("[부분 실패] 일부 아웃박스 처리 실패 시 실패 카운트 반영")
        void shouldCountFailuresWhenSomeProcessingFailed() {
            // Given
            ProcessPendingSchedulerOutboxCommand command =
                    ProcessPendingSchedulerOutboxCommand.of(10, 30);

            // CrawlSchedulerOutBox.equals()가 outBoxId 기반이므로 각각 다른 ID를 가진 fixture 사용
            CrawlSchedulerOutBox outBox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox(); // ID 1L
            CrawlSchedulerOutBox outBox2 = CrawlSchedulerOutBoxFixture.aCompletedOutBox(); // ID 2L
            CrawlSchedulerOutBox outBox3 = CrawlSchedulerOutBoxFixture.aFailedOutBox(); // ID 3L
            List<CrawlSchedulerOutBox> outBoxes = List.of(outBox1, outBox2, outBox3);

            given(outBoxReadManager.findPendingOlderThan(10, 30)).willReturn(outBoxes);
            given(processor.processOutbox(outBox1)).willReturn(true);
            given(processor.processOutbox(outBox2)).willReturn(false);
            given(processor.processOutbox(outBox3)).willReturn(true);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(3);
            assertThat(result.success()).isEqualTo(2);
            assertThat(result.failed()).isEqualTo(1);
        }

        @Test
        @DisplayName("[실패] 모든 아웃박스 처리 실패 시 실패 카운트 반영")
        void shouldCountAllAsFailedWhenAllProcessingFailed() {
            // Given
            ProcessPendingSchedulerOutboxCommand command =
                    ProcessPendingSchedulerOutboxCommand.of(5, 60);

            // CrawlSchedulerOutBox.equals()가 outBoxId 기반이므로 서로 다른 ID 사용
            CrawlSchedulerOutBox outBox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox(); // ID 1L
            CrawlSchedulerOutBox outBox2 = CrawlSchedulerOutBoxFixture.aFailedOutBox(); // ID 3L
            List<CrawlSchedulerOutBox> outBoxes = List.of(outBox1, outBox2);

            given(outBoxReadManager.findPendingOlderThan(5, 60)).willReturn(outBoxes);
            given(processor.processOutbox(outBox1)).willReturn(false);
            given(processor.processOutbox(outBox2)).willReturn(false);

            // When
            SchedulerBatchProcessingResult result = service.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isZero();
            assertThat(result.failed()).isEqualTo(2);
        }
    }
}
