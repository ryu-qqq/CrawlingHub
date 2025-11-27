package com.ryuqq.crawlinghub.application.task.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.CrawlTaskBundle;
import com.ryuqq.crawlinghub.application.task.dto.command.CreateCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.command.TriggerCrawlTaskCommand;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlTaskAssembler 단위 테스트
 *
 * <p>Mockist 스타일 테스트: ObjectMapper 의존성 Spy
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlTaskAssembler 테스트")
class CrawlTaskAssemblerTest {

    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks private CrawlTaskAssembler assembler;

    @Nested
    @DisplayName("toBundle(TriggerCrawlTaskCommand, CrawlScheduler) 테스트")
    class ToBundleTrigger {

        @Test
        @DisplayName("[성공] TriggerCrawlTaskCommand → CrawlTaskBundle 생성")
        void shouldCreateBundleFromTriggerCommand() {
            // Given
            TriggerCrawlTaskCommand command = new TriggerCrawlTaskCommand(1L);
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // When
            CrawlTaskBundle result = assembler.toBundle(command, scheduler);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlTask()).isNotNull();
            assertThat(result.getCrawlTask().getCrawlSchedulerId())
                    .isEqualTo(scheduler.getCrawlSchedulerId());
            assertThat(result.getCrawlTask().getSellerId()).isEqualTo(scheduler.getSellerId());
            assertThat(result.getCrawlTask().getTaskType()).isEqualTo(CrawlTaskType.META);
            assertThat(result.getOutboxPayload()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("toBundle(CreateCrawlTaskCommand) 테스트")
    class ToBundleCreate {

        @Test
        @DisplayName("[성공] CreateCrawlTaskCommand (META) → CrawlTaskBundle 생성")
        void shouldCreateBundleForMetaTask() {
            // Given
            CreateCrawlTaskCommand command =
                    new CreateCrawlTaskCommand(1L, 100L, CrawlTaskType.META, null);

            // When
            CrawlTaskBundle result = assembler.toBundle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlTask().getTaskType()).isEqualTo(CrawlTaskType.META);
            assertThat(result.getOutboxPayload()).contains("META");
        }

        @Test
        @DisplayName("[성공] CreateCrawlTaskCommand (MINI_SHOP) → CrawlTaskBundle 생성")
        void shouldCreateBundleForMiniShopTask() {
            // Given
            CreateCrawlTaskCommand command =
                    new CreateCrawlTaskCommand(1L, 100L, CrawlTaskType.MINI_SHOP, null);

            // When
            CrawlTaskBundle result = assembler.toBundle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlTask().getTaskType()).isEqualTo(CrawlTaskType.MINI_SHOP);
        }

        @Test
        @DisplayName("[성공] CreateCrawlTaskCommand (DETAIL) → CrawlTaskBundle 생성")
        void shouldCreateBundleForDetailTask() {
            // Given
            CreateCrawlTaskCommand command =
                    new CreateCrawlTaskCommand(1L, 100L, CrawlTaskType.DETAIL, 12345L);

            // When
            CrawlTaskBundle result = assembler.toBundle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlTask().getTaskType()).isEqualTo(CrawlTaskType.DETAIL);
            assertThat(result.getCrawlTask().getEndpoint().toFullUrl()).contains("12345");
        }

        @Test
        @DisplayName("[성공] CreateCrawlTaskCommand (OPTION) → CrawlTaskBundle 생성")
        void shouldCreateBundleForOptionTask() {
            // Given
            CreateCrawlTaskCommand command =
                    new CreateCrawlTaskCommand(1L, 100L, CrawlTaskType.OPTION, 12345L);

            // When
            CrawlTaskBundle result = assembler.toBundle(command);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCrawlTask().getTaskType()).isEqualTo(CrawlTaskType.OPTION);
        }
    }

    @Nested
    @DisplayName("toOutboxPayload() 테스트")
    class ToOutboxPayload {

        @Test
        @DisplayName("[성공] CrawlTask → Outbox 페이로드 JSON 변환")
        void shouldConvertTaskToOutboxPayload() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();

            // When
            String result = assembler.toOutboxPayload(task);

            // Then
            assertThat(result).isNotBlank();
            assertThat(result).contains("schedulerId");
            assertThat(result).contains("sellerId");
            assertThat(result).contains("taskType");
            assertThat(result).contains("endpoint");
        }

        @Test
        @DisplayName("[성공] CrawlTask + CrawlScheduler → Outbox 페이로드 JSON 변환")
        void shouldConvertTaskAndSchedulerToOutboxPayload() {
            // Given
            CrawlTask task = CrawlTaskFixture.aWaitingTask();
            CrawlScheduler scheduler = CrawlSchedulerFixture.anActiveScheduler();

            // When
            String result = assembler.toOutboxPayload(task, scheduler);

            // Then
            assertThat(result).isNotBlank();
            assertThat(result).contains("schedulerId");
            assertThat(result).contains("sellerId");
        }
    }

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteria {

        @Test
        @DisplayName("[성공] ListCrawlTasksQuery → CrawlTaskCriteria 변환")
        void shouldConvertQueryToCriteria() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(1L, CrawlTaskStatus.WAITING, 0, 20);

            // When
            CrawlTaskCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.crawlSchedulerId().value()).isEqualTo(1L);
            assertThat(result.status()).isEqualTo(CrawlTaskStatus.WAITING);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] ListCrawlTasksQuery → CrawlTaskCriteria 변환 (null 상태)")
        void shouldConvertQueryToCriteriaWithNullStatus() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(1L, null, 0, 10);

            // When
            CrawlTaskCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.status()).isNull();
        }
    }

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
