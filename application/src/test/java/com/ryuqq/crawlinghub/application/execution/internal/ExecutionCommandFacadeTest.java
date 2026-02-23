package com.ryuqq.crawlinghub.application.execution.internal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionIdFixture;
import com.ryuqq.crawlinghub.application.execution.dto.bundle.CrawlTaskExecutionBundle;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.execution.manager.CrawlExecutionCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ExecutionCommandFacade 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExecutionCommandFacade 테스트")
class ExecutionCommandFacadeTest {

    @Mock private CrawlTaskCommandManager taskCommandManager;

    @Mock private CrawlExecutionCommandManager executionCommandManager;

    @InjectMocks private ExecutionCommandFacade facade;

    @Nested
    @DisplayName("persistForPrepare() 테스트")
    class PersistForPrepare {

        @Test
        @DisplayName("[성공] Bundle의 Task + Execution 모두 저장")
        void shouldPersistTaskAndExecution() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());

            given(taskCommandManager.persist(task)).willReturn(CrawlTaskIdFixture.anAssignedId());
            given(executionCommandManager.persist(execution))
                    .willReturn(CrawlExecutionIdFixture.anAssignedId());

            // When
            facade.persist(bundle);

            // Then
            then(taskCommandManager).should().persist(task);
            then(executionCommandManager).should().persist(execution);
        }
    }

    @Nested
    @DisplayName("persistForComplete() 테스트")
    class PersistForComplete {

        @Test
        @DisplayName("[성공] Bundle의 Execution + Task 모두 저장")
        void shouldPersistExecutionAndTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();
            ExecuteCrawlTaskCommand command =
                    new ExecuteCrawlTaskCommand(1L, 1L, 1L, "META", "https://example.com/api");
            CrawlTaskExecutionBundle bundle =
                    CrawlTaskExecutionBundle.of(task, execution, command, Instant.now());

            given(executionCommandManager.persist(execution))
                    .willReturn(CrawlExecutionIdFixture.anAssignedId());
            given(taskCommandManager.persist(task)).willReturn(CrawlTaskIdFixture.anAssignedId());

            // When
            facade.persist(bundle);

            // Then
            then(executionCommandManager).should().persist(execution);
            then(taskCommandManager).should().persist(task);
        }
    }

    @Nested
    @DisplayName("persistTask() 테스트")
    class PersistTask {

        @Test
        @DisplayName("[성공] CrawlTask 단건 저장 위임")
        void shouldDelegatePersistTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aPublishedTask();
            given(taskCommandManager.persist(task)).willReturn(CrawlTaskIdFixture.anAssignedId());

            // When
            facade.persistTask(task);

            // Then
            then(taskCommandManager).should().persist(task);
        }
    }
}
