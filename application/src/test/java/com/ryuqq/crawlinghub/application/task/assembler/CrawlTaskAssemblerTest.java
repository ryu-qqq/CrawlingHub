package com.ryuqq.crawlinghub.application.task.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskAssembler 단위 테스트
 *
 * <p>Domain → Result 변환만 테스트
 *
 * <p>Command → Domain, Query → Criteria 변환은 CrawlTaskCommandFactory, CrawlTaskQueryFactory에서 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskAssembler 테스트")
class CrawlTaskAssemblerTest {

    @InjectMocks private CrawlTaskAssembler assembler;

    @Nested
    @DisplayName("toResult() 테스트")
    class ToResult {

        @Test
        @DisplayName("[성공] CrawlTask → CrawlTaskResult 변환")
        void shouldConvertTaskToResult() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskResult result = assembler.toResult(task);

            // Then
            assertThat(result.crawlTaskId()).isEqualTo(task.getIdValue());
            assertThat(result.crawlSchedulerId()).isEqualTo(task.getCrawlSchedulerIdValue());
            assertThat(result.sellerId()).isEqualTo(task.getSellerIdValue());
            assertThat(result.requestUrl()).isEqualTo(task.getEndpoint().toFullUrl());
            assertThat(result.baseUrl()).isEqualTo(task.getEndpoint().baseUrl());
            assertThat(result.path()).isEqualTo(task.getEndpoint().path());
            assertThat(result.queryParams()).isEqualTo(task.getEndpoint().queryParams());
            assertThat(result.status()).isEqualTo("WAITING");
            assertThat(result.taskType()).isEqualTo(task.getTaskType().name());
            assertThat(result.retryCount()).isEqualTo(task.getRetryCountValue());
        }

        @Test
        @DisplayName("[성공] RUNNING 상태 CrawlTask → CrawlTaskResult 변환")
        void shouldConvertRunningTaskToResult() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();

            // When
            CrawlTaskResult result = assembler.toResult(task);

            // Then
            assertThat(result.status()).isEqualTo("RUNNING");
        }
    }

    @Nested
    @DisplayName("toResults() 테스트")
    class ToResults {

        @Test
        @DisplayName("[성공] CrawlTask 목록 → CrawlTaskResult 목록 변환")
        void shouldConvertTaskListToResults() {
            // Given
            List<CrawlTask> tasks =
                    List.of(
                            CrawlTaskFixture.aTaskWithId(1L),
                            CrawlTaskFixture.aTaskWithId(2L),
                            CrawlTaskFixture.aTaskWithId(3L));

            // When
            List<CrawlTaskResult> result = assembler.toResults(tasks);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).crawlTaskId()).isEqualTo(1L);
            assertThat(result.get(1).crawlTaskId()).isEqualTo(2L);
            assertThat(result.get(2).crawlTaskId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyTasks() {
            // Given
            List<CrawlTask> tasks = List.of();

            // When
            List<CrawlTaskResult> result = assembler.toResults(tasks);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPageResult() 테스트")
    class ToPageResult {

        @Test
        @DisplayName("[성공] CrawlTask 목록 → CrawlTaskPageResult 변환")
        void shouldConvertTasksToPageResult() {
            // Given
            List<CrawlTask> tasks =
                    List.of(CrawlTaskFixture.aTaskWithId(1L), CrawlTaskFixture.aTaskWithId(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            CrawlTaskPageResult result = assembler.toPageResult(tasks, page, size, totalElements);

            // Then
            assertThat(result.results()).hasSize(2);
            assertThat(result.pageMeta().page()).isZero();
            assertThat(result.pageMeta().size()).isEqualTo(10);
            assertThat(result.pageMeta().totalElements()).isEqualTo(25L);
        }

        @Test
        @DisplayName("[성공] 빈 결과 → 빈 CrawlTaskPageResult 반환")
        void shouldReturnEmptyPageResult() {
            // Given
            List<CrawlTask> tasks = List.of();
            int page = 0;
            int size = 10;
            long totalElements = 0L;

            // When
            CrawlTaskPageResult result = assembler.toPageResult(tasks, page, size, totalElements);

            // Then
            assertThat(result.results()).isEmpty();
            assertThat(result.pageMeta().totalElements()).isZero();
        }
    }
}
