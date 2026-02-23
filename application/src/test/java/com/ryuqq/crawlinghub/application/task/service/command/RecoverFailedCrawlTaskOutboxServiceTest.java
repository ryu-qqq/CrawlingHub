package com.ryuqq.crawlinghub.application.task.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.task.dto.command.RecoverFailedCrawlTaskOutboxCommand;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RecoverFailedCrawlTaskOutboxService 단위 테스트
 *
 * <p>FAILED 아웃박스를 PENDING으로 복원하는 서비스 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecoverFailedCrawlTaskOutboxService 테스트")
class RecoverFailedCrawlTaskOutboxServiceTest {

    @Mock private CrawlTaskOutboxReadManager outboxReadManager;
    @Mock private CrawlTaskOutboxCommandManager outboxCommandManager;

    @InjectMocks private RecoverFailedCrawlTaskOutboxService sut;

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] FAILED 아웃박스가 없으면 empty 결과 반환")
        void shouldReturnEmptyWhenNoFailedOutboxes() {
            // Given
            RecoverFailedCrawlTaskOutboxCommand command =
                    new RecoverFailedCrawlTaskOutboxCommand(10, 300);
            given(outboxReadManager.findFailedOlderThan(10, 300)).willReturn(List.of());

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isZero();
            assertThat(result.success()).isZero();
        }

        @Test
        @DisplayName("[성공] FAILED 아웃박스를 PENDING으로 복원")
        void shouldResetFailedOutboxToPending() {
            // Given
            RecoverFailedCrawlTaskOutboxCommand command =
                    new RecoverFailedCrawlTaskOutboxCommand(10, 300);
            CrawlTaskOutbox failedOutbox = CrawlTaskOutboxFixture.aRetryableFailedOutbox();

            given(outboxReadManager.findFailedOlderThan(10, 300)).willReturn(List.of(failedOutbox));

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(1);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isZero();
            then(outboxCommandManager).should().persist(failedOutbox);
        }

        @Test
        @DisplayName("[성공] 복수 FAILED 아웃박스를 모두 PENDING으로 복원")
        void shouldResetMultipleFailedOutboxesToPending() {
            // Given
            RecoverFailedCrawlTaskOutboxCommand command =
                    new RecoverFailedCrawlTaskOutboxCommand(10, 300);
            CrawlTaskOutbox outbox1 = CrawlTaskOutboxFixture.aFailedOutbox();
            CrawlTaskOutbox outbox2 = CrawlTaskOutboxFixture.aRetryableFailedOutbox();

            given(outboxReadManager.findFailedOlderThan(10, 300))
                    .willReturn(List.of(outbox1, outbox2));

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(2);
            assertThat(result.failed()).isZero();
        }

        @Test
        @DisplayName("[부분 실패] persist 예외 발생 시 실패 카운트 증가")
        void shouldCountFailWhenPersistThrows() {
            // Given
            RecoverFailedCrawlTaskOutboxCommand command =
                    new RecoverFailedCrawlTaskOutboxCommand(10, 300);
            CrawlTaskOutbox outbox1 = CrawlTaskOutboxFixture.aFailedOutbox();
            CrawlTaskOutbox outbox2 = CrawlTaskOutboxFixture.aRetryableFailedOutbox();

            given(outboxReadManager.findFailedOlderThan(10, 300))
                    .willReturn(List.of(outbox1, outbox2));
            willThrow(new RuntimeException("DB 오류")).given(outboxCommandManager).persist(outbox1);

            // When
            SchedulerBatchProcessingResult result = sut.execute(command);

            // Then
            assertThat(result.total()).isEqualTo(2);
            assertThat(result.success()).isEqualTo(1);
            assertThat(result.failed()).isEqualTo(1);
        }
    }
}
