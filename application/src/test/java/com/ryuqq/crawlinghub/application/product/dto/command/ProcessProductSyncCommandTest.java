package com.ryuqq.crawlinghub.application.product.dto.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * ProcessProductSyncCommand 단위 테스트
 *
 * <p>Compact Constructor 검증
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("ProcessProductSyncCommand 테스트")
class ProcessProductSyncCommandTest {

    @Nested
    @DisplayName("생성자 검증 테스트")
    class Constructor {

        @Test
        @DisplayName("[성공] 유효한 값으로 생성")
        void shouldCreateWithValidValues() {
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(1L, 100L, 10L, 999L, "CREATE", null, "key-abc");

            assertThat(command.outboxId()).isEqualTo(1L);
            assertThat(command.crawledProductId()).isEqualTo(100L);
            assertThat(command.sellerId()).isEqualTo(10L);
            assertThat(command.itemNo()).isEqualTo(999L);
            assertThat(command.syncType()).isEqualTo("CREATE");
            assertThat(command.externalProductId()).isNull();
            assertThat(command.idempotencyKey()).isEqualTo("key-abc");
        }

        @Test
        @DisplayName("[성공] externalProductId가 있는 UPDATE 타입 생성")
        void shouldCreateWithExternalProductId() {
            ProcessProductSyncCommand command =
                    new ProcessProductSyncCommand(
                            2L, 200L, 20L, 888L, "UPDATE_PRICE", 555L, "key-xyz");

            assertThat(command.syncType()).isEqualTo("UPDATE_PRICE");
            assertThat(command.externalProductId()).isEqualTo(555L);
        }

        @Test
        @DisplayName("[실패] outboxId가 null이면 예외")
        void shouldThrowWhenOutboxIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            null, 100L, 10L, 999L, "CREATE", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("outboxId");
        }

        @Test
        @DisplayName("[실패] crawledProductId가 null이면 예외")
        void shouldThrowWhenCrawledProductIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            1L, null, 10L, 999L, "CREATE", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("crawledProductId");
        }

        @Test
        @DisplayName("[실패] sellerId가 null이면 예외")
        void shouldThrowWhenSellerIdIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            1L, 100L, null, 999L, "CREATE", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sellerId");
        }

        @Test
        @DisplayName("[실패] itemNo가 null이면 예외")
        void shouldThrowWhenItemNoIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            1L, 100L, 10L, null, "CREATE", null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("itemNo");
        }

        @Test
        @DisplayName("[실패] syncType이 null이면 예외")
        void shouldThrowWhenSyncTypeIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            1L, 100L, 10L, 999L, null, null, "key"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("syncType");
        }

        @Test
        @DisplayName("[실패] idempotencyKey가 null이면 예외")
        void shouldThrowWhenIdempotencyKeyIsNull() {
            assertThatThrownBy(
                            () ->
                                    new ProcessProductSyncCommand(
                                            1L, 100L, 10L, 999L, "CREATE", null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("idempotencyKey");
        }
    }
}
