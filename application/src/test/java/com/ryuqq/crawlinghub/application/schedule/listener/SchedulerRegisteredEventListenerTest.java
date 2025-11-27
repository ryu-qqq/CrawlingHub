package com.ryuqq.crawlinghub.application.schedule.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CronExpressionFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.SchedulerNameFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerOutBoxManager;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.application.schedule.port.out.query.CrawlSchedulerOutBoxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.event.SchedulerRegisteredEvent;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CronExpression;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerName;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SchedulerRegisteredEventListener 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port, Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulerRegisteredEventListener 테스트")
class SchedulerRegisteredEventListenerTest {

    @Mock private EventBridgeClientPort eventBridgeClientPort;

    @Mock private CrawlSchedulerOutBoxQueryPort outBoxQueryPort;

    @Mock private CrawlerSchedulerOutBoxManager outBoxManager;

    @InjectMocks private SchedulerRegisteredEventListener listener;

    @Nested
    @DisplayName("handleSchedulerRegistered() 테스트")
    class HandleSchedulerRegistered {

        @Test
        @DisplayName("[성공] EventBridge 생성 → Outbox COMPLETED 상태로 변경")
        void shouldCreateEventBridgeAndMarkCompleted() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            SchedulerRegisteredEvent event =
                    new SchedulerRegisteredEvent(
                            schedulerId, historyId, sellerId, schedulerName, cronExpression);

            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            given(outBoxQueryPort.findByHistoryId(historyId)).willReturn(Optional.of(outBox));

            // When
            listener.handleSchedulerRegistered(event);

            // Then
            verify(outBoxQueryPort).findByHistoryId(historyId);
            verify(eventBridgeClientPort).createScheduler(event);
            verify(outBoxManager).markAsCompleted(outBox);
            verify(outBoxManager, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("[실패] Outbox 미존재 → 아무 작업 안함")
        void shouldDoNothingWhenOutboxNotFound() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            SchedulerRegisteredEvent event =
                    new SchedulerRegisteredEvent(
                            schedulerId, historyId, sellerId, schedulerName, cronExpression);

            given(outBoxQueryPort.findByHistoryId(historyId)).willReturn(Optional.empty());

            // When
            listener.handleSchedulerRegistered(event);

            // Then
            verify(outBoxQueryPort).findByHistoryId(historyId);
            verify(eventBridgeClientPort, never()).createScheduler(any());
            verify(outBoxManager, never()).markAsCompleted(any());
            verify(outBoxManager, never()).markAsFailed(any(), anyString());
        }

        @Test
        @DisplayName("[실패] EventBridge 호출 예외 → Outbox FAILED 상태로 변경")
        void shouldMarkAsFailedWhenEventBridgeFails() {
            // Given
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            SchedulerName schedulerName = SchedulerNameFixture.aDefaultName();
            CronExpression cronExpression = CronExpressionFixture.aDefaultCron();

            SchedulerRegisteredEvent event =
                    new SchedulerRegisteredEvent(
                            schedulerId, historyId, sellerId, schedulerName, cronExpression);

            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            given(outBoxQueryPort.findByHistoryId(historyId)).willReturn(Optional.of(outBox));
            doThrow(new RuntimeException("AWS EventBridge error"))
                    .when(eventBridgeClientPort)
                    .createScheduler(event);

            // When
            listener.handleSchedulerRegistered(event);

            // Then
            verify(outBoxQueryPort).findByHistoryId(historyId);
            verify(eventBridgeClientPort).createScheduler(event);
            verify(outBoxManager, never()).markAsCompleted(any());
            verify(outBoxManager).markAsFailed(outBox, "AWS EventBridge error");
        }
    }
}
