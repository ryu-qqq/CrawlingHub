package com.ryuqq.crawlinghub.application.common.dto.result;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * BatchItemResult 단위 테스트
 *
 * <p>성공/실패 팩토리 메서드 및 불변성 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("BatchItemResult 테스트")
class BatchItemResultTest {

    @Nested
    @DisplayName("success() 팩토리 메서드 테스트")
    class Success {

        @Test
        @DisplayName("[성공] 성공 결과 생성 시 success=true, errorCode=null, errorMessage=null")
        void shouldCreateSuccessResultWithNullErrors() {
            // When
            BatchItemResult<Long> result = BatchItemResult.success(1L);

            // Then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.success()).isTrue();
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }

        @Test
        @DisplayName("[성공] String ID 타입으로 성공 결과 생성")
        void shouldCreateSuccessResultWithStringId() {
            // When
            BatchItemResult<String> result = BatchItemResult.success("task-001");

            // Then
            assertThat(result.id()).isEqualTo("task-001");
            assertThat(result.success()).isTrue();
        }
    }

    @Nested
    @DisplayName("failure() 팩토리 메서드 테스트")
    class Failure {

        @Test
        @DisplayName("[성공] 실패 결과 생성 시 success=false, errorCode와 errorMessage 포함")
        void shouldCreateFailureResultWithErrorInfo() {
            // When
            BatchItemResult<Long> result = BatchItemResult.failure(2L, "DB_ERROR", "데이터베이스 연결 실패");

            // Then
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("DB_ERROR");
            assertThat(result.errorMessage()).isEqualTo("데이터베이스 연결 실패");
        }

        @Test
        @DisplayName("[성공] 에러 코드와 메시지 없이 실패 결과 생성 가능")
        void shouldCreateFailureResultWithNullErrorInfo() {
            // When
            BatchItemResult<Long> result = BatchItemResult.failure(3L, null, null);

            // Then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
        }
    }

    @Nested
    @DisplayName("직접 생성자 테스트")
    class DirectConstructor {

        @Test
        @DisplayName("[성공] record 생성자로 직접 생성")
        void shouldCreateDirectly() {
            // When
            BatchItemResult<Integer> result = new BatchItemResult<>(42, true, null, null);

            // Then
            assertThat(result.id()).isEqualTo(42);
            assertThat(result.success()).isTrue();
        }
    }
}
