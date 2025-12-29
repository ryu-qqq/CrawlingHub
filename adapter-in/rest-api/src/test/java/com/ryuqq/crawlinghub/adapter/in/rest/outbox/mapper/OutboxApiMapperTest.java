package com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
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
class OutboxApiMapperTest {

    private OutboxApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OutboxApiMapper();
    }

    @Nested
    @DisplayName("toQuery() 테스트")
    class ToQueryTests {

        @Test
        @DisplayName("모든 필드가 있는 요청을 쿼리로 변환한다")
        void toQuery_WithAllFields_ShouldConvertCorrectly() {
            // given
            List<String> statuses = List.of("PENDING", "FAILED");
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");
            int page = 0;
            int size = 20;

            // when
            GetOutboxListQuery result =
                    mapper.toQuery(statuses, createdFrom, createdTo, page, size);

            // then
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
            // given
            Instant createdFrom = Instant.parse("2024-01-01T00:00:00Z");
            Instant createdTo = Instant.parse("2024-12-31T23:59:59Z");

            // when
            GetOutboxListQuery result = mapper.toQuery(null, createdFrom, createdTo, 0, 20);

            // then
            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("빈 statuses 리스트를 쿼리로 변환한다 (기본값 PENDING, FAILED)")
        void toQuery_WithEmptyStatuses_ShouldUseDefaults() {
            // given
            List<String> statuses = List.of();

            // when
            GetOutboxListQuery result = mapper.toQuery(statuses, null, null, 0, 20);

            // then
            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("날짜 필터가 null인 요청을 쿼리로 변환한다")
        void toQuery_WithNullDateRange_ShouldConvertWithNullDates() {
            // given
            List<String> statuses = List.of("SENT");

            // when
            GetOutboxListQuery result = mapper.toQuery(statuses, null, null, 0, 20);

            // then
            assertThat(result.createdFrom()).isNull();
            assertThat(result.createdTo()).isNull();
        }

        @Test
        @DisplayName("단일 상태 필터를 쿼리로 변환한다")
        void toQuery_WithSingleStatus_ShouldConvertCorrectly() {
            // given
            List<String> statuses = List.of("SENT");

            // when
            GetOutboxListQuery result = mapper.toQuery(statuses, null, null, 0, 20);

            // then
            assertThat(result.statuses()).containsExactly(OutboxStatus.SENT);
        }

        @Test
        @DisplayName("소문자 상태 값도 변환한다 (case-insensitive)")
        void toQuery_WithLowercaseStatus_ShouldConvertCorrectly() {
            // given
            List<String> statuses = List.of("pending", "failed");

            // when
            GetOutboxListQuery result = mapper.toQuery(statuses, null, null, 0, 20);

            // then
            assertThat(result.statuses())
                    .containsExactly(OutboxStatus.PENDING, OutboxStatus.FAILED);
        }

        @Test
        @DisplayName("유효하지 않은 상태 값은 IllegalArgumentException을 발생시킨다")
        void toQuery_WithInvalidStatus_ShouldThrowException() {
            // given
            List<String> statuses = List.of("INVALID_STATUS");

            // when & then
            assertThatThrownBy(() -> mapper.toQuery(statuses, null, null, 0, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid outbox status: 'INVALID_STATUS'")
                    .hasMessageContaining("Valid values: PENDING, FAILED, SENT");
        }

        @Test
        @DisplayName("빈 문자열 상태 값은 IllegalArgumentException을 발생시킨다")
        void toQuery_WithBlankStatus_ShouldThrowException() {
            // given
            List<String> statuses = List.of("  ");

            // when & then
            assertThatThrownBy(() -> mapper.toQuery(statuses, null, null, 0, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Status cannot be null or blank");
        }

        @Test
        @DisplayName("여러 상태 중 하나가 유효하지 않으면 IllegalArgumentException을 발생시킨다")
        void toQuery_WithOneInvalidStatus_ShouldThrowException() {
            // given
            List<String> statuses = List.of("PENDING", "UNKNOWN");

            // when & then
            assertThatThrownBy(() -> mapper.toQuery(statuses, null, null, 0, 20))
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
            // given
            Instant now = Instant.now();
            OutboxResponse response =
                    new OutboxResponse(
                            1L, "idempotency-key-123", OutboxStatus.PENDING, 0, now, null);

            // when
            OutboxApiResponse result = mapper.toApiResponse(response);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.idempotencyKey()).isEqualTo("idempotency-key-123");
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.retryCount()).isZero();
            assertThat(result.createdAt()).isEqualTo(now.toString());
            assertThat(result.processedAt()).isNull();
        }

        @Test
        @DisplayName("processedAt이 있는 OutboxResponse를 변환한다")
        void toApiResponse_WithProcessedAt_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            Instant processedAt = now.plusSeconds(60);
            OutboxResponse response =
                    new OutboxResponse(2L, "key-456", OutboxStatus.SENT, 1, now, processedAt);

            // when
            OutboxApiResponse result = mapper.toApiResponse(response);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(2L);
            assertThat(result.status()).isEqualTo("SENT");
            assertThat(result.retryCount()).isEqualTo(1);
            assertThat(result.processedAt()).isEqualTo(processedAt.toString());
        }
    }

    @Nested
    @DisplayName("toPageApiResponse() 테스트")
    class ToPageApiResponseTests {

        @Test
        @DisplayName("PageResponse를 PageApiResponse로 변환한다")
        void toPageApiResponse_ShouldConvertCorrectly() {
            // given
            Instant now = Instant.now();
            OutboxResponse response1 =
                    new OutboxResponse(1L, "key-1", OutboxStatus.PENDING, 0, now, null);
            OutboxResponse response2 =
                    new OutboxResponse(2L, "key-2", OutboxStatus.FAILED, 2, now, null);

            PageResponse<OutboxResponse> pageResponse =
                    new PageResponse<>(List.of(response1, response2), 0, 20, 2, 1, true, true);

            // when
            PageApiResponse<OutboxApiResponse> result = mapper.toPageApiResponse(pageResponse);

            // then
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

    @Nested
    @DisplayName("toRepublishApiResponse() 테스트")
    class ToRepublishApiResponseTests {

        @Test
        @DisplayName("성공 RepublishResultResponse를 변환한다")
        void toRepublishApiResponse_Success_ShouldConvertCorrectly() {
            // given
            RepublishResultResponse response = new RepublishResultResponse(1L, true, "SQS 재발행 완료");

            // when
            RepublishResultApiResponse result = mapper.toRepublishApiResponse(response);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(1L);
            assertThat(result.success()).isTrue();
            assertThat(result.message()).isEqualTo("SQS 재발행 완료");
        }

        @Test
        @DisplayName("실패 RepublishResultResponse를 변환한다")
        void toRepublishApiResponse_Failure_ShouldConvertCorrectly() {
            // given
            RepublishResultResponse response =
                    new RepublishResultResponse(999L, false, "Outbox를 찾을 수 없습니다.");

            // when
            RepublishResultApiResponse result = mapper.toRepublishApiResponse(response);

            // then
            assertThat(result.crawlTaskId()).isEqualTo(999L);
            assertThat(result.success()).isFalse();
            assertThat(result.message()).isEqualTo("Outbox를 찾을 수 없습니다.");
        }
    }
}
