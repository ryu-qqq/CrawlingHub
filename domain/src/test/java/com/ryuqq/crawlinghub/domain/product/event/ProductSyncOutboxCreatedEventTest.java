package com.ryuqq.crawlinghub.domain.product.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductSyncOutboxCreatedEvent 테스트")
class ProductSyncOutboxCreatedEventTest {

    private static final Long OUTBOX_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final String PRODUCT_JSON = "{\"productId\":100,\"name\":\"테스트 상품\"}";
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 11, 7, 10, 30, 0);

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 값으로 이벤트 생성 성공")
        void shouldCreateEvent() {
            // When
            ProductSyncOutboxCreatedEvent event = new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                PRODUCT_JSON,
                CREATED_AT
            );

            // Then
            assertThat(event.getOutboxId()).isEqualTo(OUTBOX_ID);
            assertThat(event.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.getProductJson()).isEqualTo(PRODUCT_JSON);
            assertThat(event.getCreatedAt()).isEqualTo(CREATED_AT);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Outbox ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenOutboxIdIsNull(Long nullOutboxId) {
            // When & Then
            assertThatThrownBy(() -> new ProductSyncOutboxCreatedEvent(
                nullOutboxId,
                PRODUCT_ID,
                PRODUCT_JSON,
                CREATED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Outbox ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Product ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenProductIdIsNull(Long nullProductId) {
            // When & Then
            assertThatThrownBy(() -> new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                nullProductId,
                PRODUCT_JSON,
                CREATED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Product JSON이 null이면 예외 발생")
        void shouldThrowExceptionWhenProductJsonIsNull(String nullJson) {
            // When & Then
            assertThatThrownBy(() -> new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                nullJson,
                CREATED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product JSON은 필수입니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Product JSON이 빈 문자열이면 예외 발생")
        void shouldThrowExceptionWhenProductJsonIsBlank(String blankJson) {
            // When & Then
            assertThatThrownBy(() -> new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                blankJson,
                CREATED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product JSON은 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("생성 시각이 null이면 예외 발생")
        void shouldThrowExceptionWhenCreatedAtIsNull(LocalDateTime nullCreatedAt) {
            // When & Then
            assertThatThrownBy(() -> new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                PRODUCT_JSON,
                nullCreatedAt
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("생성 시각은 필수입니다");
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterTests {

        @Test
        @DisplayName("getOutboxId()는 정확한 Outbox ID 반환")
        void shouldReturnOutboxId() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            Long result = event.getOutboxId();

            // Then
            assertThat(result).isEqualTo(OUTBOX_ID);
        }

        @Test
        @DisplayName("getProductId()는 정확한 Product ID 반환")
        void shouldReturnProductId() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            Long result = event.getProductId();

            // Then
            assertThat(result).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("getProductJson()은 정확한 JSON 반환")
        void shouldReturnProductJson() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            String result = event.getProductJson();

            // Then
            assertThat(result).isEqualTo(PRODUCT_JSON);
        }

        @Test
        @DisplayName("getCreatedAt()는 정확한 생성 시각 반환")
        void shouldReturnCreatedAt() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            LocalDateTime result = event.getCreatedAt();

            // Then
            assertThat(result).isEqualTo(CREATED_AT);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 Outbox ID를 가진 이벤트는 동일하다")
        void shouldBeEqualWhenSameOutboxId() {
            // Given
            ProductSyncOutboxCreatedEvent event1 = new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                PRODUCT_JSON,
                CREATED_AT
            );
            ProductSyncOutboxCreatedEvent event2 = new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                999L,  // 다른 productId
                "{\"different\":\"json\"}",  // 다른 JSON
                LocalDateTime.now()  // 다른 시각
            );

            // Then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("다른 Outbox ID를 가진 이벤트는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentOutboxId() {
            // Given
            ProductSyncOutboxCreatedEvent event1 = new ProductSyncOutboxCreatedEvent(
                1L,
                PRODUCT_ID,
                PRODUCT_JSON,
                CREATED_AT
            );
            ProductSyncOutboxCreatedEvent event2 = new ProductSyncOutboxCreatedEvent(
                2L,
                PRODUCT_ID,
                PRODUCT_JSON,
                CREATED_AT
            );

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("자기 자신과 비교하면 동일하다")
        void shouldBeEqualToSelf() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // Then
            assertThat(event).isEqualTo(event);
        }

        @Test
        @DisplayName("null과 비교하면 동일하지 않다")
        void shouldNotBeEqualToNull() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // Then
            assertThat(event).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와 비교하면 동일하지 않다")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();
            String otherObject = "other";

            // Then
            assertThat(event).isNotEqualTo(otherObject);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드를 포함")
        void shouldIncludeKeyFieldsInToString() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            String result = event.toString();

            // Then
            assertThat(result)
                .contains("ProductSyncOutboxCreatedEvent")
                .contains("outboxId=" + OUTBOX_ID)
                .contains("productId=" + PRODUCT_ID)
                .contains("createdAt=" + CREATED_AT);
        }

        @Test
        @DisplayName("toString()은 productJson을 포함하지 않는다")
        void shouldNotIncludeProductJsonInToString() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When
            String result = event.toString();

            // Then
            assertThat(result).doesNotContain(PRODUCT_JSON);
            // productJson은 민감한 데이터일 수 있으므로 toString에 포함하지 않음
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Outbox 생성 직후 이벤트 발행 시나리오")
        void shouldHandleOutboxCreationScenario() {
            // Given
            Long newOutboxId = 123L;
            Long newProductId = 456L;
            String productJson = "{\"id\":456,\"name\":\"갤럭시 S24\",\"price\":1200000}";
            LocalDateTime now = LocalDateTime.now();

            // When
            ProductSyncOutboxCreatedEvent event = new ProductSyncOutboxCreatedEvent(
                newOutboxId,
                newProductId,
                productJson,
                now
            );

            // Then
            assertThat(event.getOutboxId()).isEqualTo(newOutboxId);
            assertThat(event.getProductId()).isEqualTo(newProductId);
            assertThat(event.getProductJson()).isEqualTo(productJson);
            assertThat(event.getCreatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("복잡한 JSON도 올바르게 저장")
        void shouldHandleComplexJson() {
            // Given
            String complexJson = """
                {
                    "id": 100,
                    "name": "복잡한 상품",
                    "options": [
                        {"name": "색상", "value": "빨강"},
                        {"name": "사이즈", "value": "L"}
                    ],
                    "price": 50000
                }
                """;

            // When
            ProductSyncOutboxCreatedEvent event = new ProductSyncOutboxCreatedEvent(
                OUTBOX_ID,
                PRODUCT_ID,
                complexJson,
                CREATED_AT
            );

            // Then
            assertThat(event.getProductJson()).isEqualTo(complexJson);
        }

        @Test
        @DisplayName("EventListener가 이벤트를 처리하는 시나리오")
        void shouldBeProcessableByEventListener() {
            // Given
            ProductSyncOutboxCreatedEvent event = createValidEvent();

            // When (EventListener 시뮬레이션)
            Long outboxId = event.getOutboxId();
            String json = event.getProductJson();

            // Then
            assertThat(outboxId).isNotNull();
            assertThat(json).isNotBlank();
            // EventListener는 outboxId를 사용하여 Outbox를 조회하고
            // json을 파싱하여 외부 API로 전송
        }
    }

    private ProductSyncOutboxCreatedEvent createValidEvent() {
        return new ProductSyncOutboxCreatedEvent(
            OUTBOX_ID,
            PRODUCT_ID,
            PRODUCT_JSON,
            CREATED_AT
        );
    }
}
