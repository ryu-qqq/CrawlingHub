package com.ryuqq.crawlinghub.application.product.service.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.manager.SyncOutboxManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox.SyncType;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
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
 * RetrySyncOutboxService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RetrySyncOutboxService 테스트")
class RetrySyncOutboxServiceTest {

    private static final Long OUTBOX_ID = 1L;
    private static final CrawledProductId PRODUCT_ID = CrawledProductId.of(100L);
    private static final SellerId SELLER_ID = SellerId.of(200L);

    @Mock private SyncOutboxQueryPort syncOutboxQueryPort;
    @Mock private SyncOutboxManager syncOutboxManager;

    private RetrySyncOutboxService service;

    @BeforeEach
    void setUp() {
        service = new RetrySyncOutboxService(syncOutboxQueryPort, syncOutboxManager);
    }

    @Nested
    @DisplayName("execute() 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] FAILED 상태 Outbox → PENDING으로 변경")
        void shouldResetFailedOutboxToPending() {
            // Given
            RetrySyncOutboxCommand command = new RetrySyncOutboxCommand(OUTBOX_ID);
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.FAILED);
            given(syncOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When
            OutboxRetryResponse response = service.execute(command);

            // Then
            assertThat(response.outboxId()).isEqualTo(OUTBOX_ID);
            assertThat(response.previousStatus()).isEqualTo("FAILED");
            assertThat(response.newStatus()).isEqualTo("PENDING");
            assertThat(response.message()).isEqualTo("재시도 요청이 등록되었습니다.");

            verify(syncOutboxManager).resetToPending(outbox);
        }

        @Test
        @DisplayName("[실패] 존재하지 않는 Outbox → IllegalArgumentException")
        void shouldThrowExceptionWhenOutboxNotFound() {
            // Given
            RetrySyncOutboxCommand command = new RetrySyncOutboxCommand(999L);
            given(syncOutboxQueryPort.findById(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SyncOutbox를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("[실패] PENDING 상태 Outbox → IllegalStateException")
        void shouldThrowExceptionWhenAlreadyPending() {
            // Given
            RetrySyncOutboxCommand command = new RetrySyncOutboxCommand(OUTBOX_ID);
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.PENDING);
            given(syncOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 PENDING 상태입니다");
        }

        @Test
        @DisplayName("[실패] COMPLETED 상태 Outbox → IllegalStateException")
        void shouldThrowExceptionWhenAlreadyCompleted() {
            // Given
            RetrySyncOutboxCommand command = new RetrySyncOutboxCommand(OUTBOX_ID);
            CrawledProductSyncOutbox outbox = createOutbox(ProductOutboxStatus.COMPLETED);
            given(syncOutboxQueryPort.findById(OUTBOX_ID)).willReturn(Optional.of(outbox));

            // When & Then
            assertThatThrownBy(() -> service.execute(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("이미 완료된 Outbox는 재시도할 수 없습니다");
        }
    }

    private CrawledProductSyncOutbox createOutbox(ProductOutboxStatus status) {
        return CrawledProductSyncOutbox.reconstitute(
                OUTBOX_ID,
                PRODUCT_ID,
                SELLER_ID,
                12345L,
                SyncType.CREATE,
                "idempotency-key-123",
                null,
                status,
                0,
                null,
                Instant.now(),
                null);
    }
}
