package com.ryuqq.crawlinghub.application.schedule.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerEventBridgeSyncManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxProcessor 단위 테스트
 *
 * <p>Mockist 스타일 테스트: EventBridgeSyncManager, OutBoxCommandManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutBoxProcessor 테스트")
class CrawlSchedulerOutBoxProcessorTest {

    @Mock private CrawlSchedulerEventBridgeSyncManager eventBridgeSyncManager;

    @Mock private CrawlSchedulerOutBoxCommandManager outBoxCommandManager;

    @InjectMocks private CrawlSchedulerOutBoxProcessor processor;

    @Nested
    @DisplayName("processOutbox() 아웃박스 처리 테스트")
    class ProcessOutbox {

        @Test
        @DisplayName("[성공] 정상 처리 시 true 반환 및 COMPLETED 상태 저장")
        void shouldReturnTrueAndPersistCompletedWhenProcessedSuccessfully() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            given(outBoxCommandManager.persist(outBox)).willReturn(CrawlSchedulerOutBoxId.of(1L));

            // When
            boolean result = processor.processOutbox(outBox);

            // Then
            assertThat(result).isTrue();
            // PROCESSING → COMPLETED 상태로 2번 persist 호출
            then(outBoxCommandManager).should(times(2)).persist(outBox);
            then(eventBridgeSyncManager).should().syncFromOutBox(outBox);
            // 최종 상태는 COMPLETED
            assertThat(outBox.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.COMPLETED);
        }

        @Test
        @DisplayName("[실패] EventBridge 동기화 실패 시 false 반환 및 FAILED 상태 저장")
        void shouldReturnFalseAndPersistFailedWhenEventBridgeFails() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            given(outBoxCommandManager.persist(outBox)).willReturn(CrawlSchedulerOutBoxId.of(1L));
            willThrow(new RuntimeException("EventBridge 연결 실패"))
                    .given(eventBridgeSyncManager)
                    .syncFromOutBox(outBox);

            // When
            boolean result = processor.processOutbox(outBox);

            // Then
            assertThat(result).isFalse();
            // PROCESSING 전환 후 FAILED 전환으로 총 2번 persist
            then(outBoxCommandManager).should(times(2)).persist(outBox);
            assertThat(outBox.getStatus()).isEqualTo(CrawlSchedulerOubBoxStatus.FAILED);
        }

        @Test
        @DisplayName("[실패] 첫 번째 persist 실패 시 catch 블록에서도 persist 시도하고 false 반환")
        void shouldReturnFalseWhenFirstPersistFails() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            // 첫 번째 persist(PROCESSING 전환) 실패 → catch 블록에서 FAILED 전환 후 persist 재시도
            // catch 블록의 persist도 실패하면 예외가 전파되지만, 테스트에서는 첫 호출만 throw
            given(outBoxCommandManager.persist(outBox))
                    .willThrow(new RuntimeException("DB 저장 실패"))
                    .willReturn(null);

            // When
            boolean result = processor.processOutbox(outBox);

            // Then
            assertThat(result).isFalse();
            // PROCESSING 전환 persist 실패 → catch 블록에서 FAILED 전환 persist 1번 더 시도
            then(outBoxCommandManager).should(times(2)).persist(outBox);
        }
    }
}
