package com.ryuqq.crawlinghub.application.execution.dto.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ExecuteCrawlTaskCommand 단위 테스트
 *
 * <p>Compact Constructor 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ExecuteCrawlTaskCommand 테스트")
class ExecuteCrawlTaskCommandTest {

    @Nested
    @DisplayName("생성자 검증 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 2L, 3L, "PRODUCT", "https://example.com");

            assertThat(command.taskId()).isEqualTo(1L);
            assertThat(command.schedulerId()).isEqualTo(2L);
            assertThat(command.sellerId()).isEqualTo(3L);
            assertThat(command.taskType()).isEqualTo("PRODUCT");
            assertThat(command.endpoint()).isEqualTo("https://example.com");
        }

        @Test
        @DisplayName("[실패] taskId가 null이면 예외")
        void shouldThrowWhenTaskIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ExecuteCrawlTaskCommand(
                                            null, 2L, 3L, "PRODUCT", "https://example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("taskId");
        }

        @Test
        @DisplayName("[실패] schedulerId가 null이면 예외")
        void shouldThrowWhenSchedulerIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ExecuteCrawlTaskCommand(
                                            1L, null, 3L, "PRODUCT", "https://example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("schedulerId");
        }

        @Test
        @DisplayName("[실패] sellerId가 null이면 예외")
        void shouldThrowWhenSellerIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ExecuteCrawlTaskCommand(
                                            1L, 2L, null, "PRODUCT", "https://example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("[실패] taskType이 null이면 예외")
        void shouldThrowWhenTaskTypeIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ExecuteCrawlTaskCommand(
                                            1L, 2L, 3L, null, "https://example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("taskType");
        }

        @Test
        @DisplayName("[실패] taskType이 빈 문자열이면 예외")
        void shouldThrowWhenTaskTypeIsBlank() {
            assertThatThrownBy(
                            () ->
                                    new ExecuteCrawlTaskCommand(
                                            1L, 2L, 3L, "  ", "https://example.com"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("[실패] endpoint가 null이면 예외")
        void shouldThrowWhenEndpointIsNull() {
            assertThatThrownBy(() -> new ExecuteCrawlTaskCommand(1L, 2L, 3L, "PRODUCT", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("endpoint");
        }

        @Test
        @DisplayName("[실패] endpoint가 빈 문자열이면 예외")
        void shouldThrowWhenEndpointIsBlank() {
            assertThatThrownBy(() -> new ExecuteCrawlTaskCommand(1L, 2L, 3L, "PRODUCT", ""))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
