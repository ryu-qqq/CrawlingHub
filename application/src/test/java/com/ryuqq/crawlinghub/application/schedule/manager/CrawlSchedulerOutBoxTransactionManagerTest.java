package com.ryuqq.crawlinghub.application.schedule.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerOutBoxIdFixture;
import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.schedule.port.out.command.PersistCrawlScheduleOutBoxPort;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlSchedulerOutBox;
import com.ryuqq.crawlinghub.domain.schedule.id.CrawlSchedulerOutBoxId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerOutBoxTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerOutBoxTransactionManager 테스트")
class CrawlSchedulerOutBoxTransactionManagerTest {

    @Mock private PersistCrawlScheduleOutBoxPort persistCrawlScheduleOutBoxPort;

    @Mock private TimeProvider timeProvider;

    @InjectMocks private CrawlSchedulerOutBoxTransactionManager manager;

    @BeforeEach
    void setUp() {
        java.time.Instant fixedInstant = FixedClock.aDefaultClock().instant();
        lenient().when(timeProvider.now()).thenReturn(fixedInstant);
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlSchedulerOutBox 저장 → ID 반환")
        void shouldPersistOutBoxAndReturnId() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            CrawlSchedulerOutBoxId expectedId = CrawlSchedulerOutBoxIdFixture.anAssignedId();

            given(persistCrawlScheduleOutBoxPort.persist(outBox)).willReturn(expectedId);

            // When
            CrawlSchedulerOutBoxId result = manager.persist(outBox);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(persistCrawlScheduleOutBoxPort).persist(outBox);
        }
    }

    @Nested
    @DisplayName("markAsCompleted() 테스트")
    class MarkAsCompleted {

        @Test
        @DisplayName("[성공] OutBox 완료 처리 및 저장")
        void shouldMarkAsCompletedAndPersist() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();

            // When
            manager.markAsCompleted(outBox);

            // Then
            assertThat(outBox.isCompleted()).isTrue();
            verify(persistCrawlScheduleOutBoxPort).persist(outBox);
        }
    }

    @Nested
    @DisplayName("markAsFailed() 테스트")
    class MarkAsFailed {

        @Test
        @DisplayName("[성공] OutBox 실패 처리 및 저장")
        void shouldMarkAsFailedAndPersist() {
            // Given
            CrawlSchedulerOutBox outBox = CrawlSchedulerOutBoxFixture.aPendingOutBox();
            String errorMessage = "EventBridge 동기화 실패";

            // When
            manager.markAsFailed(outBox, errorMessage);

            // Then
            assertThat(outBox.isFailed()).isTrue();
            verify(persistCrawlScheduleOutBoxPort).persist(outBox);
        }
    }
}
