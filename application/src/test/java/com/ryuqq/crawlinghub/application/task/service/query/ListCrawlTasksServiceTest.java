package com.ryuqq.crawlinghub.application.task.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskCriteria;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ListCrawlTasksService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListCrawlTasksService 테스트")
class ListCrawlTasksServiceTest {

    @Mock
    private CrawlTaskQueryPort crawlTaskQueryPort;

    @Mock
    private CrawlTaskAssembler assembler;

    @InjectMocks
    private ListCrawlTasksService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 태스크 목록 조회 시 PageResponse 반환")
        void shouldReturnPageResponseWhenTasksExist() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(1L, null, 0, 10);
            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> tasks = List.of(CrawlTaskFixture.aWaitingTask());
            long totalElements = 1L;

            CrawlTaskResponse response = new CrawlTaskResponse(
                    1L, 1L, 1L, "https://example.com/api",
                    CrawlTaskStatus.WAITING, CrawlTaskType.META, 0, LocalDateTime.now());
            PageResponse<CrawlTaskResponse> expectedResponse = PageResponse.of(
                    List.of(response), 0, 10, 1L, 1, true, true);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(crawlTaskQueryPort.findByCriteria(criteria)).willReturn(tasks);
            given(crawlTaskQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlTaskResponse> result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).hasSize(1);
            then(assembler).should().toCriteria(query);
            then(crawlTaskQueryPort).should().findByCriteria(criteria);
            then(crawlTaskQueryPort).should().countByCriteria(criteria);
            then(assembler).should().toPageResponse(tasks, 0, 10, totalElements);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 태스크 없을 시 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoTasksFound() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(999L, null, 0, 10);
            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> emptyTasks = Collections.emptyList();
            long totalElements = 0L;

            PageResponse<CrawlTaskResponse> expectedResponse = PageResponse.of(
                    Collections.emptyList(), 0, 10, 0L, 0, true, true);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(crawlTaskQueryPort.findByCriteria(criteria)).willReturn(emptyTasks);
            given(crawlTaskQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlTaskResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        @DisplayName("[성공] 상태 필터로 조회")
        void shouldFilterByStatus() {
            // Given
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(
                    1L, CrawlTaskStatus.WAITING, 0, 10);
            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> waitingTasks = List.of(CrawlTaskFixture.aWaitingTask());
            long totalElements = 1L;

            CrawlTaskResponse response = new CrawlTaskResponse(
                    1L, 1L, 1L, "https://example.com/api",
                    CrawlTaskStatus.WAITING, CrawlTaskType.META, 0, LocalDateTime.now());
            PageResponse<CrawlTaskResponse> expectedResponse = PageResponse.of(
                    List.of(response), 0, 10, 1L, 1, true, true);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(crawlTaskQueryPort.findByCriteria(criteria)).willReturn(waitingTasks);
            given(crawlTaskQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlTaskResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(CrawlTaskStatus.WAITING);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            ListCrawlTasksQuery query = new ListCrawlTasksQuery(1L, null, page, size);
            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> tasks = List.of(CrawlTaskFixture.aWaitingTask());
            long totalElements = 50L;

            PageResponse<CrawlTaskResponse> expectedResponse = PageResponse.of(
                    Collections.emptyList(), page, size, totalElements, 3, false, false);

            given(assembler.toCriteria(query)).willReturn(criteria);
            given(crawlTaskQueryPort.findByCriteria(criteria)).willReturn(tasks);
            given(crawlTaskQueryPort.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlTaskResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            then(assembler).should().toPageResponse(tasks, page, size, totalElements);
        }
    }
}
