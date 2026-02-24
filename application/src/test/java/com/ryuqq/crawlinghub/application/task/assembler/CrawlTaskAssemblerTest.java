package com.ryuqq.crawlinghub.application.task.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            CrawlTaskResult result = assembler.toResult(task);

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
            CrawlTask task = CrawlTaskFixture.aRunningTask();

            CrawlTaskResult result = assembler.toResult(task);

            assertThat(result.status()).isEqualTo("RUNNING");
        }
    }

    @Nested
    @DisplayName("toResults() 테스트")
    class ToResults {

        @Test
        @DisplayName("[성공] CrawlTask 목록 → CrawlTaskResult 목록 변환")
        void shouldConvertTaskListToResults() {
            List<CrawlTask> tasks =
                    List.of(
                            CrawlTaskFixture.aTaskWithId(1L),
                            CrawlTaskFixture.aTaskWithId(2L),
                            CrawlTaskFixture.aTaskWithId(3L));

            List<CrawlTaskResult> result = assembler.toResults(tasks);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).crawlTaskId()).isEqualTo(1L);
            assertThat(result.get(1).crawlTaskId()).isEqualTo(2L);
            assertThat(result.get(2).crawlTaskId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyTasks() {
            List<CrawlTask> tasks = List.of();

            List<CrawlTaskResult> result = assembler.toResults(tasks);

            assertThat(result).isEmpty();
        }
    }
}
