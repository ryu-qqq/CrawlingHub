package com.ryuqq.crawlinghub.application.schedule.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.common.dto.query.CommonSearchParams;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResult;
import com.ryuqq.crawlinghub.application.schedule.factory.query.CrawlSchedulerQueryFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.common.vo.QueryContext;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSearchCriteria;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerSortKey;
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
 * SearchCrawlSchedulerByOffsetService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCrawlSchedulerByOffsetService 테스트")
class SearchCrawlSchedulerByOffsetServiceTest {

    @Mock private CrawlSchedulerReadManager readManager;

    @Mock private CrawlSchedulerQueryFactory queryFactory;

    @Mock private CrawlSchedulerAssembler assembler;

    @InjectMocks private SearchCrawlSchedulerByOffsetService service;

    @Nested
    @DisplayName("execute() 스케줄러 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 목록 조회 시 CrawlSchedulerPageResult 반환")
        void shouldReturnPageResultWhenSchedulersExist() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            1L,
                            List.of("ACTIVE"),
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            null,
                            null,
                            QueryContext.of(
                                    CrawlSchedulerSortKey.CREATED_AT, null, PageRequest.of(0, 10)));

            List<CrawlScheduler> schedulers = List.of(CrawlSchedulerFixture.anActiveScheduler());
            long totalElements = 1L;

            CrawlSchedulerPageResult expectedResult =
                    CrawlSchedulerPageResult.of(
                            List.of(CrawlSchedulerResult.from(schedulers.get(0))),
                            PageMeta.of(0, 10, 1L));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(schedulers);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResult);

            // When
            CrawlSchedulerPageResult result = service.execute(params);

            // Then
            assertThat(result).isEqualTo(expectedResult);
            assertThat(result.results()).hasSize(1);
            then(queryFactory).should().createCriteria(params);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().countByCriteria(criteria);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 없을 시 빈 CrawlSchedulerPageResult 반환")
        void shouldReturnEmptyPageResultWhenNoSchedulersFound() {
            // Given
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            999L,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, 0, 10));

            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            null,
                            null,
                            QueryContext.of(
                                    CrawlSchedulerSortKey.CREATED_AT, null, PageRequest.of(0, 10)));

            CrawlSchedulerPageResult emptyResult = CrawlSchedulerPageResult.empty();

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(Collections.emptyList());
            given(readManager.countByCriteria(criteria)).willReturn(0L);
            given(assembler.toPageResult(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(emptyResult);

            // When
            CrawlSchedulerPageResult result = service.execute(params);

            // Then
            assertThat(result.results()).isEmpty();
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            CrawlSchedulerSearchParams params =
                    CrawlSchedulerSearchParams.of(
                            null,
                            null,
                            null,
                            null,
                            CommonSearchParams.of(null, null, null, null, null, page, size));

            CrawlSchedulerSearchCriteria criteria =
                    CrawlSchedulerSearchCriteria.of(
                            null,
                            null,
                            null,
                            null,
                            QueryContext.of(
                                    CrawlSchedulerSortKey.CREATED_AT,
                                    null,
                                    PageRequest.of(page, size)));

            List<CrawlScheduler> schedulers = List.of(CrawlSchedulerFixture.anActiveScheduler());
            long totalElements = 50L;

            CrawlSchedulerPageResult expectedResult =
                    CrawlSchedulerPageResult.of(
                            List.of(CrawlSchedulerResult.from(schedulers.get(0))),
                            PageMeta.of(page, size, totalElements));

            given(queryFactory.createCriteria(params)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(schedulers);
            given(readManager.countByCriteria(criteria)).willReturn(totalElements);
            given(assembler.toPageResult(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResult);

            // When
            CrawlSchedulerPageResult result = service.execute(params);

            // Then
            assertThat(result.pageMeta().page()).isEqualTo(page);
            assertThat(result.pageMeta().size()).isEqualTo(size);
            then(assembler).should().toPageResult(schedulers, page, size, totalElements);
        }
    }
}
