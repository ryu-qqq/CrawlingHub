package com.ryuqq.crawlinghub.application.useragent.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.application.useragent.dto.command.RecoverLeakedUserAgentCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * RecoverLeakedUserAgentCommand 단위 테스트
 *
 * <p>커맨드 생성 유효성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("RecoverLeakedUserAgentCommand 테스트")
class RecoverLeakedUserAgentCommandTest {

    @Nested
    @DisplayName("생성자 유효성 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 양수 leakThresholdMillis로 생성")
        void shouldCreateWithPositiveThreshold() {
            // When
            RecoverLeakedUserAgentCommand command = new RecoverLeakedUserAgentCommand(30000L);

            // Then
            assertThat(command.leakThresholdMillis()).isEqualTo(30000L);
        }

        @Test
        @DisplayName("[실패] leakThresholdMillis가 0이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenThresholdIsZero() {
            // When & Then
            assertThatThrownBy(() -> new RecoverLeakedUserAgentCommand(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("leakThresholdMillis는 0보다 커야 합니다");
        }

        @Test
        @DisplayName("[실패] leakThresholdMillis가 음수이면 IllegalArgumentException 발생")
        void shouldThrowExceptionWhenThresholdIsNegative() {
            // When & Then
            assertThatThrownBy(() -> new RecoverLeakedUserAgentCommand(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("leakThresholdMillis는 0보다 커야 합니다");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class Of {

        @Test
        @DisplayName("[성공] of()로 커맨드 생성")
        void shouldCreateWithFactoryMethod() {
            // When
            RecoverLeakedUserAgentCommand command = RecoverLeakedUserAgentCommand.of(60000L);

            // Then
            assertThat(command.leakThresholdMillis()).isEqualTo(60000L);
        }

        @Test
        @DisplayName("[실패] of()에 0 전달 시 예외 발생")
        void shouldThrowExceptionWhenFactoryMethodReceivesZero() {
            // When & Then
            assertThatThrownBy(() -> RecoverLeakedUserAgentCommand.of(0L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
