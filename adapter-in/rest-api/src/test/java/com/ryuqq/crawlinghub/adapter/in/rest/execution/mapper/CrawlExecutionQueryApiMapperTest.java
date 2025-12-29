package com.ryuqq.crawlinghub.adapter.in.rest.execution.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.query.SearchCrawlExecutionsApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.execution.dto.response.CrawlExecutionDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.GetCrawlExecutionQuery;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlExecutionQueryApiMapper 단위 테스트
 *
 * <p>CrawlExecution Query API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawlExecutionQueryApiMapper 단위 테스트")
class CrawlExecutionQueryApiMapperTest {

    private CrawlExecutionQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlExecutionQueryApiMapper();
    }

    @Nested
    @DisplayName("toQuery() 테스트")
    class ToQueryTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 쿼리로 변환한다")
        void toQuery_WithAllFields_ShouldConvertCorrectly() {
            // given
            LocalDateTime from = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
            LocalDateTime to = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
            SearchCrawlExecutionsApiRequest request =
                    new SearchCrawlExecutionsApiRequest(
                            1L, 10L, 100L, List.of("SUCCESS"), from, to, 0, 20);

            // when
            ListCrawlExecutionsQuery result = mapper.toQuery(request);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.crawlSchedulerId()).isEqualTo(10L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.statuses()).containsExactly(CrawlExecutionStatus.SUCCESS);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("statuses가 null인 요청을 쿼리로 변환한다")
        void toQuery_WithNullStatuses_ShouldConvertWithNullStatuses() {
            // given
            SearchCrawlExecutionsApiRequest request =
                    new SearchCrawlExecutionsApiRequest(1L, 10L, 100L, null, null, null, 0, 20);

            // when
            ListCrawlExecutionsQuery result = mapper.toQuery(request);

            // then
            assertThat(result.statuses()).isNull();
        }

        @Test
        @DisplayName("FAILED 상태로 요청을 변환한다")
        void toQuery_WithFailedStatus_ShouldConvertCorrectly() {
            // given
            SearchCrawlExecutionsApiRequest request =
                    new SearchCrawlExecutionsApiRequest(
                            1L, null, null, List.of("FAILED"), null, null, 0, 20);

            // when
            ListCrawlExecutionsQuery result = mapper.toQuery(request);

            // then
            assertThat(result.statuses()).containsExactly(CrawlExecutionStatus.FAILED);
        }
    }

    @Nested
    @DisplayName("toGetQuery() 테스트")
    class ToGetQueryTests {

        @Test
        @DisplayName("crawlExecutionId로 조회 쿼리를 생성한다")
        void toGetQuery_ShouldCreateQuery() {
            // given
            Long crawlExecutionId = 123L;

            // when
            GetCrawlExecutionQuery result = mapper.toGetQuery(crawlExecutionId);

            // then
            assertThat(result.crawlExecutionId()).isEqualTo(123L);
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("Application 응답을 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant startedAt = Instant.now();
            Instant completedAt = startedAt.plusSeconds(5);
            CrawlExecutionResponse appResponse =
                    new CrawlExecutionResponse(
                            1L,
                            10L,
                            100L,
                            1000L,
                            CrawlExecutionStatus.SUCCESS,
                            200,
                            5000L,
                            startedAt,
                            completedAt);

            // when
            CrawlExecutionApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.crawlExecutionId()).isEqualTo(1L);
            assertThat(result.crawlTaskId()).isEqualTo(10L);
            assertThat(result.crawlSchedulerId()).isEqualTo(100L);
            assertThat(result.sellerId()).isEqualTo(1000L);
            assertThat(result.status()).isEqualTo("SUCCESS");
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.durationMs()).isEqualTo(5000L);
            assertThat(result.startedAt()).isEqualTo(startedAt.toString());
            assertThat(result.completedAt()).isEqualTo(completedAt.toString());
        }

        @Test
        @DisplayName("null 필드를 처리한다")
        void toApiResponse_WithNullFields_ShouldHandleNullValues() {
            // given
            Instant startedAt = Instant.now();
            CrawlExecutionResponse appResponse =
                    new CrawlExecutionResponse(
                            1L, 10L, 100L, 1000L, null, null, null, startedAt, null);

            // when
            CrawlExecutionApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.status()).isNull();
            assertThat(result.httpStatusCode()).isNull();
            assertThat(result.durationMs()).isNull();
            assertThat(result.completedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDetailApiResponse() 테스트")
    class ToDetailApiResponseTests {

        @Test
        @DisplayName("상세 응답을 API 응답으로 변환한다")
        void toDetailApiResponse_ShouldConvertCorrectly() {
            // given
            Instant startedAt = Instant.now();
            Instant completedAt = startedAt.plusSeconds(3);
            CrawlExecutionDetailResponse appResponse =
                    new CrawlExecutionDetailResponse(
                            1L,
                            10L,
                            100L,
                            1000L,
                            CrawlExecutionStatus.SUCCESS,
                            200,
                            "{\"data\":\"test\"}",
                            null,
                            3000L,
                            startedAt,
                            completedAt);

            // when
            CrawlExecutionDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.crawlExecutionId()).isEqualTo(1L);
            assertThat(result.crawlTaskId()).isEqualTo(10L);
            assertThat(result.crawlSchedulerId()).isEqualTo(100L);
            assertThat(result.sellerId()).isEqualTo(1000L);
            assertThat(result.status()).isEqualTo("SUCCESS");
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).isEqualTo("{\"data\":\"test\"}");
            assertThat(result.errorMessage()).isNull();
            assertThat(result.durationMs()).isEqualTo(3000L);
        }

        @Test
        @DisplayName("실패 응답의 에러 메시지를 처리한다")
        void toDetailApiResponse_WithError_ShouldIncludeErrorMessage() {
            // given
            Instant startedAt = Instant.now();
            CrawlExecutionDetailResponse appResponse =
                    new CrawlExecutionDetailResponse(
                            1L,
                            10L,
                            100L,
                            1000L,
                            CrawlExecutionStatus.FAILED,
                            500,
                            null,
                            "Connection timeout",
                            1000L,
                            startedAt,
                            null);

            // when
            CrawlExecutionDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.status()).isEqualTo("FAILED");
            assertThat(result.errorMessage()).isEqualTo("Connection timeout");
            assertThat(result.responseBody()).isNull();
        }
    }

    @Nested
    @DisplayName("toPageApiResponse() 테스트")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("페이지 응답을 API 페이지 응답으로 변환한다")
        void toPageApiResponse_ShouldConvertCorrectly() {
            // given
            Instant startedAt = Instant.now();
            List<CrawlExecutionResponse> items =
                    List.of(
                            new CrawlExecutionResponse(
                                    1L,
                                    10L,
                                    100L,
                                    1000L,
                                    CrawlExecutionStatus.SUCCESS,
                                    200,
                                    5000L,
                                    startedAt,
                                    startedAt.plusSeconds(5)),
                            new CrawlExecutionResponse(
                                    2L,
                                    10L,
                                    100L,
                                    1000L,
                                    CrawlExecutionStatus.FAILED,
                                    500,
                                    1000L,
                                    startedAt,
                                    null));
            PageResponse<CrawlExecutionResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 2L, 1, true, true);

            // when
            PageApiResponse<CrawlExecutionApiResponse> result =
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
            PageResponse<CrawlExecutionResponse> pageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // when
            PageApiResponse<CrawlExecutionApiResponse> result =
                    mapper.toPageApiResponse(pageResponse);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }
}
