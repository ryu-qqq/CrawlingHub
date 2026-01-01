package com.ryuqq.crawlinghub.application.task.manager;

import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlEndpointFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskTypeFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.seller.SellerIdFixture;
import com.ryuqq.crawlinghub.application.task.manager.messaging.CrawlTaskMessageManager;
import com.ryuqq.crawlinghub.application.task.port.out.messaging.CrawlTaskMessagePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.event.CrawlTaskRegisteredEvent;
import java.time.Clock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskMessageManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: MessagePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskMessageManager 테스트")
class CrawlTaskMessageManagerTest {

    @Mock private CrawlTaskMessagePort crawlTaskMessagePort;

    @InjectMocks private CrawlTaskMessageManager manager;

    private static final Clock FIXED_CLOCK = FixedClock.aDefaultClock();

    @Nested
    @DisplayName("publishFromEvent() 테스트")
    class PublishFromEvent {

        @Test
        @DisplayName("[성공] CrawlTaskRegisteredEvent 기반 메시지 발행")
        void shouldPublishFromEvent() {
            // Given
            CrawlTaskRegisteredEvent event =
                    CrawlTaskRegisteredEvent.of(
                            CrawlTaskIdFixture.anAssignedId(),
                            CrawlSchedulerIdFixture.anAssignedId(),
                            SellerIdFixture.anAssignedId(),
                            CrawlTaskTypeFixture.defaultType(),
                            CrawlEndpointFixture.aMiniShopListEndpoint(),
                            "{\"payload\": \"test\"}",
                            FIXED_CLOCK);

            // When
            manager.publishFromEvent(event);

            // Then
            verify(crawlTaskMessagePort).publishFromEvent(event);
        }
    }

    @Nested
    @DisplayName("publishFromOutbox() 테스트")
    class PublishFromOutbox {

        @Test
        @DisplayName("[성공] Outbox 기반 메시지 발행 (재시도)")
        void shouldPublishFromOutbox() {
            // Given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            manager.publishFromOutbox(outbox);

            // Then
            verify(crawlTaskMessagePort).publishFromOutbox(outbox);
        }
    }

    @Nested
    @DisplayName("publish() 테스트")
    class Publish {

        @Test
        @DisplayName("[성공] CrawlTask 직접 발행")
        void shouldPublishDirectly() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            String idempotencyKey = "unique-key-12345";

            // When
            manager.publish(task, idempotencyKey);

            // Then
            verify(crawlTaskMessagePort).publish(task, idempotencyKey);
        }
    }
}
