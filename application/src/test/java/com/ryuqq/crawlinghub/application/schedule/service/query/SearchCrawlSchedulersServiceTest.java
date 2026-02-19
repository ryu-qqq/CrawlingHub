package com.ryuqq.crawlinghub.application.schedule.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.ryuqq.cralwinghub.domain.fixture.schedule.CrawlSchedulerFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.assembler.CrawlSchedulerAssembler;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.application.schedule.factory.query.CrawlSchedulerQueryFactory;
import com.ryuqq.crawlinghub.application.schedule.manager.query.CrawlSchedulerReadManager;
import com.ryuqq.crawlinghub.domain.common.vo.PageRequest;
import com.ryuqq.crawlinghub.domain.schedule.aggregate.CrawlScheduler;
import com.ryuqq.crawlinghub.domain.schedule.query.CrawlSchedulerPageCriteria;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
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
 * SearchCrawlSchedulersService 단위 테스트
 *
 * <p>Mockist 스타일 테스트: Port 의존성 Mocking
 *
 * @author development-team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchCrawlSchedulersService 테스트")
class SearchCrawlSchedulersServiceTest {

    @Mock private CrawlSchedulerReadManager readManager;

    @Mock private CrawlSchedulerQueryFactory queryFactory;

    @Mock private CrawlSchedulerAssembler assembler;

    @InjectMocks private SearchCrawlSchedulersService service;

    @Nested
    @DisplayName("execute() 스케줄러 목록 조회 테스트")
    class Execute {

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 목록 조회 시 PageResponse 반환")
        void shouldReturnPageResponseWhenSchedulersExist() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(
                            1L, List.of(SchedulerStatus.ACTIVE), null, null, 0, 10);
            CrawlSchedulerPageCriteria criteria =
                    CrawlSchedulerPageCriteria.of(null, null, null, PageRequest.of(0, 10));
            List<CrawlScheduler> schedulers = List.of(CrawlSchedulerFixture.anActiveScheduler());
            long totalElements = 1L;

            CrawlSchedulerResponse response =
                    new CrawlSchedulerResponse(
                            1L,
                            1L,
                            "daily-crawl",
                            "cron(0 0 * * ? *)",
                            SchedulerStatus.ACTIVE,
                            Instant.now(),
                            Instant.now());
            PageResponse<CrawlSchedulerResponse> expectedResponse =
                    PageResponse.of(List.of(response), 0, 10, 1L, 1, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(schedulers);
            given(readManager.count(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlSchedulerResponse> result = service.execute(query);

            // Then
            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.content()).hasSize(1);
            then(queryFactory).should().createCriteria(query);
            then(readManager).should().findByCriteria(criteria);
            then(readManager).should().count(criteria);
            then(assembler).should().toPageResponse(schedulers, 0, 10, totalElements);
        }

        @Test
        @DisplayName("[성공] 조건에 맞는 스케줄러 없을 시 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponseWhenNoSchedulersFound() {
            // Given
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(999L, null, null, null, 0, 10);
            CrawlSchedulerPageCriteria criteria =
                    CrawlSchedulerPageCriteria.of(null, null, null, PageRequest.of(0, 10));
            List<CrawlScheduler> emptySchedulers = Collections.emptyList();
            long totalElements = 0L;

            PageResponse<CrawlSchedulerResponse> expectedResponse =
                    PageResponse.of(Collections.emptyList(), 0, 10, 0L, 0, true, true);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(emptySchedulers);
            given(readManager.count(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlSchedulerResponse> result = service.execute(query);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }

        @Test
        @DisplayName("[성공] 페이징 파라미터가 올바르게 전달됨")
        void shouldPassCorrectPagingParameters() {
            // Given
            int page = 2;
            int size = 20;
            SearchCrawlSchedulersQuery query =
                    new SearchCrawlSchedulersQuery(null, null, null, null, page, size);
            CrawlSchedulerPageCriteria criteria =
                    CrawlSchedulerPageCriteria.of(null, null, null, PageRequest.of(page, size));
            List<CrawlScheduler> schedulers = List.of(CrawlSchedulerFixture.anActiveScheduler());
            long totalElements = 50L;

            PageResponse<CrawlSchedulerResponse> expectedResponse =
                    PageResponse.of(
                            Collections.emptyList(), page, size, totalElements, 3, false, false);

            given(queryFactory.createCriteria(query)).willReturn(criteria);
            given(readManager.findByCriteria(criteria)).willReturn(schedulers);
            given(readManager.count(criteria)).willReturn(totalElements);
            given(assembler.toPageResponse(anyList(), anyInt(), anyInt(), anyLong()))
                    .willReturn(expectedResponse);

            // When
            PageResponse<CrawlSchedulerResponse> result = service.execute(query);

            // Then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.size()).isEqualTo(size);
            then(assembler).should().toPageResponse(schedulers, page, size, totalElements);
        }
    }
}
