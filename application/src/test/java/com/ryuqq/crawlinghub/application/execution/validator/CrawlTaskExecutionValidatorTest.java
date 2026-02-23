package com.ryuqq.crawlinghub.application.execution.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.exception.CrawlTaskNotFoundException;
import com.ryuqq.crawlinghub.domain.task.id.CrawlTaskId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskExecutionValidator 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskExecutionValidator 테스트")
class CrawlTaskExecutionValidatorTest {

    @Mock private CrawlTaskReadManager crawlTaskReadManager;

    @InjectMocks private CrawlTaskExecutionValidator validator;

    @Nested
    @DisplayName("validateAndGet() 테스트")
    class ValidateAndGet {

        @Test
        @DisplayName("[성공] PUBLISHED 상태 Task → Optional.of(task) 반환")
        void shouldReturnTaskWhenPublished() {
            // Given
            Long taskId = 1L;
            CrawlTask publishedTask = CrawlTaskFixture.aPublishedTask();

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(publishedTask));

            // When
            Optional<CrawlTask> result = validator.validateAndGet(taskId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(publishedTask);
        }

        @Test
        @DisplayName("[멱등성] SUCCESS 상태 Task → Optional.empty() 반환")
        void shouldReturnEmptyWhenTaskAlreadyCompleted() {
            // Given
            Long taskId = 1L;
            CrawlTask completedTask = CrawlTaskFixture.aSuccessTask();

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(completedTask));

            // When
            Optional<CrawlTask> result = validator.validateAndGet(taskId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[멱등성] RUNNING 상태 Task → Optional.empty() 반환 (다른 워커 처리 중)")
        void shouldReturnEmptyWhenTaskIsRunning() {
            // Given
            Long taskId = 1L;
            CrawlTask runningTask = CrawlTaskFixture.aRunningTask();

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.of(runningTask));

            // When
            Optional<CrawlTask> result = validator.validateAndGet(taskId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[실패] CrawlTask 미존재 → CrawlTaskNotFoundException")
        void shouldThrowWhenTaskNotFound() {
            // Given
            Long taskId = 999L;

            given(crawlTaskReadManager.findById(any(CrawlTaskId.class)))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> validator.validateAndGet(taskId))
                    .isInstanceOf(CrawlTaskNotFoundException.class);
        }
    }
}
