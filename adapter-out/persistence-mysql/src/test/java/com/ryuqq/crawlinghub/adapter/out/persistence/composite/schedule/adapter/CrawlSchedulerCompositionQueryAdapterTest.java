package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskSummaryDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.mapper.CrawlSchedulerCompositeMapper;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.repository.CrawlSchedulerCompositeQueryDslRepository;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SellerSummary;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CrawlSchedulerCompositionQueryAdapter 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("adapter")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlSchedulerCompositionQueryAdapter 단위 테스트")
class CrawlSchedulerCompositionQueryAdapterTest {

    @Mock private CrawlSchedulerCompositeQueryDslRepository compositeRepository;
    @Mock private CrawlSchedulerCompositeMapper compositeMapper;

    @InjectMocks private CrawlSchedulerCompositionQueryAdapter adapter;

    @Nested
    @DisplayName("findSchedulerDetailById() 테스트")
    class FindSchedulerDetailByIdTests {

        @Test
        @DisplayName("스케줄러가 존재하면 상세 결과를 반환한다")
        void findSchedulerDetailById_WithExistingScheduler_ShouldReturnResult() {
            // given
            Long schedulerId = 1L;
            LocalDateTime now = LocalDateTime.of(2025, 11, 20, 10, 30, 0);

            CrawlSchedulerCompositeDto compositeDto =
                    new CrawlSchedulerCompositeDto(
                            1L,
                            100L,
                            "daily-crawl",
                            "0 0 9 * * ?",
                            "ACTIVE",
                            now.minusDays(30),
                            now,
                            "TestSeller",
                            "머스트잇셀러");

            List<CrawlSchedulerTaskSummaryDto> taskSummaries =
                    List.of(
                            new CrawlSchedulerTaskSummaryDto(
                                    1001L, "SUCCESS", "SEARCH", now.minusHours(1), now));

            List<CrawlSchedulerTaskStatisticsDto> taskStatistics =
                    List.of(
                            new CrawlSchedulerTaskStatisticsDto("SUCCESS", 145),
                            new CrawlSchedulerTaskStatisticsDto("FAILED", 3));

            CrawlSchedulerDetailResult expectedResult =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(
                                    1L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", null, null),
                            new SellerSummary(100L, "TestSeller", "머스트잇셀러"),
                            new ExecutionInfo(null, "SUCCESS"),
                            new SchedulerStatistics(148, 145, 3, 0.9797),
                            List.of());

            given(compositeRepository.fetchSchedulerWithSeller(schedulerId))
                    .willReturn(Optional.of(compositeDto));
            given(compositeRepository.fetchRecentTasks(schedulerId, 10)).willReturn(taskSummaries);
            given(compositeRepository.fetchTaskStatistics(schedulerId)).willReturn(taskStatistics);
            given(compositeMapper.toResult(compositeDto, taskSummaries, taskStatistics))
                    .willReturn(expectedResult);

            // when
            Optional<CrawlSchedulerDetailResult> result =
                    adapter.findSchedulerDetailById(schedulerId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(expectedResult);
            assertThat(result.get().scheduler().schedulerName()).isEqualTo("daily-crawl");
            assertThat(result.get().seller().sellerName()).isEqualTo("TestSeller");
        }

        @Test
        @DisplayName("스케줄러가 존재하지 않으면 빈 Optional을 반환한다")
        void findSchedulerDetailById_WithNonExistentScheduler_ShouldReturnEmpty() {
            // given
            Long schedulerId = 999L;
            given(compositeRepository.fetchSchedulerWithSeller(schedulerId))
                    .willReturn(Optional.empty());

            // when
            Optional<CrawlSchedulerDetailResult> result =
                    adapter.findSchedulerDetailById(schedulerId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("태스크가 없는 스케줄러도 정상적으로 조회한다")
        void findSchedulerDetailById_WithNoTasks_ShouldReturnResultWithEmptyTasks() {
            // given
            Long schedulerId = 2L;
            LocalDateTime now = LocalDateTime.of(2025, 11, 20, 10, 30, 0);

            CrawlSchedulerCompositeDto compositeDto =
                    new CrawlSchedulerCompositeDto(
                            2L,
                            100L,
                            "empty-crawl",
                            "0 0 9 * * ?",
                            "ACTIVE",
                            now,
                            now,
                            "TestSeller",
                            "머스트잇셀러");

            CrawlSchedulerDetailResult expectedResult =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(
                                    2L, "empty-crawl", "0 0 9 * * ?", "ACTIVE", null, null),
                            new SellerSummary(100L, "TestSeller", "머스트잇셀러"),
                            new ExecutionInfo(null, null),
                            new SchedulerStatistics(0, 0, 0, 0.0),
                            List.of());

            given(compositeRepository.fetchSchedulerWithSeller(schedulerId))
                    .willReturn(Optional.of(compositeDto));
            given(compositeRepository.fetchRecentTasks(schedulerId, 10)).willReturn(List.of());
            given(compositeRepository.fetchTaskStatistics(schedulerId)).willReturn(List.of());
            given(compositeMapper.toResult(compositeDto, List.of(), List.of()))
                    .willReturn(expectedResult);

            // when
            Optional<CrawlSchedulerDetailResult> result =
                    adapter.findSchedulerDetailById(schedulerId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().recentTasks()).isEmpty();
            assertThat(result.get().statistics().totalTasks()).isZero();
        }
    }
}
