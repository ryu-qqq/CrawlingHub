package com.ryuqq.crawlinghub.application.task.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.crawl.task.CrawlTaskFixture;
import com.ryuqq.crawlinghub.application.task.assembler.CrawlTaskAssembler;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskQueryFactory;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTask;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskCriteria;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SearchCrawlTaskByOffsetService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@Tag("application")
@Tag("service")
@DisplayName("SearchCrawlTaskByOffsetService 테스트")
class SearchCrawlTaskByOffsetServiceTest {

    @Mock private CrawlTaskReadManager readManager;

    @Mock private CrawlTaskQueryFactory queryFactory;

    @Mock private CrawlTaskAssembler assembler;

    @InjectMocks private SearchCrawlTaskByOffsetService service;

    @Nested
    @DisplayName("execute() 크롤 태스크 다건 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 정상 조회 시 CrawlTaskPageResult 반환")
        void shouldReturnPageResultWhenTasksExist() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(List.of(100L), null, null, null, null, null, 0, 20);

            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> crawlTasks =
                    List.of(CrawlTaskFixture.aTaskWithId(1L), CrawlTaskFixture.aTaskWithId(2L));
            long totalElements = 2L;

            CrawlTaskPageResult expectedResult =
                    CrawlTaskPageResult.of(
                            List.of(
                                    CrawlTaskResult.from(crawlTasks.get(0)),
                                    CrawlTaskResult.from(crawlTasks.get(1))),
                            PageMeta.of(0, 20, 2L));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(crawlTasks);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(crawlTasks, 0, 20, 2L)).willReturn(expectedResult);

            // When
            CrawlTaskPageResult result = service.execute(params);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.results()).hasSize(2);
            then(queryFactory).should().createCriteria(params);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
            then(assembler).should().toPageResult(crawlTasks, 0, 20, 2L);
        }

        @Test
        @DisplayName("[성공] 빈 결과 시 빈 CrawlTaskPageResult 반환")
        void shouldReturnEmptyPageResultWhenNoTasks() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(List.of(999L), null, null, null, null, null, 0, 20);

            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> emptyList = List.of();
            CrawlTaskPageResult emptyResult = CrawlTaskPageResult.empty();

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(emptyList);
            given(readManager.countByCriteria(criteria)).willReturn(0L);
            given(assembler.toPageResult(emptyList, 0, 20, 0L)).willReturn(emptyResult);

            // When
            CrawlTaskPageResult result = service.execute(params);

            // Then
            assertThat(result.isEmpty()).isTrue();
            then(queryFactory).should().createCriteria(params);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 필터 조합 적용 시 정상 조회")
        void shouldReturnFilteredResults() {
            // Given
            CrawlTaskSearchParams params =
                    new CrawlTaskSearchParams(
                            List.of(100L, 200L),
                            List.of(10L),
                            List.of("FAILED"),
                            List.of("META"),
                            null,
                            null,
                            1,
                            10);

            CrawlTaskCriteria criteria = org.mockito.Mockito.mock(CrawlTaskCriteria.class);
            List<CrawlTask> crawlTasks = List.of(CrawlTaskFixture.aFailedTask());
            CrawlTaskPageResult expectedResult =
                    CrawlTaskPageResult.of(
                            List.of(CrawlTaskResult.from(crawlTasks.get(0))),
                            PageMeta.of(1, 10, 1L));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(crawlTasks);
            given(readManager.countByCriteria(criteria)).willReturn(1L);
            given(assembler.toPageResult(crawlTasks, 1, 10, 1L)).willReturn(expectedResult);

            // When
            CrawlTaskPageResult result = service.execute(params);

            // Then
            assertThat(result.results()).hasSize(1);
            then(queryFactory).should().createCriteria(params);
        }
    }
}
