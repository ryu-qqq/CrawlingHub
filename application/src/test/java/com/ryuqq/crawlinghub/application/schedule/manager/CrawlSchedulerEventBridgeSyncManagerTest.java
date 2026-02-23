package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.client.EventBridgeClientPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerEventBridgeSyncManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: EventBridgeClientPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerEventBridgeSyncManager 테스트")
class CrawlSchedulerEventBridgeSyncManagerTest {

    @Mock private EventBridgeClientPort eventBridgeClientPort;

    @InjectMocks private CrawlSchedulerEventBridgeSyncManager manager;

    @Nested
    @DisplayName("syncFromOutBox() 테스트")
    class SyncFromOutBox {

        @Test
        @DisplayName("[성공] 아웃박스를 EventBridge에 동기화")
        void shouldSyncOutBoxToEventBridge() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // When
            manager.syncFromOutBox(outBox);

            // Then
            then(eventBridgeClientPort).should().syncFromOutBox(outBox);
        }

        @Test
        @DisplayName("[성공] 완료된 아웃박스도 동기화 가능")
        void shouldSyncCompletedOutBoxToEventBridge() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aCompletedOutBox();

            // When
            manager.syncFromOutBox(outBox);

            // Then
            then(eventBridgeClientPort).should().syncFromOutBox(outBox);
        }
    }
}
