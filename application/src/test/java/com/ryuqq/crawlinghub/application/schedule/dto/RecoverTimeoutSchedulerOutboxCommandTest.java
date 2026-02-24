package com.ryuqq.crawlinghub.application.schedule.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.schedule.dto.command.RecoverTimeoutSchedulerOutboxCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * RecoverTimeoutSchedulerOutboxCommand 단위 테스트
 *
 * <p>Compact Constructor 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("RecoverTimeoutSchedulerOutboxCommand 테스트")
class RecoverTimeoutSchedulerOutboxCommandTest {

    @Nested
    @DisplayName("생성자 검증 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            RecoverTimeoutSchedulerOutboxCommand command =
                    new RecoverTimeoutSchedulerOutboxCommand(10, 300L);
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.timeoutSeconds()).isEqualTo(300L);
        }

        @Test
        @DisplayName("[성공] 팩토리 메서드로 생성")
        void shouldCreateViaFactory() {
            RecoverTimeoutSchedulerOutboxCommand command =
                    RecoverTimeoutSchedulerOutboxCommand.of(5, 600L);
            assertThat(command.batchSize()).isEqualTo(5);
            assertThat(command.timeoutSeconds()).isEqualTo(600L);
        }

        @Test
        @DisplayName("[실패] batchSize가 0 이하이면 예외")
        void shouldThrowWhenBatchSizeIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutSchedulerOutboxCommand(0, 300L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("batchSize");
        }

        @Test
        @DisplayName("[실패] batchSize가 음수이면 예외")
        void shouldThrowWhenBatchSizeIsNegative() {
            assertThatThrownBy(() -> new RecoverTimeoutSchedulerOutboxCommand(-1, 300L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] timeoutSeconds가 0 이하이면 예외")
        void shouldThrowWhenTimeoutSecondsIsZero() {
            assertThatThrownBy(() -> new RecoverTimeoutSchedulerOutboxCommand(10, 0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("timeoutSeconds");
        }
    }
}
