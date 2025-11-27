package com.ryuqq.crawlinghub.application.task.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlEndpoint;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlEndpointFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskTypeFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskRegisteredEventListener 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager, Port Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskRegisteredEventListener 테스트")
class CrawlTaskRegisteredEventListenerTest {

    @Mock
    private CrawlTaskMessageManager crawlTaskMessageManager;

    @Mock
    private CrawlTaskOutboxQueryPort crawlTaskOutboxQueryPort;

    @Mock
    private CrawlTaskOutboxTransactionManager crawlTaskOutboxTransactionManager;

    @InjectMocks
    private CrawlTaskRegisteredEventListener listener;

    @Nested
    @DisplayName("handleCrawlTaskRegistered() 테스트")
    class HandleCrawlTaskRegistered {

        @Test
        @DisplayName("[성공] SQS 발행 → Outbox SENT 상태로 변경")
        void shouldPublishToSqsAndMarkAsSent() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlTaskType taskType = CrawlTaskTypeFixture.defaultType();
            CrawlEndpoint endpoint = CrawlEndpointFixture.aMiniShopListEndpoint();
            String payload = "{\"payload\":\"test\"}";

            CrawlTaskRegisteredEvent event = CrawlTaskRegisteredEvent.of(
                    taskId, schedulerId, sellerId, taskType, endpoint, payload);

            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            given(crawlTaskOutboxQueryPort.findByCrawlTaskId(taskId)).willReturn(Optional.of(outbox));

            // When
            listener.handleCrawlTaskRegistered(event);

            // Then
            verify(crawlTaskOutboxQueryPort).findByCrawlTaskId(taskId);
            verify(crawlTaskMessageManager).publishFromEvent(event);
            verify(crawlTaskOutboxTransactionManager).markAsSent(outbox);
            verify(crawlTaskOutboxTransactionManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("[실패] Outbox 미존재 → 아무 작업 안함")
        void shouldDoNothingWhenOutboxNotFound() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlTaskType taskType = CrawlTaskTypeFixture.defaultType();
            CrawlEndpoint endpoint = CrawlEndpointFixture.aMiniShopListEndpoint();
            String payload = "{\"payload\":\"test\"}";

            CrawlTaskRegisteredEvent event = CrawlTaskRegisteredEvent.of(
                    taskId, schedulerId, sellerId, taskType, endpoint, payload);

            given(crawlTaskOutboxQueryPort.findByCrawlTaskId(taskId)).willReturn(Optional.empty());

            // When
            listener.handleCrawlTaskRegistered(event);

            // Then
            verify(crawlTaskOutboxQueryPort).findByCrawlTaskId(taskId);
            verify(crawlTaskMessageManager, never()).publishFromEvent(any());
            verify(crawlTaskOutboxTransactionManager, never()).markAsSent(any());
            verify(crawlTaskOutboxTransactionManager, never()).markAsFailed(any());
        }

        @Test
        @DisplayName("[실패] SQS 발행 예외 → Outbox FAILED 상태로 변경")
        void shouldMarkAsFailedWhenSqsPublishFails() {
            // Given
            CrawlTaskId taskId = CrawlTaskIdFixture.anAssignedId();
            CrawlSchedulerId schedulerId = CrawlSchedulerIdFixture.anAssignedId();
            SellerId sellerId = SellerIdFixture.anAssignedId();
            CrawlTaskType taskType = CrawlTaskTypeFixture.defaultType();
            CrawlEndpoint endpoint = CrawlEndpointFixture.aMiniShopListEndpoint();
            String payload = "{\"payload\":\"test\"}";

            CrawlTaskRegisteredEvent event = CrawlTaskRegisteredEvent.of(
                    taskId, schedulerId, sellerId, taskType, endpoint, payload);

            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            given(crawlTaskOutboxQueryPort.findByCrawlTaskId(taskId)).willReturn(Optional.of(outbox));
            doThrow(new RuntimeException("SQS publish error"))
                    .when(crawlTaskMessageManager).publishFromEvent(event);

            // When
            listener.handleCrawlTaskRegistered(event);

            // Then
            verify(crawlTaskOutboxQueryPort).findByCrawlTaskId(taskId);
            verify(crawlTaskMessageManager).publishFromEvent(event);
            verify(crawlTaskOutboxTransactionManager, never()).markAsSent(any());
            verify(crawlTaskOutboxTransactionManager).markAsFailed(outbox);
        }
    }
}
