package com.ryuqq.crawlinghub.application.seller.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.in.command.DeactivateSchedulersBySellerUseCase;
import com.ryuqq.crawlinghub.application.seller.event.SellerDeactivatedEventHandler;
import com.ryuqq.crawlinghub.application.seller.metrics.SellerEventMetrics;
import com.ryuqq.crawlinghub.domain.seller.event.SellerDeActiveEvent;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
 * <p>Mockist 스타일 테스트: UseCase, Metrics Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SellerDeactivatedEventHandler 테스트")
class SellerDeactivatedEventHandlerTest {

    @Mock private DeactivateSchedulersBySellerUseCase deactivateSchedulersBySellerUseCase;

    @Mock private SellerEventMetrics metrics;

    @InjectMocks private SellerDeactivatedEventHandler handler;

    @Nested
    @DisplayName("handle() 테스트")
    class Handle {

        @Test
        @DisplayName("[성공] Seller 비활성화 이벤트 → 스케줄러 비활성화 및 메트릭 기록")
        void shouldDeactivateSchedulersAndRecordMetrics() {
            // Given
            Clock clock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneId.of("UTC"));
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SellerDeActiveEvent event = SellerDeActiveEvent.of(sellerId, clock);
            int deactivatedCount = 3;

            given(deactivateSchedulersBySellerUseCase.execute(sellerId.value()))
                    .willReturn(deactivatedCount);

            // Runnable 실행을 위한 doAnswer 설정
            doAnswer(
                            invocation -> {
                                Runnable runnable = invocation.getArgument(0);
                                runnable.run();
                                return null;
                            })
                    .when(metrics)
                    .recordDeactivationEvent(any(Runnable.class));

            // When
            handler.handle(event);

            // Then
            verify(metrics).recordDeactivationEvent(any(Runnable.class));
            verify(deactivateSchedulersBySellerUseCase).execute(sellerId.value());
            verify(metrics)
                    .recordSchedulersDeactivated(eq(sellerId.value()), eq((long) deactivatedCount));
        }

        @Test
        @DisplayName("[성공] 비활성화할 스케줄러가 없는 경우에도 정상 처리")
        void shouldHandleZeroSchedulers() {
            // Given
            Clock clock = Clock.fixed(Instant.parse("2025-01-15T10:00:00Z"), ZoneId.of("UTC"));
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SellerDeActiveEvent event = SellerDeActiveEvent.of(sellerId, clock);
            int deactivatedCount = 0;

            given(deactivateSchedulersBySellerUseCase.execute(sellerId.value()))
                    .willReturn(deactivatedCount);

            doAnswer(
                            invocation -> {
                                Runnable runnable = invocation.getArgument(0);
                                runnable.run();
                                return null;
                            })
                    .when(metrics)
                    .recordDeactivationEvent(any(Runnable.class));

            // When
            handler.handle(event);

            // Then
            verify(metrics).recordDeactivationEvent(any(Runnable.class));
            verify(deactivateSchedulersBySellerUseCase).execute(sellerId.value());
            verify(metrics).recordSchedulersDeactivated(eq(sellerId.value()), eq(0L));
        }
    }
}
