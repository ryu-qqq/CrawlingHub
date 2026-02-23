package com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerCompositeDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskStatisticsDto;
import com.ryuqq.crawlinghub.adapter.out.persistence.composite.schedule.dto.CrawlSchedulerTaskSummaryDto;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerCompositeMapper 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("persistence")
@Tag("mapper")
@DisplayName("CrawlSchedulerCompositeMapper 단위 테스트")
class CrawlSchedulerCompositeMapperTest {

    private CrawlSchedulerCompositeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerCompositeMapper();
    }

    @Nested
    @DisplayName("toResult() 테스트")
    class ToResultTests {

        @Test
        @DisplayName("모든 데이터가 있는 경우 정상적으로 변환한다")
        void toResult_WithAllData_ShouldConvertCorrectly() {
            // given
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
                                    1001L, "SUCCESS", "SEARCH", now.minusHours(1), now),
                            new CrawlSchedulerTaskSummaryDto(1002L, "RUNNING", "SEARCH", now, now));

            List<CrawlSchedulerTaskStatisticsDto> taskStatistics =
                    List.of(
                            new CrawlSchedulerTaskStatisticsDto("SUCCESS", 145),
                            new CrawlSchedulerTaskStatisticsDto("FAILED", 3),
                            new CrawlSchedulerTaskStatisticsDto("TIMEOUT", 2));

            // when
            CrawlSchedulerDetailResult result =
                    mapper.toResult(compositeDto, taskSummaries, taskStatistics);

            // then
            assertThat(result.scheduler().id()).isEqualTo(1L);
            assertThat(result.scheduler().schedulerName()).isEqualTo("daily-crawl");
            assertThat(result.scheduler().status()).isEqualTo("ACTIVE");

            assertThat(result.seller()).isNotNull();
            assertThat(result.seller().sellerId()).isEqualTo(100L);
            assertThat(result.seller().sellerName()).isEqualTo("TestSeller");
            assertThat(result.seller().mustItSellerName()).isEqualTo("머스트잇셀러");

            assertThat(result.execution().lastExecutionStatus()).isEqualTo("SUCCESS");

            assertThat(result.statistics().totalTasks()).isEqualTo(150);
            assertThat(result.statistics().successTasks()).isEqualTo(145);
            assertThat(result.statistics().failedTasks()).isEqualTo(5);
            assertThat(result.statistics().successRate()).isGreaterThan(0.96);

            assertThat(result.recentTasks()).hasSize(2);
        }

        @Test
        @DisplayName("셀러 정보가 없는 경우 seller가 null이다")
        void toResult_WithoutSeller_ShouldReturnNullSeller() {
            // given
            LocalDateTime now = LocalDateTime.of(2025, 11, 20, 10, 30, 0);
            CrawlSchedulerCompositeDto compositeDto =
                    new CrawlSchedulerCompositeDto(
                            1L, 100L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", now, now, null, null);

            // when
            CrawlSchedulerDetailResult result = mapper.toResult(compositeDto, List.of(), List.of());

            // then
            assertThat(result.seller()).isNull();
        }

        @Test
        @DisplayName("태스크가 없는 경우 빈 목록과 기본 통계를 반환한다")
        void toResult_WithNoTasks_ShouldReturnEmptyListsAndDefaultStats() {
            // given
            LocalDateTime now = LocalDateTime.of(2025, 11, 20, 10, 30, 0);
            CrawlSchedulerCompositeDto compositeDto =
                    new CrawlSchedulerCompositeDto(
                            1L,
                            100L,
                            "daily-crawl",
                            "0 0 9 * * ?",
                            "ACTIVE",
                            now,
                            now,
                            "TestSeller",
                            "머스트잇셀러");

            // when
            CrawlSchedulerDetailResult result = mapper.toResult(compositeDto, List.of(), List.of());

            // then
            assertThat(result.recentTasks()).isEmpty();
            assertThat(result.execution().lastExecutionTime()).isNull();
            assertThat(result.execution().lastExecutionStatus()).isNull();
            assertThat(result.statistics().totalTasks()).isZero();
            assertThat(result.statistics().successRate()).isZero();
        }

        @Test
        @DisplayName("완료된 태스크의 completedAt이 설정된다")
        void toResult_WithCompletedTask_ShouldSetCompletedAt() {
            // given
            LocalDateTime now = LocalDateTime.of(2025, 11, 20, 10, 30, 0);
            CrawlSchedulerCompositeDto compositeDto =
                    new CrawlSchedulerCompositeDto(
                            1L, 100L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", now, now, null, null);

            List<CrawlSchedulerTaskSummaryDto> taskSummaries =
                    List.of(
                            new CrawlSchedulerTaskSummaryDto(
                                    1L, "SUCCESS", "SEARCH", now.minusHours(1), now),
                            new CrawlSchedulerTaskSummaryDto(2L, "RUNNING", "SEARCH", now, now));

            // when
            CrawlSchedulerDetailResult result =
                    mapper.toResult(compositeDto, taskSummaries, List.of());

            // then
            assertThat(result.recentTasks().get(0).completedAt()).isNotNull();
            assertThat(result.recentTasks().get(1).completedAt()).isNull();
        }
    }
}
