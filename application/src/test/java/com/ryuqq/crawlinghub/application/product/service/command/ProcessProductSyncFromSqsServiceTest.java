package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.internal.ProductSyncCoordinator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ProcessProductSyncFromSqsService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ProductSyncCoordinator 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessProductSyncFromSqsService 단위 테스트")
class ProcessProductSyncFromSqsServiceTest {

    @Mock private ProductSyncCoordinator productSyncCoordinator;

    @InjectMocks private ProcessProductSyncFromSqsService sut;

    @Nested
    @DisplayName("execute() 메서드 테스트")
    class ExecuteTest {

        @Test
        @DisplayName("[성공] 동기화 처리 성공 시 true 반환")
        void shouldReturnTrueWhenSyncSucceeds() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            given(productSyncCoordinator.processSyncRequest(command)).willReturn(true);

            // When
            boolean result = sut.execute(command);

            // Then
            assertThat(result).isTrue();
            then(productSyncCoordinator).should().processSyncRequest(command);
        }

        @Test
        @DisplayName("[실패] 동기화 처리 실패 시 false 반환")
        void shouldReturnFalseWhenSyncFails() {
            // Given
            ProcessProductSyncCommand command = createCommand();
            given(productSyncCoordinator.processSyncRequest(command)).willReturn(false);

            // When
            boolean result = sut.execute(command);

            // Then
            assertThat(result).isFalse();
            then(productSyncCoordinator).should().processSyncRequest(command);
        }

        @Test
        @DisplayName("[성공] Coordinator에 Command를 그대로 위임한다")
        void shouldDelegateCommandToCoordinator() {
            // Given
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(
                            10L, 20L, 100L, 12345L, "UPDATE_PRICE", 99999L, "idempotency-key-abc");
            given(productSyncCoordinator.processSyncRequest(command)).willReturn(true);

            // When
            sut.execute(command);

            // Then
            then(productSyncCoordinator).should().processSyncRequest(command);
        }
    }

    private ProcessProductSyncCommand createCommand() {
        return new ProcessProductSyncCommand(
                1L, 1L, 100L, 12345L, "CREATE", null, "idempotency-key-123");
    }
}
