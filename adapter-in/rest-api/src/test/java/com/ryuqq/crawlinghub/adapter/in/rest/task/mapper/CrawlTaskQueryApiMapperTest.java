package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskApiResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.CrawlTaskSearchParams;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskPageResult;
import com.ryuqq.crawlinghub.application.task.dto.response.CrawlTaskResult;
import com.ryuqq.crawlinghub.domain.common.vo.PageMeta;
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
    @DisplayName("toSearchParams() 테스트")
    class ToSearchParamsTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 SearchParams로 변환한다")
        void toSearchParams_WithAllFields_ShouldConvertCorrectly() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(
                            List.of(1L),
                            List.of(100L),
                            List.of("RUNNING"),
                            List.of("MINI_SHOP"),
                            null,
                            null,
                            0,
                            20);

            // when
            CrawlTaskSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.crawlSchedulerIds()).containsExactly(1L);
            assertThat(result.sellerIds()).containsExactly(100L);
            assertThat(result.statuses()).containsExactly("RUNNING");
            assertThat(result.taskTypes()).containsExactly("MINI_SHOP");
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("statuses가 null인 요청을 변환한다")
        void toSearchParams_WithNullStatuses_ShouldConvertWithNullStatuses() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(
                            List.of(1L), null, null, null, null, null, 0, 20);

            // when
            CrawlTaskSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.statuses()).isNull();
        }

        @Test
        @DisplayName("다중 crawlSchedulerIds를 변환한다")
        void toSearchParams_WithMultipleIds_ShouldConvertCorrectly() {
            // given
            SearchCrawlTasksApiRequest request =
                    new SearchCrawlTasksApiRequest(
                            List.of(1L, 2L, 3L),
                            List.of(100L, 200L),
                            null,
                            null,
                            null,
                            null,
                            0,
                            20);

            // when
            CrawlTaskSearchParams result = mapper.toSearchParams(request);

            // then
            assertThat(result.crawlSchedulerIds()).containsExactly(1L, 2L, 3L);
            assertThat(result.sellerIds()).containsExactly(100L, 200L);
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("CrawlTaskResult를 API 응답으로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            CrawlTaskResult result =
                    new CrawlTaskResult(
                            1L,
                            10L,
                            100L,
                            "https://example.com/products?page=1",
                            "https://example.com",
                            "/products",
                            Map.of("page", "1"),
                            "RUNNING",
                            "MINI_SHOP",
                            0,
                            now,
                            now);

            // when
            CrawlTaskApiResponse apiResponse = mapper.toApiResponse(result);

            // then
            assertThat(apiResponse.crawlTaskId()).isEqualTo(1L);
            assertThat(apiResponse.crawlSchedulerId()).isEqualTo(10L);
            assertThat(apiResponse.sellerId()).isEqualTo(100L);
            assertThat(apiResponse.requestUrl()).isEqualTo("https://example.com/products?page=1");
            assertThat(apiResponse.baseUrl()).isEqualTo("https://example.com");
            assertThat(apiResponse.path()).isEqualTo("/products");
            assertThat(apiResponse.queryParams()).containsEntry("page", "1");
            assertThat(apiResponse.status()).isEqualTo("RUNNING");
            assertThat(apiResponse.taskType()).isEqualTo("MINI_SHOP");
            assertThat(apiResponse.retryCount()).isZero();
            assertThat(apiResponse.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("null createdAt을 처리한다")
        void toApiResponse_WithNullCreatedAt_ShouldHandleNullValue() {
            // given
            CrawlTaskResult result =
                    new CrawlTaskResult(
                            1L,
                            10L,
                            100L,
                            "https://example.com",
                            "https://example.com",
                            "",
                            Map.of(),
                            "WAITING",
                            "DETAIL",
                            1,
                            null,
                            null);

            // when
            CrawlTaskApiResponse apiResponse = mapper.toApiResponse(result);

            // then
            assertThat(apiResponse.createdAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toPageApiResponse() 테스트")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("CrawlTaskPageResult를 API 페이지 응답으로 변환한다")
        void toPageApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            List<CrawlTaskResult> items =
                    List.of(
                            new CrawlTaskResult(
                                    1L,
                                    10L,
                                    100L,
                                    "https://example.com/1",
                                    "https://example.com",
                                    "/1",
                                    Map.of(),
                                    "RUNNING",
                                    "MINI_SHOP",
                                    0,
                                    now,
                                    now),
                            new CrawlTaskResult(
                                    2L,
                                    10L,
                                    100L,
                                    "https://example.com/2",
                                    "https://example.com",
                                    "/2",
                                    Map.of(),
                                    "SUCCESS",
                                    "DETAIL",
                                    1,
                                    now,
                                    now));

            PageMeta pageMeta = PageMeta.of(0, 20, 2L);
            CrawlTaskPageResult pageResult = CrawlTaskPageResult.of(items, pageMeta);

            // when
            PageApiResponse<CrawlTaskApiResponse> result = mapper.toPageApiResponse(pageResult);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("빈 페이지 응답을 처리한다")
        void toPageApiResponse_WithEmptyContent_ShouldReturnEmptyPage() {
            // given
            CrawlTaskPageResult pageResult = CrawlTaskPageResult.empty();

            // when
            PageApiResponse<CrawlTaskApiResponse> result = mapper.toPageApiResponse(pageResult);

            // then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }
}
