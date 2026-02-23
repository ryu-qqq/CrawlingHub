package com.ryuqq.crawlinghub.application.common.dto.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * BatchProcessingResult 단위 테스트
 *
 * <p>집계 결과 팩토리 메서드 및 불변성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("BatchProcessingResult 테스트")
class BatchProcessingResultTest {

    @Nested
    @DisplayName("from() 팩토리 메서드 테스트")
    class From {

        @Test
        @DisplayName("[성공] 성공 항목만 있는 경우 집계 정확성 검증")
        void shouldCalculateCorrectCountsWhenAllSuccess() {
            // Given
            List<BatchItemResult<Long>> items =
                    List.of(
                            BatchItemResult.success(1L),
                            BatchItemResult.success(2L),
                            BatchItemResult.success(3L));

            // When
            BatchProcessingResult<Long> result = BatchProcessingResult.from(items);

            // Then
            assertThat(result.totalCount()).isEqualTo(3);
            assertThat(result.successCount()).isEqualTo(3);
            assertThat(result.failureCount()).isZero();
        }

        @Test
        @DisplayName("[성공] 실패 항목만 있는 경우 집계 정확성 검증")
        void shouldCalculateCorrectCountsWhenAllFailure() {
            // Given
            List<BatchItemResult<Long>> items =
                    List.of(
                            BatchItemResult.failure(1L, "ERR", "실패1"),
                            BatchItemResult.failure(2L, "ERR", "실패2"));

            // When
            BatchProcessingResult<Long> result = BatchProcessingResult.from(items);

            // Then
            assertThat(result.totalCount()).isEqualTo(2);
            assertThat(result.successCount()).isZero();
            assertThat(result.failureCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("[성공] 성공/실패 혼합인 경우 집계 정확성 검증")
        void shouldCalculateCorrectCountsWhenMixed() {
            // Given
            List<BatchItemResult<Long>> items =
                    List.of(
                            BatchItemResult.success(1L),
                            BatchItemResult.failure(2L, "ERR", "실패"),
                            BatchItemResult.success(3L),
                            BatchItemResult.failure(4L, "ERR", "실패2"));

            // When
            BatchProcessingResult<Long> result = BatchProcessingResult.from(items);

            // Then
            assertThat(result.totalCount()).isEqualTo(4);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failureCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("[성공] 빈 목록으로 BatchProcessingResult 생성")
        void shouldHandleEmptyList() {
            // When
            BatchProcessingResult<Long> result = BatchProcessingResult.from(List.of());

            // Then
            assertThat(result.totalCount()).isZero();
            assertThat(result.successCount()).isZero();
            assertThat(result.failureCount()).isZero();
            assertThat(result.results()).isEmpty();
        }
    }

    @Nested
    @DisplayName("직접 생성자 테스트")
    class DirectConstructor {

        @Test
        @DisplayName("[성공] null results는 빈 리스트로 변환")
        void shouldConvertNullResultsToEmptyList() {
            // When
            BatchProcessingResult<Long> result = new BatchProcessingResult<>(0, 0, 0, null);

            // Then
            assertThat(result.results()).isEmpty();
        }

        @Test
        @DisplayName("[성공] results는 불변 리스트로 저장")
        void shouldStoreResultsAsImmutableList() {
            // Given
            List<BatchItemResult<Long>> items = List.of(BatchItemResult.success(1L));

            // When
            BatchProcessingResult<Long> result = new BatchProcessingResult<>(1, 1, 0, items);

            // Then
            assertThat(result.results()).hasSize(1);
        }
    }
}
