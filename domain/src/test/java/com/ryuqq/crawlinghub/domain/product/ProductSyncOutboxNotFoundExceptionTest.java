package com.ryuqq.crawlinghub.domain.product;

import com.ryuqq.crawlinghub.domain.product.exception.ProductSyncOutboxNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductSyncOutboxNotFoundException 테스트")
class ProductSyncOutboxNotFoundExceptionTest {

    private static final Long OUTBOX_ID = 12345L;

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("outboxId만 전달하는 생성자 - 기본 메시지 생성")
        void shouldCreateExceptionWithDefaultMessage() {
            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID);

            // Then
            assertThat(exception.getOutboxId()).isEqualTo(OUTBOX_ID);
            assertThat(exception.getMessage())
                .contains("ProductSyncOutbox를 찾을 수 없습니다")
                .contains("outboxId=" + OUTBOX_ID);
        }

        @Test
        @DisplayName("outboxId와 커스텀 메시지를 전달하는 생성자")
        void shouldCreateExceptionWithCustomMessage() {
            // Given
            String customMessage = "이미 삭제된 Outbox입니다";

            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID, customMessage);

            // Then
            assertThat(exception.getOutboxId()).isEqualTo(OUTBOX_ID);
            assertThat(exception.getMessage()).isEqualTo(customMessage);
        }
    }

    @Nested
    @DisplayName("getOutboxId() 메서드 테스트")
    class GetOutboxIdTests {

        @Test
        @DisplayName("생성 시 전달한 outboxId 반환")
        void shouldReturnOutboxId() {
            // Given
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID);

            // When
            Long result = exception.getOutboxId();

            // Then
            assertThat(result).isEqualTo(OUTBOX_ID);
        }

        @Test
        @DisplayName("다른 outboxId 값도 정확히 반환")
        void shouldReturnDifferentOutboxId() {
            // Given
            Long differentId = 99999L;
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(differentId);

            // When
            Long result = exception.getOutboxId();

            // Then
            assertThat(result).isEqualTo(differentId);
        }
    }

    @Nested
    @DisplayName("RuntimeException 상속 테스트")
    class InheritanceTests {

        @Test
        @DisplayName("RuntimeException을 상속한다")
        void shouldExtendRuntimeException() {
            // Given
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Unchecked Exception이므로 명시적 throws 불필요")
        void shouldBeUncheckedException() {
            // When & Then (compile-time check)
            // Unchecked exception이므로 try-catch 없이 throw 가능
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID);

            assertThat(exception).isNotNull();
        }
    }

    @Nested
    @DisplayName("메시지 포맷 테스트")
    class MessageFormatTests {

        @Test
        @DisplayName("기본 메시지는 outboxId를 포함한다")
        void shouldIncludeOutboxIdInDefaultMessage() {
            // Given
            Long outboxId = 54321L;

            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(outboxId);

            // Then
            assertThat(exception.getMessage()).contains(outboxId.toString());
        }

        @Test
        @DisplayName("커스텀 메시지는 원본 그대로 유지")
        void shouldPreserveCustomMessage() {
            // Given
            String exactMessage = "정확한 메시지";

            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(OUTBOX_ID, exactMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo(exactMessage);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("존재하지 않는 Outbox 조회 시나리오")
        void shouldHandleNonExistentOutbox() {
            // Given
            Long nonExistentId = 0L;

            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(nonExistentId);

            // Then
            assertThat(exception.getOutboxId()).isEqualTo(nonExistentId);
            assertThat(exception.getMessage())
                .contains("ProductSyncOutbox를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("이미 삭제된 Outbox 조회 시나리오")
        void shouldHandleDeletedOutbox() {
            // Given
            Long deletedId = 777L;
            String message = "이미 삭제된 Outbox입니다: outboxId=" + deletedId;

            // When
            ProductSyncOutboxNotFoundException exception =
                new ProductSyncOutboxNotFoundException(deletedId, message);

            // Then
            assertThat(exception.getOutboxId()).isEqualTo(deletedId);
            assertThat(exception.getMessage()).contains("이미 삭제된");
        }
    }
}
