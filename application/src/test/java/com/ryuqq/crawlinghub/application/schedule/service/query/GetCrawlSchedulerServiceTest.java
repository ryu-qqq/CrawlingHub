package com.ryuqq.crawlinghub.application.schedule.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SellerSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.TaskSummary;
import com.ryuqq.crawlinghub.application.schedule.manager.CrawlSchedulerCompositionReadManager;
import com.ryuqq.crawlinghub.domain.schedule.exception.CrawlSchedulerNotFoundException;
import java.time.Instant;
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
 * GetCrawlSchedulerService 단위 테스트
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@Tag("service")
@ExtendWith(MockitoExtension.class)
@DisplayName("GetCrawlSchedulerService 단위 테스트")
class GetCrawlSchedulerServiceTest {

    @Mock private CrawlSchedulerCompositionReadManager compositionReadManager;

    @InjectMocks private GetCrawlSchedulerService service;

    @Nested
    @DisplayName("execute() 테스트")
    class ExecuteTests {

        @Test
        @DisplayName("스케줄러 ID로 상세 정보를 조회한다")
        void execute_WithValidId_ShouldReturnDetailResult() {
            // given
            Long schedulerId = 1L;
            Instant now = Instant.now();
            CrawlSchedulerDetailResult expected =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(1L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", now, now),
                            new SellerSummary(100L, "TestSeller", "머스트잇셀러"),
                            new ExecutionInfo(now, "SUCCESS"),
                            new SchedulerStatistics(150, 145, 5, 0.9667),
                            List.of(
                                    new TaskSummary(
                                            1001L,
                                            "SUCCESS",
                                            "SEARCH",
                                            now.minusSeconds(3600),
                                            now)));

            given(compositionReadManager.getSchedulerDetail(schedulerId)).willReturn(expected);

            // when
            CrawlSchedulerDetailResult result = service.execute(schedulerId);

            // then
            assertThat(result).isEqualTo(expected);
            assertThat(result.scheduler().id()).isEqualTo(1L);
            assertThat(result.scheduler().schedulerName()).isEqualTo("daily-crawl");
            assertThat(result.seller().sellerId()).isEqualTo(100L);
            assertThat(result.statistics().totalTasks()).isEqualTo(150);
            assertThat(result.recentTasks()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 스케줄러 ID로 조회 시 예외가 발생한다")
        void execute_WithNonExistentId_ShouldThrowException() {
            // given
            Long schedulerId = 999L;
            given(compositionReadManager.getSchedulerDetail(schedulerId))
                    .willThrow(new CrawlSchedulerNotFoundException(schedulerId));

            // when & then
            assertThatThrownBy(() -> service.execute(schedulerId))
                    .isInstanceOf(CrawlSchedulerNotFoundException.class);
        }
    }
}
