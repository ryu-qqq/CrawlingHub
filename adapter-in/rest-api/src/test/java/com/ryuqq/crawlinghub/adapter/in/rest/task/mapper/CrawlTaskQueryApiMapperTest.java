package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskDetailApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetCrawlTaskQuery;
import com.ryuqq.crawlinghub.application.task.dto.query.ListCrawlTasksQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskDetailResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResponse;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlTaskQueryApiMapper 단위 테스트
 *
 * <p>CrawlTask Query API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("CrawlTaskQueryApiMapper 단위 테스트")
class CrawlTaskQueryApiMapperTest {

    private CrawlTaskQueryApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlTaskQueryApiMapper();
    }

    @Nested
    @DisplayName("toQuery() 테스트")
    class ToQueryTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 쿼리로 변환한다")
        void toQuery_WithAllFields_ShouldConvertCorrectly() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(
                            1L, 100L, List.of("RUNNING"), List.of("MINI_SHOP"), null, null, 0, 20);

            // when
            ListCrawlTasksQuery result = mapper.toQuery(request);

            // then
            assertThat(result.crawlSchedulerId()).isEqualTo(1L);
            assertThat(result.statuses()).containsExactly(CrawlTaskStatus.RUNNING);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("statuses가 null인 요청을 쿼리로 변환한다")
        void toQuery_WithNullStatuses_ShouldConvertWithNullStatuses() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(1L, null, null, null, null, null, 0, 20);

            // when
            ListCrawlTasksQuery result = mapper.toQuery(request);

            // then
            assertThat(result.statuses()).isNull();
        }

        @Test
        @DisplayName("statuses가 빈 리스트인 요청을 쿼리로 변환하면 null이 된다")
        void toQuery_WithEmptyStatuses_ShouldConvertWithNullStatuses() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(1L, null, List.of(), null, null, null, 0, 20);

            // when
            ListCrawlTasksQuery result = mapper.toQuery(request);

            // then
            assertThat(result.statuses()).isNull();
        }
    }

    @Nested
    @DisplayName("toGetQuery() 테스트")
    class ToGetQueryTests {

        @Test
        @DisplayName("crawlTaskId로 조회 쿼리를 생성한다")
        void toGetQuery_ShouldCreateQuery() {
            // given
            Long crawlTaskId = 123L;

            // when
            GetCrawlTaskQuery result = mapper.toGetQuery(crawlTaskId);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(123L);
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
            CrawlTaskResponse appResponse =
                    new CrawlTaskResponse(
                            1L,
                            10L,
                            100L,
                            "https://example.com",
                            CrawlTaskStatus.RUNNING,
                            CrawlTaskType.MINI_SHOP,
                            0,
                            now);

            // when
            CrawlTaskApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.crawlSchedulerId()).isEqualTo(10L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.requestUrl()).isEqualTo("https://example.com");
            assertThat(result.status()).isEqualTo("RUNNING");
            assertThat(result.taskType()).isEqualTo("MINI_SHOP");
            assertThat(result.retryCount()).isZero();
            assertThat(result.createdAt()).isEqualTo(now.toString());
        }

        @Test
        @DisplayName("null createdAt을 처리한다")
        void toApiResponse_WithNullCreatedAt_ShouldHandleNullValue() {
            // given
            CrawlTaskResponse appResponse =
                    new CrawlTaskResponse(
                            1L,
                            10L,
                            100L,
                            "https://example.com",
                            CrawlTaskStatus.WAITING,
                            CrawlTaskType.DETAIL,
                            1,
                            null);

            // when
            CrawlTaskApiResponse result = mapper.toApiResponse(appResponse);

            // then
            assertThat(result.createdAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDetailApiResponse() 테스트")
    class ToDetailApiResponseTests {

        @Test
        @DisplayName("상세 응답을 API 응답으로 변환한다")
        void toDetailApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            Map<String, String> queryParams = Map.of("page", "1", "size", "20");
            CrawlTaskDetailResponse appResponse =
                    new CrawlTaskDetailResponse(
                            1L,
                            10L,
                            100L,
                            CrawlTaskStatus.SUCCESS,
                            CrawlTaskType.OPTION,
                            0,
                            "https://example.com",
                            "/api/products",
                            queryParams,
                            "https://example.com/api/products?page=1&size=20",
                            now,
                            now);

            // when
            CrawlTaskDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.crawlSchedulerId()).isEqualTo(10L);
            assertThat(result.sellerId()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo("SUCCESS");
            assertThat(result.taskType()).isEqualTo("OPTION");
            assertThat(result.retryCount()).isZero();
            assertThat(result.baseUrl()).isEqualTo("https://example.com");
            assertThat(result.path()).isEqualTo("/api/products");
            assertThat(result.queryParams()).containsEntry("page", "1");
            assertThat(result.fullUrl())
                    .isEqualTo("https://example.com/api/products?page=1&size=20");
            assertThat(result.createdAt()).isEqualTo(now.toString());
            assertThat(result.updatedAt()).isEqualTo(now.toString());
        }

        @Test
        @DisplayName("null 시각 필드를 처리한다")
        void toDetailApiResponse_WithNullInstants_ShouldHandleNullValues() {
            // given
            CrawlTaskDetailResponse appResponse =
                    new CrawlTaskDetailResponse(
                            1L,
                            10L,
                            100L,
                            CrawlTaskStatus.FAILED,
                            CrawlTaskType.META,
                            3,
                            "https://example.com",
                            "/api",
                            Map.of(),
                            "https://example.com/api",
                            null,
                            null);

            // when
            CrawlTaskDetailApiResponse result = mapper.toDetailApiResponse(appResponse);

            // then
            assertThat(result.createdAt()).isNull();
            assertThat(result.updatedAt()).isNull();
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
            List<CrawlTaskResponse> items =
                    List.of(
                            new CrawlTaskResponse(
                                    1L,
                                    10L,
                                    100L,
                                    "https://example.com/1",
                                    CrawlTaskStatus.RUNNING,
                                    CrawlTaskType.MINI_SHOP,
                                    0,
                                    now),
                            new CrawlTaskResponse(
                                    2L,
                                    10L,
                                    100L,
                                    "https://example.com/2",
                                    CrawlTaskStatus.SUCCESS,
                                    CrawlTaskType.DETAIL,
                                    1,
                                    now));
            PageResponse<CrawlTaskResponse> pageResponse =
                    new PageResponse<>(items, 0, 20, 2L, 1, true, true);

            // when
            PageApiResponse<CrawlTaskApiResponse> result = mapper.toPageApiResponse(pageResponse);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 페이지 응답을 처리한다")
        void toPageApiResponse_WithEmptyContent_ShouldReturnEmptyPage() {
            // given
            PageResponse<CrawlTaskResponse> pageResponse =
                    new PageResponse<>(List.of(), 0, 20, 0L, 0, true, true);

            // when
            PageApiResponse<CrawlTaskApiResponse> result = mapper.toPageApiResponse(pageResponse);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }
}
