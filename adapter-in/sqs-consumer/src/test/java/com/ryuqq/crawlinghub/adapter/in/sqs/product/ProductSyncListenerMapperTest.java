package com.ryuqq.crawlinghub.adapter.in.sqs.product;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.messaging.ProductSyncPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * ProductSyncListenerMapper 단위 테스트
 *
 * <p>ProductSyncPayload → ProcessProductSyncCommand 변환을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@DisplayName("ProductSyncListenerMapper 단위 테스트")
class ProductSyncListenerMapperTest {

    private ProductSyncListenerMapper sut;

    @BeforeEach
    void setUp() {
        sut = new ProductSyncListenerMapper();
    }

    @Nested
    @DisplayName("toCommand 메서드 테스트")
    class ToCommandTest {

        @Test
        @DisplayName("[성공] ProductSyncPayload의 모든 필드가 ProcessProductSyncCommand에 올바르게 매핑된다")
        void shouldMapAllFieldsCorrectly() {
            // Given
            ProductSyncPayload payload =
                    new ProductSyncPayload(
                            100L, // outboxId
                            200L, // crawledProductId
                            300L, // sellerId
                            400L, // itemNo
                            "CREATE", // syncType
                            null, // externalProductId (CREATE 시 null)
                            "idem-key-001" // idempotencyKey
                            );

            // When
            ProcessProductSyncCommand command = sut.toCommand(payload);

            // Then
            assertThat(command.outboxId()).isEqualTo(100L);
            assertThat(command.crawledProductId()).isEqualTo(200L);
            assertThat(command.sellerId()).isEqualTo(300L);
            assertThat(command.itemNo()).isEqualTo(400L);
            assertThat(command.syncType()).isEqualTo("CREATE");
            assertThat(command.externalProductId()).isNull();
            assertThat(command.idempotencyKey()).isEqualTo("idem-key-001");
        }

        @Test
        @DisplayName("[성공] UPDATE 타입 페이로드에서 externalProductId가 올바르게 매핑된다")
        void shouldMapExternalProductIdForUpdateType() {
            // Given
            ProductSyncPayload payload =
                    new ProductSyncPayload(
                            101L, // outboxId
                            201L, // crawledProductId
                            301L, // sellerId
                            401L, // itemNo
                            "UPDATE", // syncType
                            999L, // externalProductId (UPDATE 시 있음)
                            "idem-key-002" // idempotencyKey
                            );

            // When
            ProcessProductSyncCommand command = sut.toCommand(payload);

            // Then
            assertThat(command.outboxId()).isEqualTo(101L);
            assertThat(command.syncType()).isEqualTo("UPDATE");
            assertThat(command.externalProductId()).isEqualTo(999L);
            assertThat(command.idempotencyKey()).isEqualTo("idem-key-002");
        }

        @Test
        @DisplayName("[성공] 동일 페이로드로 생성된 커맨드는 동일한 필드 값을 가진다")
        void shouldCreateCommandWithSameValuesAsPayload() {
            // Given
            ProductSyncPayload payload =
                    new ProductSyncPayload(1L, 2L, 3L, 4L, "CREATE", null, "key-001");

            // When
            ProcessProductSyncCommand command1 = sut.toCommand(payload);
            ProcessProductSyncCommand command2 = sut.toCommand(payload);

            // Then: 같은 payload로 생성한 커맨드는 동일한 값을 가짐
            assertThat(command1.outboxId()).isEqualTo(command2.outboxId());
            assertThat(command1.crawledProductId()).isEqualTo(command2.crawledProductId());
            assertThat(command1.syncType()).isEqualTo(command2.syncType());
        }
    }
}
