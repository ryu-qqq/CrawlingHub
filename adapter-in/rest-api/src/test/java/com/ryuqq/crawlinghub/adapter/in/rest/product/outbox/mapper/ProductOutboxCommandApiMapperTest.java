package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ryuqq.crawlinghub.adapter.in.rest.product.outbox.dto.response.OutboxRetryApiResponse;
import com.ryuqq.crawlinghub.application.product.dto.command.RetryImageOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductOutboxCommandApiMapper 단위 테스트
 *
 * <p>ProductOutbox Command REST API ↔ Application Layer 변환 로직을 검증합니다.
 *
 * <p><strong>테스트 범위:</strong>
 *
 * <ul>
 *   <li>outboxId → RetrySyncOutboxCommand 변환
 *   <li>outboxId → RetryImageOutboxCommand 변환
 *   <li>OutboxRetryResponse → OutboxRetryApiResponse 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("rest-api")
@Tag("mapper")
@DisplayName("ProductOutboxCommandApiMapper 단위 테스트")
class ProductOutboxCommandApiMapperTest {

    private ProductOutboxCommandApiMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductOutboxCommandApiMapper();
    }

    @Nested
    @DisplayName("toRetrySyncOutboxCommand()는")
    class ToRetrySyncOutboxCommand {

        @Test
        @DisplayName("outboxId를 RetrySyncOutboxCommand로 변환한다")
        void shouldConvertOutboxIdToRetrySyncOutboxCommand() {
            // Given
            Long outboxId = 42L;

            // When
            RetrySyncOutboxCommand command = mapper.toRetrySyncOutboxCommand(outboxId);

            // Then
            assertThat(command.outboxId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("다른 outboxId 값도 정상 변환한다")
        void shouldConvertDifferentOutboxIds() {
            // Given
            Long outboxId = 1L;

            // When
            RetrySyncOutboxCommand command = mapper.toRetrySyncOutboxCommand(outboxId);

            // Then
            assertThat(command.outboxId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null outboxId는 RetrySyncOutboxCommand 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenOutboxIdIsNull() {
            // Given
            Long outboxId = null;

            // When & Then
            assertThatThrownBy(() -> mapper.toRetrySyncOutboxCommand(outboxId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Outbox ID는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("toRetryImageOutboxCommand()는")
    class ToRetryImageOutboxCommand {

        @Test
        @DisplayName("outboxId를 RetryImageOutboxCommand로 변환한다")
        void shouldConvertOutboxIdToRetryImageOutboxCommand() {
            // Given
            Long outboxId = 99L;

            // When
            RetryImageOutboxCommand command = mapper.toRetryImageOutboxCommand(outboxId);

            // Then
            assertThat(command.outboxId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("다른 outboxId 값도 정상 변환한다")
        void shouldConvertDifferentOutboxIds() {
            // Given
            Long outboxId = 500L;

            // When
            RetryImageOutboxCommand command = mapper.toRetryImageOutboxCommand(outboxId);

            // Then
            assertThat(command.outboxId()).isEqualTo(500L);
        }

        @Test
        @DisplayName("null outboxId는 RetryImageOutboxCommand 생성 시 예외가 발생한다")
        void shouldThrowExceptionWhenOutboxIdIsNull() {
            // Given
            Long outboxId = null;

            // When & Then
            assertThatThrownBy(() -> mapper.toRetryImageOutboxCommand(outboxId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Outbox ID는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("toApiResponse()는")
    class ToApiResponse {

        @Test
        @DisplayName("OutboxRetryResponse를 OutboxRetryApiResponse로 변환한다")
        void shouldConvertOutboxRetryResponseToApiResponse() {
            // Given
            OutboxRetryResponse response =
                    new OutboxRetryResponse(10L, "FAILED", "PENDING", "재시도 요청이 등록되었습니다.");

            // When
            OutboxRetryApiResponse apiResponse = mapper.toApiResponse(response);

            // Then
            assertThat(apiResponse.outboxId()).isEqualTo(10L);
            assertThat(apiResponse.previousStatus()).isEqualTo("FAILED");
            assertThat(apiResponse.newStatus()).isEqualTo("PENDING");
            assertThat(apiResponse.message()).isEqualTo("재시도 요청이 등록되었습니다.");
        }

        @Test
        @DisplayName("정적 팩토리로 생성된 OutboxRetryResponse도 정상 변환한다")
        void shouldConvertSuccessFactoryResponse() {
            // Given
            OutboxRetryResponse response = OutboxRetryResponse.success(20L, "FAILED", "PENDING");

            // When
            OutboxRetryApiResponse apiResponse = mapper.toApiResponse(response);

            // Then
            assertThat(apiResponse.outboxId()).isEqualTo(20L);
            assertThat(apiResponse.previousStatus()).isEqualTo("FAILED");
            assertThat(apiResponse.newStatus()).isEqualTo("PENDING");
            assertThat(apiResponse.message()).isEqualTo("재시도 요청이 등록되었습니다.");
        }

        @Test
        @DisplayName("모든 필드가 null인 OutboxRetryResponse도 변환한다")
        void shouldHandleNullFields() {
            // Given
            OutboxRetryResponse response = new OutboxRetryResponse(null, null, null, null);

            // When
            OutboxRetryApiResponse apiResponse = mapper.toApiResponse(response);

            // Then
            assertThat(apiResponse.outboxId()).isNull();
            assertThat(apiResponse.previousStatus()).isNull();
            assertThat(apiResponse.newStatus()).isNull();
            assertThat(apiResponse.message()).isNull();
        }
    }
}
