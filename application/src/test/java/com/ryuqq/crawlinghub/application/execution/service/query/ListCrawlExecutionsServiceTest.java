package com.ryuqq.crawlinghub.application.execution.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.assembler.CrawlExecutionAssembler;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.application.execution.factory.query.CrawlExecutionQueryFactory;
import com.ryuqq.crawlinghub.application.execution.manager.query.CrawlExecutionReadManager;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ListCrawlExecutionsService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Manager 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ListCrawlExecutionsService 테스트")
class ListCrawlExecutionsServiceTest {

    @Mock private CrawlExecutionReadManager readManager;

    @Mock private CrawlExecutionQueryFactory queryFactory;

    @Mock private CrawlExecutionAssembler assembler;

    @InjectMocks private ListCrawlExecutionsService service;

    @Nested
    @DisplayName("execute() 크롤 실행 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 실행 목록 조회 시 PageResponse 반환")
        void shouldReturnPageResponseWhenExecutionsExist() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(1L, null, null, null, null, null, 0, 10);
            CrawlExecutionCriteria criteria = Mockito.mock(CrawlExecutionCriteria.class);
            List<CrawlExecution> executions = List.of(CrawlExecutionFixture.aRunningExecution());
            long totalElements = 1L;

            Instant now = Instant.now();
            CrawlExecutionResponse response =
                    new CrawlExecutionResponse(
                            1L,
                            1L,
                            1L,
                            1L,
                            CrawlExecutionStatus.RUNNING,
                            null,
                            null,
                            now,
                            null,
                            now,
                            null);
            PageResponse<CrawlExecutionResponse> expectedResponse =
                    PageResponse.of(List.of(response), 0, 10, 1L, 1, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(executions);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlExecutionResponse> result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).hasSize(1);
            then(queryFactory).should().createCriteria(query);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
            then(assembler).should().toPageResponse(executions, 0, 10, totalElements);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 실행 없을 시 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoExecutionsFound() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(999L, null, null, null, null, null, 0, 10);
            CrawlExecutionCriteria criteria = Mockito.mock(CrawlExecutionCriteria.class);
            List<CrawlExecution> emptyExecutions = Collections.emptyList();
            long totalElements = 0L;

            PageResponse<CrawlExecutionResponse> expectedResponse =
                    PageResponse.of(Collections.emptyList(), 0, 10, 0L, 0, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(emptyExecutions);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlExecutionResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        @DisplayName("[성공] 상태 필터로 조회")
        void shouldFilterByStatus() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            1L,
                            null,
                            null,
                            List.of(CrawlExecutionStatus.SUCCESS),
                            null,
                            null,
                            0,
                            10);
            CrawlExecutionCriteria criteria = Mockito.mock(CrawlExecutionCriteria.class);
            List<CrawlExecution> successExecutions =
                    List.of(CrawlExecutionFixture.aSuccessExecution());
            long totalElements = 1L;

            Instant now = Instant.now();
            CrawlExecutionResponse response =
                    new CrawlExecutionResponse(
                            1L,
                            1L,
                            1L,
                            1L,
                            CrawlExecutionStatus.SUCCESS,
                            200,
                            1500L,
                            now,
                            now,
                            now,
                            null);
            PageResponse<CrawlExecutionResponse> expectedResponse =
                    PageResponse.of(List.of(response), 0, 10, 1L, 1, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(successExecutions);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlExecutionResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
        }

        @Test
        @DisplayName("[성공] 셀러 ID로 필터링")
        void shouldFilterBySellerId() {
            // Given
            Long sellerId = 5L;
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(null, null, sellerId, null, null, null, 0, 10);
            CrawlExecutionCriteria criteria = Mockito.mock(CrawlExecutionCriteria.class);
            List<CrawlExecution> executions = List.of(CrawlExecutionFixture.aRunningExecution());
            long totalElements = 1L;

            Instant now = Instant.now();
            CrawlExecutionResponse response =
                    new CrawlExecutionResponse(
                            1L,
                            1L,
                            1L,
                            sellerId,
                            CrawlExecutionStatus.RUNNING,
                            null,
                            null,
                            now,
                            null,
                            now,
                            null);
            PageResponse<CrawlExecutionResponse> expectedResponse =
                    PageResponse.of(List.of(response), 0, 10, 1L, 1, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(executions);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlExecutionResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).sellerId()).isEqualTo(sellerId);
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(1L, null, null, null, null, null, page, size);
            CrawlExecutionCriteria criteria = Mockito.mock(CrawlExecutionCriteria.class);
            List<CrawlExecution> executions = List.of(CrawlExecutionFixture.aRunningExecution());
            long totalElements = 50L;

            PageResponse<CrawlExecutionResponse> expectedResponse =
                    PageResponse.of(
                            Collections.emptyList(), page, size, totalElements, 3, false, false);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(executions);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlExecutionResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            then(assembler).should().toPageResponse(executions, page, size, totalElements);
        }
    }
}
