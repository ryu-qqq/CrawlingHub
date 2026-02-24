package com.ryuqq.crawlinghub.application.schedule.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.schedule.dto.command.ProcessPendingSchedulerOutboxCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProcessPendingSchedulerOutboxCommand 단위 테스트
 *
 * <p>커맨드 생성 유효성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProcessPendingSchedulerOutboxCommand 테스트")
class ProcessPendingSchedulerOutboxCommandTest {

    @Nested
    @DisplayName("생성자 유효성 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 양수 batchSize와 0 이상의 delaySeconds로 생성")
        void shouldCreateWithValidParameters() {
            // When
            ProcessPendingSchedulerOutboxCommand command =
                    new ProcessPendingSchedulerOutboxCommand(10, 30);

            // Then
            assertThat(command.batchSize()).isEqualTo(10);
            assertThat(command.delaySeconds()).isEqualTo(30);
        }

        @Test
        @DisplayName("[성공] delaySeconds 0으로 생성 가능")
        void shouldCreateWithZeroDelaySeconds() {
            // When
            ProcessPendingSchedulerOutboxCommand command =
                    new ProcessPendingSchedulerOutboxCommand(5, 0);

            // Then
            assertThat(command.delaySeconds()).isZero();
        }

        @Test
        @DisplayName("[실패] batchSize 0 이하이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenBatchSizeIsZero() {
            // When & Then
            assertThatThrownBy(() -> new ProcessPendingSchedulerOutboxCommand(0, 30))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("batchSize는 0보다 커야 합니다");
        }

        @Test
        @DisplayName("[실패] 음수 batchSize이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenBatchSizeIsNegative() {
            // When & Then
            assertThatThrownBy(() -> new ProcessPendingSchedulerOutboxCommand(-1, 30))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] 음수 delaySeconds이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenDelaySecondsIsNegative() {
            // When & Then
            assertThatThrownBy(() -> new ProcessPendingSchedulerOutboxCommand(10, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("delaySeconds는 0 이상이어야 합니다");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] of()로 커맨드 생성")
        void shouldCreateWithFactoryMethod() {
            // When
            ProcessPendingSchedulerOutboxCommand command =
                    ProcessPendingSchedulerOutboxCommand.of(20, 60);

            // Then
            assertThat(command.batchSize()).isEqualTo(20);
            assertThat(command.delaySeconds()).isEqualTo(60);
        }
    }
}
