package com.ryuqq.crawlinghub.application.execution.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.cralwinghub.domain.fixture.execution.CrawlExecutionFixture;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.execution.dto.query.ListCrawlExecutionsQuery;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionDetailResponse;
import com.ryuqq.crawlinghub.application.execution.dto.response.CrawlExecutionResponse;
import com.ryuqq.crawlinghub.domain.execution.aggregate.CrawlExecution;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionCriteria;
import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * CrawlExecutionAssembler 단위 테스트
 *
 * <p>순수 변환 로직 테스트: 외부 의존성 없음
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("CrawlExecutionAssembler 테스트")
class CrawlExecutionAssemblerTest {

    private final CrawlExecutionAssembler assembler = new CrawlExecutionAssembler();

    @Nested
    @DisplayName("toCriteria() 테스트")
    class ToCriteria {

        @Test
        @DisplayName("[성공] ListCrawlExecutionsQuery → CrawlExecutionCriteria 변환 (전체 필드)")
        void shouldConvertQueryToCriteria() {
            // Given
            LocalDateTime from = LocalDateTime.of(2025, 11, 1, 0, 0);
            LocalDateTime to = LocalDateTime.of(2025, 11, 30, 23, 59);
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            1L, 10L, 100L, CrawlExecutionStatus.SUCCESS, from, to, 0, 20);

            // When
            CrawlExecutionCriteria result = assembler.toCriteria(query);

            // Then
            Instant expectedFrom = from.atZone(ZoneId.systemDefault()).toInstant();
            Instant expectedTo = to.atZone(ZoneId.systemDefault()).toInstant();

            assertThat(result.crawlTaskId().value()).isEqualTo(1L);
            assertThat(result.crawlSchedulerId().value()).isEqualTo(10L);
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(result.from()).isEqualTo(expectedFrom);
            assertThat(result.to()).isEqualTo(expectedTo);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(20);
        }

        @Test
        @DisplayName("[성공] ListCrawlExecutionsQuery → CrawlExecutionCriteria 변환 (null 필드)")
        void shouldConvertQueryToCriteriaWithNullFields() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(null, null, null, null, null, null, 0, 10);

            // When
            CrawlExecutionCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.crawlTaskId()).isNull();
            assertThat(result.crawlSchedulerId()).isNull();
            assertThat(result.status()).isNull();
            assertThat(result.from()).isNull();
            assertThat(result.to()).isNull();
        }

        @Test
        @DisplayName("[성공] toCriteria() - sellerId 포함 Criteria 생성")
        void shouldConvertQueryToCriteriaWithSellerId() {
            // Given
            ListCrawlExecutionsQuery query =
                    new ListCrawlExecutionsQuery(
                            1L, 10L, 100L, CrawlExecutionStatus.RUNNING, null, null, 0, 10);

            // When
            CrawlExecutionCriteria result = assembler.toCriteria(query);

            // Then
            assertThat(result.crawlTaskId().value()).isEqualTo(1L);
            assertThat(result.sellerId().value()).isEqualTo(100L);
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.RUNNING);
        }
    }

    @Nested
    @DisplayName("toResponse() 테스트")
    class ToResponse {

        @Test
        @DisplayName("[성공] CrawlExecution (RUNNING) → CrawlExecutionResponse 변환")
        void shouldConvertRunningExecutionToResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRunningExecution();

            // When
            CrawlExecutionResponse result = assembler.toResponse(execution);

            // Then
            assertThat(result.crawlExecutionId()).isEqualTo(execution.getId().value());
            assertThat(result.crawlTaskId()).isEqualTo(execution.getCrawlTaskId().value());
            assertThat(result.crawlSchedulerId())
                    .isEqualTo(execution.getCrawlSchedulerId().value());
            assertThat(result.sellerId()).isEqualTo(execution.getSellerId().value());
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.RUNNING);
        }

        @Test
        @DisplayName("[성공] CrawlExecution (SUCCESS) → CrawlExecutionResponse 변환")
        void shouldConvertSuccessExecutionToResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionResponse result = assembler.toResponse(execution);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.durationMs()).isNotNull();
            assertThat(result.startedAt()).isNotNull();
            assertThat(result.completedAt()).isNotNull();
        }

        @Test
        @DisplayName("[성공] CrawlExecution (FAILED) → CrawlExecutionResponse 변환")
        void shouldConvertFailedExecutionToResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aFailedExecution();

            // When
            CrawlExecutionResponse result = assembler.toResponse(execution);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(result.httpStatusCode()).isEqualTo(500);
        }

        @Test
        @DisplayName("[성공] CrawlExecution (Rate Limited) → CrawlExecutionResponse 변환")
        void shouldConvertRateLimitedExecutionToResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aRateLimitedExecution();

            // When
            CrawlExecutionResponse result = assembler.toResponse(execution);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(result.httpStatusCode()).isEqualTo(429);
        }
    }

    @Nested
    @DisplayName("toDetailResponse() 테스트")
    class ToDetailResponse {

        @Test
        @DisplayName("[성공] CrawlExecution (SUCCESS) → CrawlExecutionDetailResponse 변환")
        void shouldConvertSuccessExecutionToDetailResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aSuccessExecution();

            // When
            CrawlExecutionDetailResponse result = assembler.toDetailResponse(execution);

            // Then
            assertThat(result.crawlExecutionId()).isEqualTo(execution.getId().value());
            assertThat(result.crawlTaskId()).isEqualTo(execution.getCrawlTaskId().value());
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.SUCCESS);
            assertThat(result.httpStatusCode()).isEqualTo(200);
            assertThat(result.responseBody()).isNotNull();
            assertThat(result.errorMessage()).isNull();
            assertThat(result.durationMs()).isNotNull();
        }

        @Test
        @DisplayName("[성공] CrawlExecution (FAILED) → CrawlExecutionDetailResponse 변환 (에러 메시지 포함)")
        void shouldConvertFailedExecutionToDetailResponseWithErrorMessage() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aFailedExecution();

            // When
            CrawlExecutionDetailResponse result = assembler.toDetailResponse(execution);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.FAILED);
            assertThat(result.httpStatusCode()).isEqualTo(500);
            assertThat(result.errorMessage()).isNotNull();
        }

        @Test
        @DisplayName("[성공] CrawlExecution (TIMEOUT) → CrawlExecutionDetailResponse 변환")
        void shouldConvertTimeoutExecutionToDetailResponse() {
            // Given
            CrawlExecution execution = CrawlExecutionFixture.aTimeoutExecution();

            // When
            CrawlExecutionDetailResponse result = assembler.toDetailResponse(execution);

            // Then
            assertThat(result.status()).isEqualTo(CrawlExecutionStatus.TIMEOUT);
        }
    }

    @Nested
    @DisplayName("toResponses() 테스트")
    class ToResponses {

        @Test
        @DisplayName("[성공] CrawlExecution 목록 → CrawlExecutionResponse 목록 변환")
        void shouldConvertExecutionListToResponses() {
            // Given
            List<CrawlExecution> executions =
                    List.of(
                            CrawlExecutionFixture.anExecutionWithId(1L),
                            CrawlExecutionFixture.anExecutionWithId(2L),
                            CrawlExecutionFixture.anExecutionWithId(3L));

            // When
            List<CrawlExecutionResponse> result = assembler.toResponses(executions);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).crawlExecutionId()).isEqualTo(1L);
            assertThat(result.get(1).crawlExecutionId()).isEqualTo(2L);
            assertThat(result.get(2).crawlExecutionId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("[성공] 빈 목록 → 빈 목록 반환")
        void shouldReturnEmptyListForEmptyExecutions() {
            // Given
            List<CrawlExecution> executions = List.of();

            // When
            List<CrawlExecutionResponse> result = assembler.toResponses(executions);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toPageResponse() 테스트")
    class ToPageResponse {

        @Test
        @DisplayName("[성공] CrawlExecution 목록 → PageResponse 변환")
        void shouldConvertExecutionsToPageResponse() {
            // Given
            List<CrawlExecution> executions =
                    List.of(
                            CrawlExecutionFixture.anExecutionWithId(1L),
                            CrawlExecutionFixture.anExecutionWithId(2L));
            int page = 0;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlExecutionResponse> result =
                    assembler.toPageResponse(executions, page, size, totalElements);

            // Then
            assertThat(result.content()).hasSize(2);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(25L);
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isFalse();
        }

        @Test
        @DisplayName("[성공] 마지막 페이지 → last = true")
        void shouldReturnLastPageWhenOnLastPage() {
            // Given
            List<CrawlExecution> executions = List.of(CrawlExecutionFixture.anExecutionWithId(1L));
            int page = 2;
            int size = 10;
            long totalElements = 25L;

            // When
            PageResponse<CrawlExecutionResponse> result =
                    assembler.toPageResponse(executions, page, size, totalElements);

            // Then
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
        }

        @Test
        @DisplayName("[성공] 빈 결과 → 빈 PageResponse 반환")
        void shouldReturnEmptyPageResponse() {
            // Given
            List<CrawlExecution> executions = List.of();
            int page = 0;
            int size = 10;
            long totalElements = 0L;

            // When
            PageResponse<CrawlExecutionResponse> result =
                    assembler.toPageResponse(executions, page, size, totalElements);

            // Then
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
        }
    }
}
