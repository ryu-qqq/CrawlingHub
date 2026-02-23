package com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.mapper.CrawlTaskOutboxApiMapper;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * OutboxApiMapper 단위 테스트
 *
 * <p>Outbox API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("OutboxApiMapper 단위 테스트")
class CrawlTaskOutboxApiMapperTest {

    private CrawlTaskOutboxApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CrawlTaskOutboxApiMapper();
    }

    @Nested
    @DisplayName("toQuery() 테스트")
    class ToQueryTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 쿼리로 변환한다")
        void toQuery_WithAllFields_ShouldConvertCorrectly() {
            List<String> statuses = List.of("PENDING", "FAILED");
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(statuses, createdFrom, createdTo, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
            assertThat(result.createdFrom()).isEqualTo(createdFrom);
            assertThat(result.createdTo()).isEqualTo(createdTo);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("statuses가 null인 요청을 쿼리로 변환한다 (기본값 PENDING, FAILED)")
        void toQuery_WithNullStatuses_ShouldUseDefaults() {
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(null, createdFrom, createdTo, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("빈 statuses 리스트를 쿼리로 변환한다 (기본값 PENDING, FAILED)")
        void toQuery_WithEmptyStatuses_ShouldUseDefaults() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(List.of(), null, null, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("날짜 필터가 null인 요청을 쿼리로 변환한다")
        void toQuery_WithNullDateRange_ShouldConvertWithNullDates() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(List.of("SENT"), null, null, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.createdFrom()).isNull();
            assertThat(result.createdTo()).isNull();
        }

        @Test
        @DisplayName("단일 상태 필터를 쿼리로 변환한다")
        void toQuery_WithSingleStatus_ShouldConvertCorrectly() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(List.of("SENT"), null, null, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.statuses()).containsExactly(OutboxStatus.SENT);
        }

        @Test
        @DisplayName("소문자 상태 값도 변환한다 (case-insensitive)")
        void toQuery_WithLowercaseStatus_ShouldConvertCorrectly() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(
                            List.of("pending", "failed"), null, null, 0, 20);

            GetOutboxListQuery result = mapper.toQuery(request);

            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("유효하지 않은 상태 값은 IllegalArgumentException을 발생시킨다")
        void toQuery_WithInvalidStatus_ShouldThrowException() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(
                            List.of("INVALID_STATUS"), null, null, 0, 20);

            assertThatThrownBy(() -> mapper.toQuery(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid outbox status: 'INVALID_STATUS'")
                    .hasMessageContaining("Valid values:")
                    .hasMessageContaining("PENDING")
                    .hasMessageContaining("FAILED")
                    .hasMessageContaining("SENT");
        }

        @Test
        @DisplayName("빈 문자열 상태 값은 IllegalArgumentException을 발생시킨다")
        void toQuery_WithBlankStatus_ShouldThrowException() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(List.of("  "), null, null, 0, 20);

            assertThatThrownBy(() -> mapper.toQuery(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status cannot be null or blank");
        }

        @Test
        @DisplayName("여러 상태 중 하나가 유효하지 않으면 IllegalArgumentException을 발생시킨다")
        void toQuery_WithOneInvalidStatus_ShouldThrowException() {
            SearchCrawlTasksOutboxApiRequest request =
                    new SearchCrawlTasksOutboxApiRequest(
                            List.of("PENDING", "UNKNOWN"), null, null, 0, 20);

            assertThatThrownBy(() -> mapper.toQuery(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid outbox status: 'UNKNOWN'");
        }
    }

    @Nested
    @DisplayName("toApiResponse() 테스트")
    class ToApiResponseTests {

        @Test
        @DisplayName("OutboxResponse를 OutboxApiResponse로 변환한다")
        void toApiResponse_ShouldConvertCorrectly() {
            Instant now = Instant.now();
            OutboxResponse response =
                    new OutboxResponse(
                            1L, "idempotency-key-123", OutboxStatus.PENDING, 0, now, null, null);

            CrawlTaskOutboxApiResponse result = mapper.toApiResponse(response);

            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.idempotencyKey()).isEqualTo("idempotency-key-123");
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.retryCount()).isZero();
            assertThat(result.createdAt()).isEqualTo(format(now));
            assertThat(result.updatedAt()).isNull();
            assertThat(result.processedAt()).isNull();
        }

        @Test
        @DisplayName("processedAt이 있는 OutboxResponse를 변환한다")
        void toApiResponse_WithProcessedAt_ShouldConvertCorrectly() {
            Instant now = Instant.now();
            Instant processedAt = now.plusSeconds(60);
            OutboxResponse response =
                    new OutboxResponse(2L, "key-456", OutboxStatus.SENT, 1, now, now, processedAt);

            CrawlTaskOutboxApiResponse result = mapper.toApiResponse(response);

            assertThat(result.crawlTaskId()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo("SENT");
            assertThat(result.retryCount()).isEqualTo(1);
            assertThat(result.updatedAt()).isEqualTo(format(now));
            assertThat(result.processedAt()).isEqualTo(format(processedAt));
        }
    }

    @Nested
    @DisplayName("toPageApiResponse() 테스트")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("PageResponse를 PageApiResponse로 변환한다")
        void toPageApiResponse_ShouldConvertCorrectly() {
            Instant now = Instant.now();
            OutboxResponse response1 =
                    new OutboxResponse(1L, "key-1", OutboxStatus.PENDING, 0, now, null, null);
            OutboxResponse response2 =
                    new OutboxResponse(2L, "key-2", OutboxStatus.FAILED, 2, now, null, null);

            PageResponse<OutboxResponse> pageResponse =
                    new PageResponse<>(List.of(response1, response2), 0, 20, 2, 1, true, true);

            PageApiResponse<CrawlTaskOutboxApiResponse> result =
                    mapper.toPageApiResponse(pageResponse);

            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();

            assertThat(result.content().get(0).crawlTaskId()).isEqualTo(1L);
            assertThat(result.content().get(1).crawlTaskId()).isEqualTo(2L);
        }
    }
}
