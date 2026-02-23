package com.ryuqq.crawlinghub.application.task.manager;

import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskOutboxFixture;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskOutboxPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskOutboxCommandManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskOutboxCommandManager 테스트")
class CrawlTaskOutboxCommandManagerTest {

    @Mock private CrawlTaskOutboxPersistencePort crawlTaskOutboxPersistencePort;

    @InjectMocks private CrawlTaskOutboxCommandManager manager;

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
}
