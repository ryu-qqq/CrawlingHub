package com.ryuqq.crawlinghub.application.task.coordinator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskIdFixture;
import com.ryuqq.crawlinghub.application.task.dto.bundle.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.internal.CrawlTaskCommandFacade;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxCommandManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskCommandCoordinator 단위 테스트
 *
 * <p>Mockist 스타일 테스트: CommandManager, OutboxCommandManager Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskCommandCoordinator 테스트")
class CrawlTaskCommandFacadeTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2025-11-27T12:00:00Z");

    @Mock private CrawlTaskCommandManager commandManager;

    @Mock private CrawlTaskOutboxCommandManager outboxCommandManager;

    @InjectMocks private CrawlTaskCommandFacade coordinator;

    @Nested
    @DisplayName("persist() 테스트")
    class Persist {

        @Test
        @DisplayName("[성공] CrawlTaskBundle 저장 → CrawlTaskId 반환")
        void shouldPersistBundleAndReturnTaskId() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskBundle bundle = CrawlTaskBundle.of(task, FIXED_INSTANT);
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(commandManager.persist(task)).willReturn(expectedId);

            // When
            CrawlTaskId result = coordinator.persist(bundle);

            // Then
            assertThat(result).isEqualTo(expectedId);
            verify(commandManager).persist(task);
            verify(outboxCommandManager).persist(any(CrawlTaskOutbox.class));
        }

        @Test
        @DisplayName("[성공] Outbox도 함께 저장됨")
        void shouldPersistOutboxAlongWithTask() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlTaskBundle bundle = CrawlTaskBundle.of(task, FIXED_INSTANT);
            CrawlTaskId expectedId = CrawlTaskIdFixture.anAssignedId();

            given(commandManager.persist(task)).willReturn(expectedId);

            // When
            coordinator.persist(bundle);

            // Then
            verify(commandManager).persist(task);
            verify(outboxCommandManager).persist(any(CrawlTaskOutbox.class));
        }
    }

    @Nested
    @DisplayName("retry() 테스트")
    class Retry {

        @Test
        @DisplayName("[성공] CrawlTask 재시도 저장 및 Outbox 생성")
        void shouldRetryTaskAndCreateOutbox() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRetryTask();
            CrawlTaskBundle bundle = CrawlTaskBundle.of(task, FIXED_INSTANT);
            CrawlTaskId taskId = task.getId();

            given(commandManager.persist(task)).willReturn(taskId);

            // When
            coordinator.retry(task, bundle);

            // Then
            verify(commandManager).persist(task);
            verify(outboxCommandManager).persist(any(CrawlTaskOutbox.class));
        }
    }
}
