package com.ryuqq.crawlinghub.application.task.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.crawlinghub.application.task.port.out.command.CrawlTaskPersistencePort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskTransactionManager 단위 테스트
 *
 * <p>Mockist 스타일 테스트: PersistencePort Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskTransactionManager 테스트")
class CrawlTaskTransactionManagerTest {

    @Mock private CrawlTaskPersistencePort crawlTaskPersistencePort;

    @InjectMocks private CrawlTaskTransactionManager manager;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlTask 저장 → CrawlTaskId 반환")
        void shouldPersistTaskAndReturnId() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(crawlTaskPersistencePort.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskId result = manager.persist(task);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(crawlTaskPersistencePort).persist(task);
        }

        @Test
        @DisplayName("[성공] RUNNING 상태 CrawlTask 저장")
        void shouldPersistRunningTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(crawlTaskPersistencePort.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskId result = manager.persist(task);

            // Then
            assertThat(result).isEqualTo(expectedId);
        }
    }
}
