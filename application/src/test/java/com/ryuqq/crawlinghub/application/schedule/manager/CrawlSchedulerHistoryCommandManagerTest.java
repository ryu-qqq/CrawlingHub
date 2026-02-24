package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerHistoryIdFixture;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.CrawlScheduleHistoryCommandPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerHistory;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerHistoryId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerHistoryCommandManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: CrawlScheduleHistoryCommandPort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerHistoryCommandManager 테스트")
class CrawlSchedulerHistoryCommandManagerTest {

    @Mock private CrawlScheduleHistoryCommandPort crawlScheduleHistoryCommandPort;

    @InjectMocks private CrawlSchedulerHistoryCommandManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] 스케줄러 히스토리 저장 후 ID 반환")
        void shouldPersistHistoryAndReturnId() {
            // Given
            CrawlSchedulerHistory history = org.mockito.Mockito.mock(CrawlSchedulerHistory.class);
            CrawlSchedulerHistoryId expectedId = CrawlSchedulerHistoryIdFixture.anAssignedId();
            given(crawlScheduleHistoryCommandPort.persist(history)).willReturn(expectedId);

            // When
            CrawlSchedulerHistoryId result = manager.persist(history);

            // Then
            assertThat(result).isEqualTo(expectedId);
            then(crawlScheduleHistoryCommandPort).should().persist(history);
        }

        @Test
        @DisplayName("[성공] 다른 ID 값으로 저장 성공")
        void shouldPersistHistoryWithDifferentId() {
            // Given
            CrawlSchedulerHistory history = org.mockito.Mockito.mock(CrawlSchedulerHistory.class);
            CrawlSchedulerHistoryId expectedId = CrawlSchedulerHistoryIdFixture.anAssignedId(99L);
            given(crawlScheduleHistoryCommandPort.persist(history)).willReturn(expectedId);

            // When
            CrawlSchedulerHistoryId result = manager.persist(history);

            // Then
            assertThat(result.value()).isEqualTo(99L);
        }
    }
}
