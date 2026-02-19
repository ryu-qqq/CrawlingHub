package com.ryuqq.crawlinghub.application.seller.listener;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.application.seller.event.SellerDeactivatedEventHandler;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SellerDeactivatedEventHandler 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerDeactivatedEventHandler 테스트")
class SellerDeactivatedEventHandlerTest {

    @Mock private DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase;

    @InjectMocks private SellerDeactivatedEventHandler handler;

    @Nested
    @DisplayName("handle() 테스트")
    class Handle {

        @Test
        @DisplayName("[성공] Seller 비활성화 이벤트 → 스케줄러 비활성화")
        void shouldDeactivateSchedulers() {
            // Given
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SellerDeActiveEvent event =
                    SellerDeActiveEvent.of(sellerId, Instant.parse("2025-01-15T10:00:00Z"));
            int deactivatedCount = 3;

            given(deactivateSchedulersBySellerUseCase.execute(sellerId.value()))
                    .willReturn(deactivatedCount);

            // When
            handler.handle(event);

            // Then
            verify(deactivateSchedulersBySellerUseCase).execute(sellerId.value());
        }

        @Test
        @DisplayName("[성공] 비활성화할 스케줄러가 없는 경우에도 정상 처리")
        void shouldHandleZeroSchedulers() {
            // Given
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SellerDeActiveEvent event =
                    SellerDeActiveEvent.of(sellerId, Instant.parse("2025-01-15T10:00:00Z"));

            given(deactivateSchedulersBySellerUseCase.execute(sellerId.value())).willReturn(0);

            // When
            handler.handle(event);

            // Then
            verify(deactivateSchedulersBySellerUseCase).execute(sellerId.value());
        }
    }
}
