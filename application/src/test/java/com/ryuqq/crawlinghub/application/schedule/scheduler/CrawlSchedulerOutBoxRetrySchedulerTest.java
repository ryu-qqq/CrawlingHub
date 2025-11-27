package com.ryuqq.crawlinghub.application.schedule.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerOutBoxManager;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxRetryScheduler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port, Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutBoxRetryScheduler 테스트")
class CrawlSchedulerOutBoxRetrySchedulerTest {

    @Mock private CrawlSchedulerOutBoxQueryPort outBoxQueryPort;

    @Mock private CrawlerSchedulerOutBoxManager outBoxManager;

    @Mock private EventBridgeClientPort eventBridgeClientPort;

    @InjectMocks private CrawlSchedulerOutBoxRetryScheduler scheduler;

    @Nested
    @DisplayName("processOutbox() 테스트")
    class ProcessOutBox {

        @Test
        @DisplayName("[성공] PENDING/FAILED Outbox 재처리 → 성공 시 COMPLETED 상태로 변경")
        void shouldProcessOutboxAndMarkCompleted() {
            // Given
            CrawlSchedulerOutBox outBox1 = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBox outBox2 = CrawlSchedulerOutBoxFixture.aFailedOutBox();
            List<CrawlSchedulerOutBox> outBoxes = List.of(outBox1, outBox2);

            given(outBoxQueryPort.findPendingOrFailed(anyInt())).willReturn(outBoxes);

            // When
            scheduler.processOutbox();

            // Then
            verify(outBoxQueryPort).findPendingOrFailed(100);
            verify(eventBridgeClientPort, times(2)).syncFromOutBox(any());
            verify(outBoxManager, times(2)).markAsCompleted(any());
            verify(outBoxManager, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("[성공] 처리할 Outbox가 없는 경우 → 아무 작업 안함")
        void shouldDoNothingWhenNoOutboxFound() {
            // Given
            given(outBoxQueryPort.findPendingOrFailed(anyInt()))
                    .willReturn(Collections.emptyList());

            // When
            scheduler.processOutbox();

            // Then
            verify(outBoxQueryPort).findPendingOrFailed(100);
            verify(eventBridgeClientPort, never()).syncFromOutBox(any());
            verify(outBoxManager, never()).markAsCompleted(any());
            verify(outBoxManager, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("[실패] EventBridge 호출 예외 → Outbox FAILED 상태로 변경")
        void shouldMarkAsFailedWhenEventBridgeFails() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            List<CrawlSchedulerOutBox> outBoxes = List.of(outBox);

            given(outBoxQueryPort.findPendingOrFailed(anyInt())).willReturn(outBoxes);
            doThrow(new RuntimeException("EventBridge error"))
                    .when(eventBridgeClientPort)
                    .syncFromOutBox(outBox);

            // When
            scheduler.processOutbox();

            // Then
            verify(outBoxQueryPort).findPendingOrFailed(100);
            verify(eventBridgeClientPort).syncFromOutBox(outBox);
            verify(outBoxManager, never()).markAsCompleted(any());
            verify(outBoxManager).markAsFailed(outBox, "EventBridge error");
        }
    }
}
