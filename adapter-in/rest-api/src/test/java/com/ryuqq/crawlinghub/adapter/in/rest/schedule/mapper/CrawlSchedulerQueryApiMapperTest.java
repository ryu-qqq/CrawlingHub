package com.ryuqq.crawlinghub.adapter.in.rest.schedule.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.query.SearchCrawlSchedulersApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response.CrawlSchedulerSummaryApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.schedule.dto.query.SearchCrawlSchedulersQuery;
import com.ryuqq.crawlinghub.application.schedule.dto.response.CrawlSchedulerResponse;
import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
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
    @DisplayName("toQuery() 테스트")
    class ToQueryTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 쿼리로 변환한다")
        void toQuery_WithAllFields_ShouldConvertCorrectly() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(1L, List.of("ACTIVE"), null, null, 0, 20);

            // when
            SearchCrawlSchedulersQuery result = mapper.toQuery(request);

            // then
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.statuses()).containsExactly(SchedulerStatus.ACTIVE);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("status가 null인 요청을 쿼리로 변환한다")
        void toQuery_WithNullStatus_ShouldConvertWithNullStatus() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(1L, null, null, null, 0, 20);

            // when
            SearchCrawlSchedulersQuery result = mapper.toQuery(request);

            // then
            assertThat(result.sellerId()).isEqualTo(1L);
            assertThat(result.statuses()).isNull();
        }

        @Test
        @DisplayName("status가 빈 리스트인 요청을 쿼리로 변환하면 null이 된다")
        void toQuery_WithBlankStatus_ShouldConvertWithNullStatus() {
            // given
            SearchCrawlSchedulersApiRequest request =
                    new SearchCrawlSchedulersApiRequest(1L, List.of(), null, null, 0, 20);

            // when
            SearchCrawlSchedulersQuery result = mapper.toQuery(request);

            // then
            assertThat(result.statuses()).isNull();
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("Application 응답을 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerResponse appResponse =
                    new CrawlSchedulerResponse(
                            1L, 100L, "테스트 스케줄러", "0 0 * * * *", SchedulerStatus.ACTIVE, now, now);

            // when
            CrawlSchedulerApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.schedulerName()).isEqualTo("테스트 스케줄러");
            assertThat(result.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(result.status()).isEqualTo("ACTIVE");
            assertThat(result.createdAt()).isEqualTo(now.toString());
            assertThat(result.updatedAt()).isEqualTo(now.toString());
        }

        @Test
        @DisplayName("null 시각 필드를 처리한다")
        void toApiResponse_WithNullInstants_ShouldHandleNullValues() {
            // given
            CrawlSchedulerResponse appResponse =
                    new CrawlSchedulerResponse(
                            1L,
                            100L,
                            "테스트 스케줄러",
                            "0 0 * * * *",
                            SchedulerStatus.INACTIVE,
                            null,
                            null);

            // when
            CrawlSchedulerApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.createdAt()).isNull();
            assertThat(result.updatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toSummaryApiResponse() 테스트")
    class ToSummaryApiResponseTests {

        @Test
        @DisplayName("Application 응답을 요약 API 응답으로 변환한다")
        void toSummaryApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlSchedulerResponse appResponse =
                    new CrawlSchedulerResponse(
                            1L, 100L, "테스트 스케줄러", "0 0 * * * *", SchedulerStatus.ACTIVE, now, now);

            // when
            CrawlSchedulerSummaryApiResponse result = mapper.toSummaryApiResponse(appResponse);

            // then
            assertThat(result.crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.schedulerName()).isEqualTo("테스트 스케줄러");
            assertThat(result.cronExpression()).isEqualTo("0 0 * * * *");
            assertThat(result.status()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("toPageApiResponse() 테스트")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("페이지 응답을 API 페이지 응답으로 변환한다")
        void toPageApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            List<CrawlSchedulerResponse> items =
                    List.of(
                            new CrawlSchedulerResponse(
                                    1L,
                                    100L,
                                    "스케줄러1",
                                    "0 0 * * * *",
                                    SchedulerStatus.ACTIVE,
                                    now,
                                    now),
                            new CrawlSchedulerResponse(
                                    2L,
                                    100L,
                                    "스케줄러2",
                                    "0 30 * * * *",
                                    SchedulerStatus.INACTIVE,
                                    now,
                                    now));
            PageResponse<CrawlSchedulerResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 2L, 1, true, true);

            // when
            PageApiResponse<CrawlSchedulerSummaryApiResponse> result =
                    mapper.toPageApiResponse(pageResponse);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("빈 페이지 응답을 처리한다")
        void toPageApiResponse_WithEmptyContent_ShouldReturnEmptyPage() {
            // given
            PageResponse<CrawlSchedulerResponse> pageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // when
            PageApiResponse<CrawlSchedulerSummaryApiResponse> result =
                    mapper.toPageApiResponse(pageResponse);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }
}
