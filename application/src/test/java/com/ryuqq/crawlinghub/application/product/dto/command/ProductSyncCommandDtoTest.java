package com.ryuqq.crawlinghub.application.product.dto.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Product Sync Command DTO 단위 테스트
 *
 * <p>Record Compact Constructor 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Product Sync Command DTO 테스트")
class ProductSyncCommandDtoTest {

    @Nested
    @DisplayName("RecoverTimeoutProductSyncOutboxCommand 테스트")
    class RecoverTimeoutProductSyncOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverTimeoutProductSyncOutboxCommand command =
                    new RecoverTimeoutProductSyncOutboxCommand(10, 300L);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.timeoutSeconds()).isEqualTo(300L);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverTimeoutProductSyncOutboxCommand command =
                    RecoverTimeoutProductSyncOutboxCommand.of(5, 600L);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutProductSyncOutboxCommand(0, 300L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] timeoutSeconds가 0 이하이면 예외")
        void shouldThrowWhenTimeoutSecondsIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutProductSyncOutboxCommand(10, 0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("RecoverFailedProductSyncOutboxCommand 테스트")
    class RecoverFailedProductSyncOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverFailedProductSyncOutboxCommand command =
                    new RecoverFailedProductSyncOutboxCommand(10, 300);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.delaySeconds()).isEqualTo(300);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverFailedProductSyncOutboxCommand command =
                    RecoverFailedProductSyncOutboxCommand.of(5, 600);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverFailedProductSyncOutboxCommand(0, 300))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] delaySeconds가 0 이하이면 예외")
        void shouldThrowWhenDelaySecondsIsZero() {
            assertThatThrownBy(() -> new RecoverFailedProductSyncOutboxCommand(10, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("PublishPendingSyncOutboxCommand 테스트")
    class PublishPendingSyncOutboxCommandTest {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            PublishPendingSyncOutboxCommand command = new PublishPendingSyncOutboxCommand(10, 3);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.maxRetryCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("[성공] maxRetryCount가 0이어도 생성 가능")
        void shouldCreateWithZeroMaxRetryCount() {
            PublishPendingSyncOutboxCommand command = new PublishPendingSyncOutboxCommand(10, 0);
            assertThat(command.maxRetryCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            PublishPendingSyncOutboxCommand command = PublishPendingSyncOutboxCommand.of(5, 3);
            assertThat(command.batchSize()).isEqualTo(5);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new PublishPendingSyncOutboxCommand(0, 3))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] maxRetryCount가 음수이면 예외")
        void shouldThrowWhenMaxRetryCountIsNegative() {
            assertThatThrownBy(() -> new PublishPendingSyncOutboxCommand(10, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
