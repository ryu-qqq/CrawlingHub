package com.ryuqq.crawlinghub.application.task.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
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
 * <p>Domain → Response 변환만 테스트
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
    @DisplayName("toResponse() 테스트")
    class ToResponse {

        @Test
        @DisplayName("[성공] CrawlTask → CrawlTaskResponse 변환")
        void shouldConvertTaskToResponse() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskResponse result = assembler.toResponse(task);

            // Then
            assertThat(result.crawlTaskId()).isEqualTo(task.getId().value());
            assertThat(result.crawlSchedulerId()).isEqualTo(task.getCrawlSchedulerId().value());
            assertThat(result.sellerId()).isEqualTo(task.getSellerId().value());
            assertThat(result.requestUrl()).isEqualTo(task.getEndpoint().toFullUrl());
            assertThat(result.status()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(result.taskType()).isEqualTo(task.getTaskType());
            assertThat(result.retryCount()).isEqualTo(task.getRetryCount().value());
        }

        @Test
        @DisplayName("[성공] RUNNING 상태 CrawlTask → CrawlTaskResponse 변환")
        void shouldConvertRunningTaskToResponse() {
            // Given
            CrawlTask task = CrawlTaskFixture.aRunningTask();

            // When
            CrawlTaskResponse result = assembler.toResponse(task);

            // Then
            assertThat(result.status()).isEqualTo(CrawlTaskStatus.RUNNING);
        }
    }

    @Nested
    @DisplayName("toDetailResponse() 테스트")
    class ToDetailResponse {

        @Test
        @DisplayName("[성공] CrawlTask → CrawlTaskDetailResponse 변환")
        void shouldConvertTaskToDetailResponse() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            CrawlTaskDetailResponse result = assembler.toDetailResponse(task);

            // Then
            assertThat(result.crawlTaskId()).isEqualTo(task.getId().value());
            assertThat(result.crawlSchedulerId()).isEqualTo(task.getCrawlSchedulerId().value());
            assertThat(result.sellerId()).isEqualTo(task.getSellerId().value());
            assertThat(result.status()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(result.taskType()).isEqualTo(task.getTaskType());
            assertThat(result.baseUrl()).isEqualTo(task.getEndpoint().baseUrl());
            assertThat(result.path()).isEqualTo(task.getEndpoint().path());
            assertThat(result.queryParams()).isEqualTo(task.getEndpoint().queryParams());
            assertThat(result.fullUrl()).isEqualTo(task.getEndpoint().toFullUrl());
        }
    }

    @Nested
    @DisplayName("toResponses() 테스트")
    class ToResponses {

        @Test
        @DisplayName("[성공] CrawlTask 목록 → CrawlTaskResponse 목록 변환")
        void shouldConvertTaskListToResponses() {
            // Given
            List<CrawlTask> tasks =
                    List.of(
                            CrawlTaskFixture.aTaskWithId(1L),
                            CrawlTaskFixture.aTaskWithId(2L),
                            CrawlTaskFixture.aTaskWithId(3L));

            // When
            List<CrawlTaskResponse> result = assembler.toResponses(tasks);

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
            List<CrawlTaskResponse> result = assembler.toResponses(tasks);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponse {

        @Test
        @DisplayName("[성공] CrawlTask 목록 → PageResponse 변환")
        void shouldConvertTasksToPageResponse() {
            // Given
            List<CrawlTask> tasks =
                    List.of(CrawlTaskFixture.aTaskWithId(1L), CrawlTaskFixture.aTaskWithId(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlTaskResponse> result =
                    assembler.toPageResponse(tasks, page, size, totalElements);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25L);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → last = true")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<CrawlTask> tasks = List.of(CrawlTaskFixture.aTaskWithId(1L));
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlTaskResponse> result =
                    assembler.toPageResponse(tasks, page, size, totalElements);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] 빈 결과 → 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponse() {
            // Given
            List<CrawlTask> tasks = List.of();
            int page = 0;
            int size = 10;
            long totalElements = 0L;

            // When
            PageResponse<CrawlTaskResponse> result =
                    assembler.toPageResponse(tasks, page, size, totalElements);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
        }
    }
}
