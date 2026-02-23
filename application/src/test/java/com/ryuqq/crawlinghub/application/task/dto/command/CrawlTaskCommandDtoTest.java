package com.ryuqq.crawlinghub.application.task.dto.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlTask Command DTO 단위 테스트
 *
 * <p>Record Compact Constructor 검증 및 팩토리 메서드 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlTask Command DTO 테스트")
class CrawlTaskCommandDtoTest {

    @Nested
    @DisplayName("RecoverStuckCrawlTaskCommand 테스트")
    class RecoverStuckCrawlTaskCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverStuckCrawlTaskCommand command = new RecoverStuckCrawlTaskCommand(10, 1800L);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.timeoutSeconds()).isEqualTo(1800L);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverStuckCrawlTaskCommand command = RecoverStuckCrawlTaskCommand.of(5, 3600L);
            assertThat(command.batchSize()).isEqualTo(5);
            assertThat(command.timeoutSeconds()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverStuckCrawlTaskCommand(0, 1800L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] timeoutSeconds가 0 이하이면 예외")
        void shouldThrowWhenTimeoutSecondsIsZero() {
            assertThatThrownBy(() -> new RecoverStuckCrawlTaskCommand(10, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("RecoverFailedCrawlTaskOutboxCommand 테스트")
    class RecoverFailedCrawlTaskOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverFailedCrawlTaskOutboxCommand command =
                    new RecoverFailedCrawlTaskOutboxCommand(10, 300);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.delaySeconds()).isEqualTo(300);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverFailedCrawlTaskOutboxCommand command =
                    RecoverFailedCrawlTaskOutboxCommand.of(5, 600);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverFailedCrawlTaskOutboxCommand(0, 300))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] delaySeconds가 0 이하이면 예외")
        void shouldThrowWhenDelaySecondsIsZero() {
            assertThatThrownBy(() -> new RecoverFailedCrawlTaskOutboxCommand(10, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("RecoverTimeoutCrawlTaskOutboxCommand 테스트")
    class RecoverTimeoutCrawlTaskOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverTimeoutCrawlTaskOutboxCommand command =
                    new RecoverTimeoutCrawlTaskOutboxCommand(10, 300L);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.timeoutSeconds()).isEqualTo(300L);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverTimeoutCrawlTaskOutboxCommand command =
                    RecoverTimeoutCrawlTaskOutboxCommand.of(5, 600L);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutCrawlTaskOutboxCommand(0, 300L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] timeoutSeconds가 0 이하이면 예외")
        void shouldThrowWhenTimeoutSecondsIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutCrawlTaskOutboxCommand(10, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("ProcessPendingCrawlTaskOutboxCommand 테스트")
    class ProcessPendingCrawlTaskOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            ProcessPendingCrawlTaskOutboxCommand command =
                    new ProcessPendingCrawlTaskOutboxCommand(10, 5);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.delaySeconds()).isEqualTo(5);
        }

        @Test
        @DisplayName("[성공] delaySeconds가 0이어도 생성 가능")
        void shouldCreateWithZeroDelay() {
            ProcessPendingCrawlTaskOutboxCommand command =
                    new ProcessPendingCrawlTaskOutboxCommand(10, 0);
            assertThat(command.delaySeconds()).isZero();
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            ProcessPendingCrawlTaskOutboxCommand command =
                    ProcessPendingCrawlTaskOutboxCommand.of(5, 10);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new ProcessPendingCrawlTaskOutboxCommand(0, 5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("RetryCrawlTaskCommand 테스트")
    class RetryCrawlTaskCommandTest {

        @Test
        @DisplayName("[성공] 유효한 ID로 생성")
        void shouldCreateWithValidId() {
            RetryCrawlTaskCommand command = new RetryCrawlTaskCommand(1L);
            assertThat(command.crawlTaskId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[실패] null ID이면 예외")
        void shouldThrowWhenIdIsNull() {
            assertThatThrownBy(() -> new RetryCrawlTaskCommand(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] 0 이하 ID이면 예외")
        void shouldThrowWhenIdIsZero() {
            assertThatThrownBy(() -> new RetryCrawlTaskCommand(0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("TriggerCrawlTaskCommand 테스트")
    class TriggerCrawlTaskCommandTest {

        @Test
        @DisplayName("[성공] 유효한 스케줄러 ID로 생성")
        void shouldCreateWithValidSchedulerId() {
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(1L);
            assertThat(command.crawlSchedulerId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[실패] null 스케줄러 ID이면 예외")
        void shouldThrowWhenSchedulerIdIsNull() {
            assertThatThrownBy(() -> new TriggerCrawlTaskCommand(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
