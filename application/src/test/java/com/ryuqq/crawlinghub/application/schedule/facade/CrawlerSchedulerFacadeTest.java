package com.ryuqq.crawlinghub.application.schedule.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.factory.command.CrawlSchedulerCommandFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerHistoryTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxTransactionManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerTransactionManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * CrawlerSchedulerFacade 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager, EventPublisher Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlerSchedulerFacade 테스트")
class CrawlerSchedulerFacadeTest {

    @Mock private CrawlSchedulerTransactionManager crawlerSchedulerManager;

    @Mock private CrawlSchedulerOutBoxTransactionManager crawlerSchedulerOutBoxManager;

    @Mock private CrawlSchedulerHistoryTransactionManager crawlerSchedulerHistoryManager;

    @Mock private CrawlSchedulerCommandFactory commandFactory;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private TimeProvider timeProvider;

    @InjectMocks private CrawlerSchedulerFacade facade;

    private java.time.Instant fixedInstant;

    @BeforeEach
    void setUp() {
        fixedInstant = FixedClock.aDefaultClock().instant();
        org.mockito.Mockito.lenient().when(timeProvider.now()).thenReturn(fixedInstant);
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlSchedulerBundle 저장 및 이벤트 발행 → CrawlScheduler 반환")
        void shouldPersistBundleAndPublishEvents() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, "{\"payload\": \"test\"}");
            CrawlSchedulerId expectedSchedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId expectedHistoryId =
                    CrawlSchedulerHistoryIdFixture.anAssignedId();

            given(crawlerSchedulerManager.persist(scheduler)).willReturn(expectedSchedulerId);
            given(crawlerSchedulerHistoryManager.persist(any(CrawlSchedulerHistory.class)))
                    .willReturn(expectedHistoryId);

            // When
            CrawlScheduler result = facade.persist(bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlSchedulerIdValue()).isEqualTo(expectedSchedulerId.value());
            verify(crawlerSchedulerManager).persist(scheduler);
            verify(crawlerSchedulerHistoryManager).persist(any(CrawlSchedulerHistory.class));
            verify(crawlerSchedulerOutBoxManager).persist(any(CrawlSchedulerOutBox.class));
        }
    }

    @Nested
    @DisplayName("update() 테스트")
    class Update {

        @Test
        @DisplayName("[성공] 이벤트 없는 스케줄러 업데이트")
        void shouldUpdateWithoutEvents() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            // 이벤트 클리어 (없는 상태로 시작)
            scheduler.pollEvents();

            // When
            facade.update(scheduler);

            // Then
            verify(crawlerSchedulerManager).persist(scheduler);
        }
    }
}
