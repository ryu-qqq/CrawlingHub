package com.ryuqq.crawlinghub.application.execution.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.common.dto.command.StatusChangeContext;
import com.ryuqq.crawlinghub.application.execution.service.command.FailCrawlTaskDirectlyService;
import com.ryuqq.crawlinghub.application.task.factory.command.CrawlTaskCommandFactory;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskCommandManager;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * FailCrawlTaskDirectlyService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FailCrawlTaskDirectlyService 테스트")
class FailCrawlTaskDirectlyServiceTest {

    @Mock private CrawlTaskCommandFactory commandFactory;

    @Mock private CrawlTaskReadManager crawlTaskReadManager;

    @Mock private CrawlTaskCommandManager crawlTaskCommandManager;

    @InjectMocks private FailCrawlTaskDirectlyService service;

    private void stubStatusChangeContext(Long taskId) {
        given(commandFactory.createStatusChangeContext(anyLong()))
                .willReturn(new StatusChangeContext<>(CrawlTaskId.of(taskId), Instant.now()));
    }

    @Nested
    @DisplayName("execute() 즉시 실패 처리 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] RUNNING 상태 Task → FAILED로 전환")
        void shouldFailDirectlyWhenTaskIsRunning() {
            // Given
            Long taskId = 1L;
            CrawlTask runningTask = CrawlTaskFixture.aRunningTask();
            stubStatusChangeContext(taskId);

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(runningTask));

            // When
            service.execute(taskId, "영구적 오류");

            // Then
            then(crawlTaskCommandManager).should().persist(runningTask);
        }

        @Test
        @DisplayName("[성공] PUBLISHED 상태 Task → FAILED로 전환")
        void shouldFailDirectlyWhenTaskIsPublished() {
            // Given
            Long taskId = 1L;
            CrawlTask publishedTask = CrawlTaskFixture.aPublishedTask();
            stubStatusChangeContext(taskId);

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(publishedTask));

            // When
            service.execute(taskId, "페이로드 변환 실패");

            // Then
            then(crawlTaskCommandManager).should().persist(publishedTask);
        }

        @Test
        @DisplayName("[스킵] Task 미존재 → 아무 동작 없음")
        void shouldSkipWhenTaskNotFound() {
            // Given
            Long taskId = 999L;
            stubStatusChangeContext(taskId);

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.empty());

            // When
            service.execute(taskId, "페이로드 변환 실패");

            // Then
            then(crawlTaskCommandManager).should(never()).persist(any());
        }

        @Test
        @DisplayName("[스킵] 이미 종료 상태 (SUCCESS) → 아무 동작 없음")
        void shouldSkipWhenTaskAlreadyTerminal() {
            // Given
            Long taskId = 1L;
            CrawlTask completedTask = CrawlTaskFixture.aSuccessTask();
            stubStatusChangeContext(taskId);

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(completedTask));

            // When
            service.execute(taskId, "페이로드 변환 실패");

            // Then
            then(crawlTaskCommandManager).should(never()).persist(any());
        }
    }
}
