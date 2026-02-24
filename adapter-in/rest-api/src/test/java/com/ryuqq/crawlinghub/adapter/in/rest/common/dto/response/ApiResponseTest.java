package com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * ApiResponse 단위 테스트
 *
 * <p>표준 API 응답 래퍼의 팩토리 메서드 및 필드 구성을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@DisplayName("ApiResponse 단위 테스트")
class ApiResponseTest {

    @AfterEach
    void tearDown() {
        // 각 테스트 후 MDC 초기화
        MDC.clear();
    }

    @Nested
    @DisplayName("of(T data) 메서드는")
    class OfWithDataTest {

        @Test
        @DisplayName("데이터를 포함한 ApiResponse를 생성한다")
        void shouldCreateApiResponseWithData() {
            // Given
            String data = "테스트 데이터";

            // When
            ApiResponse<String> response = ApiResponse.of(data);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.data()).isEqualTo("테스트 데이터");
        }

        @Test
        @DisplayName("timestamp 필드가 null이 아니다")
        void shouldHaveNonNullTimestamp() {
            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.timestamp()).isNotBlank();
        }

        @Test
        @DisplayName("timestamp가 ISO 8601 포맷을 따른다")
        void shouldHaveIso8601Timestamp() {
            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            // ISO 8601 패턴: 2024-01-15T10:30:00+09:00 또는 나노초 포함 2024-01-15T10:30:00.123456+09:00
            assertThat(response.timestamp())
                    .matches(
                            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?[+-]\\d{2}:\\d{2}");
        }

        @Test
        @DisplayName("requestId 필드가 null이 아니다")
        void shouldHaveNonNullRequestId() {
            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            assertThat(response.requestId()).isNotNull();
            assertThat(response.requestId()).isNotBlank();
        }

        @Test
        @DisplayName("MDC에 traceId가 없으면 UUID를 requestId로 사용한다")
        void shouldUseUuidAsRequestIdWhenNoTraceIdInMdc() {
            // Given
            MDC.remove("traceId");

            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            // UUID 포맷: 8-4-4-4-12 자리 16진수
            assertThat(response.requestId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("MDC에 traceId가 있으면 traceId를 requestId로 사용한다")
        void shouldUseTraceIdFromMdcAsRequestId() {
            // Given
            String traceId = "abc123def456789";
            MDC.put("traceId", traceId);

            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            assertThat(response.requestId()).isEqualTo(traceId);
        }

        @Test
        @DisplayName("MDC traceId가 공백이면 UUID를 requestId로 사용한다")
        void shouldUseUuidWhenTraceIdIsBlank() {
            // Given
            MDC.put("traceId", "   ");

            // When
            ApiResponse<String> response = ApiResponse.of("data");

            // Then
            // UUID 형식이어야 함
            assertThat(response.requestId())
                    .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        }

        @Test
        @DisplayName("data가 null이어도 정상적으로 생성된다")
        void shouldCreateApiResponseWithNullData() {
            // When
            ApiResponse<String> response = ApiResponse.of((String) null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.data()).isNull();
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).isNotNull();
        }

        @Test
        @DisplayName("Integer 타입 데이터를 포함한 ApiResponse를 생성한다")
        void shouldCreateApiResponseWithIntegerData() {
            // Given
            Integer data = 42;

            // When
            ApiResponse<Integer> response = ApiResponse.of(data);

            // Then
            assertThat(response.data()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("of() 메서드 (데이터 없음)는")
    class OfWithoutDataTest {

        @Test
        @DisplayName("data가 null인 ApiResponse를 생성한다")
        void shouldCreateApiResponseWithNullData() {
            // When
            ApiResponse<Object> response = ApiResponse.of();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.data()).isNull();
        }

        @Test
        @DisplayName("timestamp와 requestId는 null이 아니다")
        void shouldHaveNonNullTimestampAndRequestId() {
            // When
            ApiResponse<Object> response = ApiResponse.of();

            // Then
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("record 필드 접근은")
    class RecordFieldAccessTest {

        @Test
        @DisplayName("data(), timestamp(), requestId() 접근자가 정상 동작한다")
        void shouldAccessAllRecordFields() {
            // Given
            String data = "test";

            // When
            ApiResponse<String> response = ApiResponse.of(data);

            // Then
            assertThat(response.data()).isNotNull();
            assertThat(response.timestamp()).isNotNull();
            assertThat(response.requestId()).isNotNull();
        }

        @Test
        @DisplayName("동일한 데이터로 생성된 두 ApiResponse는 timestamp가 다를 수 있다")
        void shouldHaveDifferentTimestampsForDifferentInstances() throws InterruptedException {
            // Given
            String data = "test";

            // When
            ApiResponse<String> response1 = ApiResponse.of(data);
            Thread.sleep(10); // 시간 차이 확보
            ApiResponse<String> response2 = ApiResponse.of(data);

            // Then
            // data는 동일
            assertThat(response1.data()).isEqualTo(response2.data());
            // timestamp는 생성 시점이 달라 다를 수 있음 (동일 밀리초이면 같을 수도 있으므로 isNotNull만 확인)
            assertThat(response1.timestamp()).isNotNull();
            assertThat(response2.timestamp()).isNotNull();
        }
    }
}
