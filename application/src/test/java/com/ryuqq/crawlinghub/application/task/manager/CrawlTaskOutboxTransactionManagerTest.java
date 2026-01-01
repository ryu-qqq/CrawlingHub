package com.ryuqq.crawlinghub.application.task.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.common.FixedClock;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.application.task.manager.command.CrawlTaskOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxTransactionManager 테스트")
class CrawlTaskOutboxTransactionManagerTest {

    @Mock private CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort;

    @Mock private ClockHolder clockHolder;

    @InjectMocks private CrawlTaskOutboxTransactionManager manager;

    @BeforeEach
    void setUp() {
        Clock fixedClock = FixedClock.aDefaultClock();
        org.mockito.Mockito.lenient().when(clockHolder.getClock()).thenReturn(fixedClock);
    }

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlTaskOutbox 저장")
        void shouldPersistOutbox() {
            // Given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            manager.persist(outbox);

            // Then
            verify(crawlTaskOutboxPersistencePort).persist(outbox);
        }
    }

    @Nested
    @DisplayName("markAsSent() 테스트")
    class MarkAsSent {

        @Test
        @DisplayName("[성공] Outbox 발행 성공 처리")
        void shouldMarkAsSentAndPersist() {
            // Given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            manager.markAsSent(outbox);

            // Then
            assertThat(outbox.isSent()).isTrue();
            verify(crawlTaskOutboxPersistencePort).persist(outbox);
        }
    }

    @Nested
    @DisplayName("markAsFailed() 테스트")
    class MarkAsFailed {

        @Test
        @DisplayName("[성공] Outbox 발행 실패 처리")
        void shouldMarkAsFailedAndPersist() {
            // Given
            CrawlTaskOutbox outbox = CrawlTaskOutboxFixture.aPendingOutbox();

            // When
            manager.markAsFailed(outbox);

            // Then
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
            verify(crawlTaskOutboxPersistencePort).persist(outbox);
        }
    }
}
