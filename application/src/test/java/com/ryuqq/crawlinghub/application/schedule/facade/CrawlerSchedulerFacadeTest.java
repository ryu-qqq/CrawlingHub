package com.ryuqq.crawlinghub.application.schedule.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerIdFixture;
import com.ryuqq.crawlinghub.application.schedule.dto.bundle.CrawlSchedulerBundle;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerHistoryCommandManager;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerOutBoxCommandManager;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlerSchedulerFacade 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlerSchedulerFacade 테스트")
class CrawlerSchedulerFacadeTest {

    @Mock private CrawlSchedulerCommandManager crawlerSchedulerManager;

    @Mock private CrawlSchedulerOutBoxCommandManager crawlerSchedulerOutBoxManager;

    @Mock private CrawlSchedulerHistoryCommandManager crawlerSchedulerHistoryManager;

    @InjectMocks private CrawlerSchedulerFacade facade;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlSchedulerBundle 저장 → CrawlSchedulerId 반환")
        void shouldPersistBundleAndReturnSchedulerId() {
            // Given
            Instant fixedInstant = FixedClock.aDefaultClock().instant();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerBundle bundle = CrawlSchedulerBundle.of(scheduler, fixedInstant);
            CrawlSchedulerId expectedSchedulerId = CrawlSchedulerIdFixture.anAssignedId();
            CrawlSchedulerHistoryId expectedHistoryId =
                    CrawlSchedulerHistoryIdFixture.anAssignedId();

            given(crawlerSchedulerManager.persist(scheduler)).willReturn(expectedSchedulerId);
            given(crawlerSchedulerHistoryManager.persist(any(CrawlSchedulerHistory.class)))
                    .willReturn(expectedHistoryId);

            // When
            CrawlSchedulerId result = facade.persist(bundle);

            // Then
            assertThat(result).isEqualTo(expectedSchedulerId);
            verify(crawlerSchedulerManager).persist(scheduler);
            verify(crawlerSchedulerHistoryManager).persist(any(CrawlSchedulerHistory.class));
            verify(crawlerSchedulerOutBoxManager).persist(any(CrawlSchedulerOutBox.class));
        }
    }

    @Nested
    @DisplayName("update() 테스트")
    class Update {

        @Test
        @DisplayName("[성공] 스케줄러 업데이트 + 히스토리/아웃박스 저장")
        void shouldUpdateWithHistoryAndOutbox() {
            // Given
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();
            CrawlSchedulerHistoryId historyId = CrawlSchedulerHistoryIdFixture.anAssignedId();

            given(crawlerSchedulerHistoryManager.persist(any(CrawlSchedulerHistory.class)))
                    .willReturn(historyId);

            // When
            facade.update(scheduler);

            // Then
            verify(crawlerSchedulerManager).persist(scheduler);
            verify(crawlerSchedulerHistoryManager).persist(any(CrawlSchedulerHistory.class));
            verify(crawlerSchedulerOutBoxManager).persist(any(CrawlSchedulerOutBox.class));
        }
    }
}
