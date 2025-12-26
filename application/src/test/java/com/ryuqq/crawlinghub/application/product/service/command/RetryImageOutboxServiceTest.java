package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.image.manager.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.dto.command.RetryImageOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.exception.ImageOutboxNotFoundException;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * RetryImageOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetryImageOutboxService 테스트")
class RetryImageOutboxServiceTest {

    private static final Long OUTBOX_ID = 1L;
    private static final Long IMAGE_ID = 100L;

    @Mock private ImageOutboxQueryPort imageOutboxQueryPort;
    @Mock private ProductImageOutboxTransactionManager outboxTransactionManager;

    private RetryImageOutboxService service;

    @BeforeEach
    void setUp() {
        service = new RetryImageOutboxService(imageOutboxQueryPort, outboxTransactionManager);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] FAILED 상태 Outbox → PENDING으로 변경")
        void shouldResetFailedOutboxToPending() {
            // Given
            RetryImageOutboxCommand command = new RetryImageOutboxCommand(OUTBOX_ID);
            ProductImageOutbox outbox = createOutbox(ProductOutboxStatus.FAILED);
            given(imageOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When
            OutboxRetryResponse response = service.execute(command);

            // Then
            assertThat(response.outboxId()).isEqualTo(OUTBOX_ID);
            assertThat(response.previousStatus()).isEqualTo("FAILED");
            assertThat(response.newStatus()).isEqualTo("PENDING");
            assertThat(response.message()).isEqualTo("재시도 요청이 등록되었습니다.");

            verify(outboxTransactionManager).resetToPending(outbox);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 Outbox → ImageOutboxNotFoundException")
        void shouldThrowExceptionWhenOutboxNotFound() {
            // Given
            RetryImageOutboxCommand command = new RetryImageOutboxCommand(999L);
            given(imageOutboxQueryPort.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(ImageOutboxNotFoundException.class)
                    .hasMessageContaining("존재하지 않는 ImageOutbox입니다");
        }

        @Test
        @DisplayName("[실패] PENDING 상태 Outbox → IllegalStateException")
        void shouldThrowExceptionWhenAlreadyPending() {
            // Given
            RetryImageOutboxCommand command = new RetryImageOutboxCommand(OUTBOX_ID);
            ProductImageOutbox outbox = createOutbox(ProductOutboxStatus.PENDING);
            given(imageOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 PENDING 상태입니다");
        }

        @Test
        @DisplayName("[실패] COMPLETED 상태 Outbox → IllegalStateException")
        void shouldThrowExceptionWhenAlreadyCompleted() {
            // Given
            RetryImageOutboxCommand command = new RetryImageOutboxCommand(OUTBOX_ID);
            ProductImageOutbox outbox = createOutbox(ProductOutboxStatus.COMPLETED);
            given(imageOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 완료된 Outbox는 재시도할 수 없습니다");
        }
    }

    private ProductImageOutbox createOutbox(ProductOutboxStatus status) {
        return ProductImageOutbox.reconstitute(
                OUTBOX_ID, IMAGE_ID, "idempotency-key-123", status, 0, null, Instant.now(), null);
    }
}
