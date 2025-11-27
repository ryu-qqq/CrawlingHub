package com.ryuqq.crawlinghub.application.schedule.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerHistoryManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlerSchedulerOutBoxManager;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerHistoryId;
import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
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

    @Mock
    private CrawlerSchedulerManager crawlerSchedulerManager;

    @Mock
    private CrawlerSchedulerOutBoxManager crawlerSchedulerOutBoxManager;

    @Mock
    private CrawlerSchedulerHistoryManager crawlerSchedulerHistoryManager;

    @Mock
    private CrawlSchedulerAssembler crawlSchedulerAssembler;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ClockHolder clockHolder;

    @InjectMocks
    private CrawlerSchedulerFacade facade;

    private FixedClock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = FixedClock.aDefaultClock();
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlSchedulerBundle 저장 및 이벤트 발행")
        void shouldPersistBundleAndPublishEvents() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle =
                    CrawlSchedulerBundle.of(scheduler, "{\"payload\": \"test\"}", fixedClock);
            CrawlSchedulerId expectedSchedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId expectedHistoryId = CrawlSchedulerHistoryIdFixture.anAssignedId();

            given(crawlerSchedulerManager.persist(scheduler)).willReturn(expectedSchedulerId);
            given(crawlerSchedulerHistoryManager.persist(any(CrawlSchedulerHistory.class)))
                    .willReturn(expectedHistoryId);

            // When
            CrawlSchedulerBundle result = facade.persist(bundle);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSavedSchedulerId()).isEqualTo(expectedSchedulerId);
            assertThat(result.getSavedHistoryId()).isEqualTo(expectedHistoryId);
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
            scheduler.clearDomainEvents();

            // When
            facade.update(scheduler);

            // Then
            verify(crawlerSchedulerManager).persist(scheduler);
        }
    }
}
