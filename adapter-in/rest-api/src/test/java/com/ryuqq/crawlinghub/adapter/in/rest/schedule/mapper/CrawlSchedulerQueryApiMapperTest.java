package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerDetailApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.ExecutionInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerInfo;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SchedulerStatistics;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.SellerSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.composite.CrawlSchedulerDetailResult.TaskSummary;
import com.ryuqq.crawlinghub.application.schedule.dto.query.CrawlSchedulerSearchParams;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerPageResult;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlSchedulerQueryApiMapper 단위 테스트
 *
 * <p>CrawlScheduler Query API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawlSchedulerQueryApiMapper 단위 테스트")
class CrawlSchedulerQueryApiMapperTest {

    private CrawlSchedulerQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlSchedulerQueryApiMapper();
    }

    @Nested
    @DisplayName("toSearchParams() 테스트")
    class ToSearchParamsTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 SearchParams로 변환한다")
        void toSearchParams_WithAllFields_ShouldConvertCorrectly() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(
                            1L,
                            List.of("ACTIVE"),
                            "schedulerName",
                            "daily",
                            "createdAt",
                            "DESC",
                            0,
                            20);

            // when
            CrawlSchedulerSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.statuses()).containsExactly("ACTIVE");
            assertThat(result.searchField()).isEqualTo("schedulerName");
            assertThat(result.searchWord()).isEqualTo("daily");
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.sortKey()).isEqualTo("createdAt");
            assertThat(result.sortDirection()).isEqualTo("DESC");
        }

        @Test
        @DisplayName("status가 null인 요청을 SearchParams로 변환한다")
        void toSearchParams_WithNullStatus_ShouldConvertWithNullStatus() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(1L, null, null, null, null, null, 0, 20);

            // when
            CrawlSchedulerSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.statuses()).isNull();
            assertThat(result.searchField()).isNull();
            assertThat(result.searchWord()).isNull();
        }

        @Test
        @DisplayName("기본 페이징 값이 적용된다")
        void toSearchParams_WithDefaultPaging_ShouldApplyDefaults() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(
                            null, null, null, null, null, null, null, null);

            // when
            CrawlSchedulerSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse() 테스트")
    class ToSummaryApiResponseTests {

        @Test
        @DisplayName("CrawlSchedulerResult를 요약 API 응답으로 변환한다")
        void toSummaryApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerResult result =
                    new CrawlSchedulerResult(
                            1L, 100L, "테스트 스케줄러", "0 0 * * * *", "ACTIVE", now, now);

            // when
            CrawlSchedulerSummaryApiResponse apiResponse = mapper.toSummaryApiResponse(result);

            // then
            assertThat(apiResponse.crawlSchedulerId()).isEqualTo(1L);
            assertThat(apiResponse.sellerId()).isEqualTo(100L);
            assertThat(apiResponse.schedulerName()).isEqualTo("테스트 스케줄러");
            assertThat(apiResponse.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(apiResponse.status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponseTests {

        @Test
        @DisplayName("CrawlSchedulerPageResult를 PageApiResponse로 변환한다")
        void toPageResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            List<CrawlSchedulerResult> results =
                    List.of(
                            new CrawlSchedulerResult(
                                    1L, 100L, "스케줄러1", "0 0 * * * *", "ACTIVE", now, now),
                            new CrawlSchedulerResult(
                                    2L, 100L, "스케줄러2", "0 30 * * * *", "INACTIVE", now, now));

            CrawlSchedulerPageResult pageResult =
                    CrawlSchedulerPageResult.of(results, PageMeta.of(0, 20, 2L));

            // when
            PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

            // then
            assertThat(apiPageResponse.content()).hasSize(2);
            assertThat(apiPageResponse.page()).isZero();
            assertThat(apiPageResponse.size()).isEqualTo(20);
            assertThat(apiPageResponse.totalElements()).isEqualTo(2L);
            assertThat(apiPageResponse.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 페이지 결과를 처리한다")
        void toPageResponse_WithEmptyContent_ShouldReturnEmptyPage() {
            // given
            CrawlSchedulerPageResult pageResult =
                    CrawlSchedulerPageResult.of(List.of(), PageMeta.of(0, 20, 0L));

            // when
            PageApiResponse<CrawlSchedulerSummaryApiResponse> apiPageResponse =
                    mapper.toPageResponse(pageResult);

            // then
            assertThat(apiPageResponse.content()).isEmpty();
            assertThat(apiPageResponse.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("toDetailApiResponse() 테스트")
    class ToDetailApiResponseTests {

        @Test
        @DisplayName("CrawlSchedulerDetailResult를 상세 API 응답으로 변환한다")
        void toDetailApiResponse_WithAllData_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.parse("2025-11-20T10:30:00Z");
            CrawlSchedulerDetailResult detailResult =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(
                                    1L,
                                    "daily-crawl",
                                    "0 0 9 * * ?",
                                    "ACTIVE",
                                    now.minusSeconds(86400 * 30),
                                    now),
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

            // when
            CrawlSchedulerDetailApiResponse response = mapper.toDetailApiResponse(detailResult);

            // then
            assertThat(response.crawlSchedulerId()).isEqualTo(1L);
            assertThat(response.schedulerName()).isEqualTo("daily-crawl");
            assertThat(response.cronExpression()).isEqualTo("0 0 9 * * ?");
            assertThat(response.status()).isEqualTo("ACTIVE");
            assertThat(response.createdAt()).isNotNull();
            assertThat(response.updatedAt()).isNotNull();

            assertThat(response.seller()).isNotNull();
            assertThat(response.seller().sellerId()).isEqualTo(100L);
            assertThat(response.seller().sellerName()).isEqualTo("TestSeller");
            assertThat(response.seller().mustItSellerName()).isEqualTo("머스트잇셀러");

            assertThat(response.execution()).isNotNull();
            assertThat(response.execution().lastExecutionStatus()).isEqualTo("SUCCESS");
            assertThat(response.execution().lastExecutionTime()).isNotNull();

            assertThat(response.statistics()).isNotNull();
            assertThat(response.statistics().totalTasks()).isEqualTo(150);
            assertThat(response.statistics().successTasks()).isEqualTo(145);
            assertThat(response.statistics().failedTasks()).isEqualTo(5);
            assertThat(response.statistics().successRate()).isGreaterThan(0.96);

            assertThat(response.recentTasks()).hasSize(1);
            assertThat(response.recentTasks().get(0).taskId()).isEqualTo(1001L);
            assertThat(response.recentTasks().get(0).status()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("셀러 정보가 null이면 seller가 null이다")
        void toDetailApiResponse_WithNullSeller_ShouldReturnNullSeller() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerDetailResult detailResult =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(1L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", now, now),
                            null,
                            new ExecutionInfo(null, null),
                            new SchedulerStatistics(0, 0, 0, 0.0),
                            List.of());

            // when
            CrawlSchedulerDetailApiResponse response = mapper.toDetailApiResponse(detailResult);

            // then
            assertThat(response.seller()).isNull();
        }

        @Test
        @DisplayName("태스크가 null이면 빈 목록으로 변환한다")
        void toDetailApiResponse_WithNullTasks_ShouldReturnEmptyList() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerDetailResult detailResult =
                    new CrawlSchedulerDetailResult(
                            new SchedulerInfo(1L, "daily-crawl", "0 0 9 * * ?", "ACTIVE", now, now),
                            null,
                            null,
                            null,
                            null);

            // when
            CrawlSchedulerDetailApiResponse response = mapper.toDetailApiResponse(detailResult);

            // then
            assertThat(response.recentTasks()).isEmpty();
            assertThat(response.execution()).isNull();
            assertThat(response.statistics()).isNull();
        }
    }
}
