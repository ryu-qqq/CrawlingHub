package com.ryuqq.crawlinghub.domain.product.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProductChangeEvent 테스트")
class ProductChangeEventTest {

    private static final Long PRODUCT_ID = 100L;
    private static final ChangeSource SOURCE = ChangeSource.MINI_SHOP;
    private static final LocalDateTime OCCURRED_AT = LocalDateTime.of(2025, 11, 7, 10, 30, 0);

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 값으로 이벤트 생성 성공")
        void shouldCreateEvent() {
            // Given
            Map<String, FieldChange> changedFields = createChangedFields();

            // When
            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                changedFields,
                OCCURRED_AT
            );

            // Then
            assertThat(event.getProductId()).isEqualTo(PRODUCT_ID);
            assertThat(event.getSource()).isEqualTo(SOURCE);
            assertThat(event.getChangedFields()).hasSize(2);
            assertThat(event.getOccurredAt()).isEqualTo(OCCURRED_AT);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Product ID가 null이면 예외 발생")
        void shouldThrowExceptionWhenProductIdIsNull(Long nullProductId) {
            // Given
            Map<String, FieldChange> changedFields = createChangedFields();

            // When & Then
            assertThatThrownBy(() -> new ProductChangeEvent(
                nullProductId,
                SOURCE,
                changedFields,
                OCCURRED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product ID는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("변경 소스가 null이면 예외 발생")
        void shouldThrowExceptionWhenSourceIsNull(ChangeSource nullSource) {
            // Given
            Map<String, FieldChange> changedFields = createChangedFields();

            // When & Then
            assertThatThrownBy(() -> new ProductChangeEvent(
                PRODUCT_ID,
                nullSource,
                changedFields,
                OCCURRED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 소스는 필수입니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("변경 필드가 null이면 예외 발생")
        void shouldThrowExceptionWhenChangedFieldsIsNull(Map<String, FieldChange> nullFields) {
            // When & Then
            assertThatThrownBy(() -> new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                nullFields,
                OCCURRED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 필드는 최소 1개 이상이어야 합니다");
        }

        @Test
        @DisplayName("변경 필드가 빈 Map이면 예외 발생")
        void shouldThrowExceptionWhenChangedFieldsIsEmpty() {
            // Given
            Map<String, FieldChange> emptyFields = new HashMap<>();

            // When & Then
            assertThatThrownBy(() -> new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                emptyFields,
                OCCURRED_AT
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 필드는 최소 1개 이상이어야 합니다");
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("발생 시각이 null이면 예외 발생")
        void shouldThrowExceptionWhenOccurredAtIsNull(LocalDateTime nullOccurredAt) {
            // Given
            Map<String, FieldChange> changedFields = createChangedFields();

            // When & Then
            assertThatThrownBy(() -> new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                changedFields,
                nullOccurredAt
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("변경 발생 시각은 필수입니다");
        }

        @Test
        @DisplayName("변경 필드 Map은 불변 복사된다")
        void shouldCopyChangedFieldsAsImmutable() {
            // Given
            Map<String, FieldChange> originalFields = new HashMap<>();
            originalFields.put("name", new FieldChange("name", "old", "new"));

            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                originalFields,
                OCCURRED_AT
            );

            // When & Then
            assertThatThrownBy(() ->
                event.getChangedFields().put("another", new FieldChange("another", "a", "b"))
            ).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Getter 메서드 테스트")
    class GetterTests {

        @Test
        @DisplayName("getProductId()는 정확한 Product ID 반환")
        void shouldReturnProductId() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            Long result = event.getProductId();

            // Then
            assertThat(result).isEqualTo(PRODUCT_ID);
        }

        @Test
        @DisplayName("getSource()는 정확한 소스 반환")
        void shouldReturnSource() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            ChangeSource result = event.getSource();

            // Then
            assertThat(result).isEqualTo(SOURCE);
        }

        @Test
        @DisplayName("getChangedFields()는 불변 Map 반환")
        void shouldReturnImmutableChangedFields() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            Map<String, FieldChange> fields = event.getChangedFields();

            // Then
            assertThat(fields).hasSize(2);
            assertThat(fields).containsKey("productName");
            assertThat(fields).containsKey("price");
        }

        @Test
        @DisplayName("getOccurredAt()는 정확한 발생 시각 반환")
        void shouldReturnOccurredAt() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            LocalDateTime result = event.getOccurredAt();

            // Then
            assertThat(result).isEqualTo(OCCURRED_AT);
        }
    }

    @Nested
    @DisplayName("hasFieldChanged() 메서드 테스트")
    class HasFieldChangedTests {

        @Test
        @DisplayName("변경된 필드명을 확인하면 true 반환")
        void shouldReturnTrueForChangedField() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            boolean result = event.hasFieldChanged("productName");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("변경되지 않은 필드명을 확인하면 false 반환")
        void shouldReturnFalseForUnchangedField() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            boolean result = event.hasFieldChanged("description");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("여러 필드명을 확인 가능")
        void shouldCheckMultipleFields() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // Then
            assertThat(event.hasFieldChanged("productName")).isTrue();
            assertThat(event.hasFieldChanged("price")).isTrue();
            assertThat(event.hasFieldChanged("stock")).isFalse();
        }
    }

    @Nested
    @DisplayName("getChangedFieldCount() 메서드 테스트")
    class GetChangedFieldCountTests {

        @Test
        @DisplayName("변경된 필드 개수를 정확히 반환")
        void shouldReturnCorrectCount() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            int count = event.getChangedFieldCount();

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("1개 필드 변경 시 1 반환")
        void shouldReturnOneForSingleChange() {
            // Given
            Map<String, FieldChange> singleChange = new HashMap<>();
            singleChange.put("price", new FieldChange("price", 10000L, 15000L));

            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                singleChange,
                OCCURRED_AT
            );

            // When
            int count = event.getChangedFieldCount();

            // Then
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("5개 필드 변경 시 5 반환")
        void shouldReturnFiveForFiveChanges() {
            // Given
            Map<String, FieldChange> fiveChanges = new HashMap<>();
            fiveChanges.put("name", new FieldChange("name", "old1", "new1"));
            fiveChanges.put("price", new FieldChange("price", "old2", "new2"));
            fiveChanges.put("stock", new FieldChange("stock", "old3", "new3"));
            fiveChanges.put("description", new FieldChange("description", "old4", "new4"));
            fiveChanges.put("category", new FieldChange("category", "old5", "new5"));

            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                fiveChanges,
                OCCURRED_AT
            );

            // When
            int count = event.getChangedFieldCount();

            // Then
            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 productId, source, occurredAt을 가진 이벤트는 동일하다")
        void shouldBeEqualWhenSameIdentity() {
            // Given
            Map<String, FieldChange> fields1 = createChangedFields();
            Map<String, FieldChange> fields2 = new HashMap<>();
            fields2.put("different", new FieldChange("different", "a", "b"));

            ProductChangeEvent event1 = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                fields1,
                OCCURRED_AT
            );
            ProductChangeEvent event2 = new ProductChangeEvent(
                PRODUCT_ID,
                SOURCE,
                fields2,  // 다른 changedFields
                OCCURRED_AT
            );

            // Then
            assertThat(event1).isEqualTo(event2);
            assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        }

        @Test
        @DisplayName("다른 productId를 가진 이벤트는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentProductId() {
            // Given
            Map<String, FieldChange> fields = createChangedFields();

            ProductChangeEvent event1 = new ProductChangeEvent(1L, SOURCE, fields, OCCURRED_AT);
            ProductChangeEvent event2 = new ProductChangeEvent(2L, SOURCE, fields, OCCURRED_AT);

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 source를 가진 이벤트는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentSource() {
            // Given
            Map<String, FieldChange> fields = createChangedFields();

            ProductChangeEvent event1 = new ProductChangeEvent(
                PRODUCT_ID, ChangeSource.MINI_SHOP, fields, OCCURRED_AT
            );
            ProductChangeEvent event2 = new ProductChangeEvent(
                PRODUCT_ID, ChangeSource.OPTION, fields, OCCURRED_AT
            );

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }

        @Test
        @DisplayName("다른 occurredAt을 가진 이벤트는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentOccurredAt() {
            // Given
            Map<String, FieldChange> fields = createChangedFields();
            LocalDateTime time1 = LocalDateTime.of(2025, 11, 7, 10, 0, 0);
            LocalDateTime time2 = LocalDateTime.of(2025, 11, 7, 11, 0, 0);

            ProductChangeEvent event1 = new ProductChangeEvent(PRODUCT_ID, SOURCE, fields, time1);
            ProductChangeEvent event2 = new ProductChangeEvent(PRODUCT_ID, SOURCE, fields, time2);

            // Then
            assertThat(event1).isNotEqualTo(event2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 주요 필드를 포함")
        void shouldIncludeKeyFieldsInToString() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            String result = event.toString();

            // Then
            assertThat(result)
                .contains("ProductChangeEvent")
                .contains("productId=" + PRODUCT_ID)
                .contains("source=" + SOURCE)
                .contains("changedFieldCount=2")
                .contains("occurredAt=" + OCCURRED_AT);
        }

        @Test
        @DisplayName("toString()은 변경 필드 수를 표시")
        void shouldShowChangedFieldCount() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When
            String result = event.toString();

            // Then
            assertThat(result).contains("changedFieldCount=2");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("미니샵 크롤링 변경 시나리오")
        void shouldHandleMiniShopCrawling() {
            // Given: 미니샵 크롤링으로 이미지, 상품명, 가격 변경
            Map<String, FieldChange> changes = new HashMap<>();
            changes.put("image", new FieldChange("image", "old.jpg", "new.jpg"));
            changes.put("productName", new FieldChange("productName", "기존 상품", "새 상품"));
            changes.put("price", new FieldChange("price", 10000L, 15000L));

            // When
            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                ChangeSource.MINI_SHOP,
                changes,
                OCCURRED_AT
            );

            // Then
            assertThat(event.getSource()).isEqualTo(ChangeSource.MINI_SHOP);
            assertThat(event.getChangedFieldCount()).isEqualTo(3);
            assertThat(event.hasFieldChanged("image")).isTrue();
            assertThat(event.hasFieldChanged("productName")).isTrue();
            assertThat(event.hasFieldChanged("price")).isTrue();
        }

        @Test
        @DisplayName("옵션 크롤링 변경 시나리오")
        void shouldHandleOptionCrawling() {
            // Given: 옵션 크롤링으로 옵션, 재고 변경
            Map<String, FieldChange> changes = new HashMap<>();
            changes.put("options", new FieldChange("options", "[]", "[{\"color\":\"red\"}]"));
            changes.put("stock", new FieldChange("stock", 100, 50));

            // When
            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                ChangeSource.OPTION,
                changes,
                OCCURRED_AT
            );

            // Then
            assertThat(event.getSource()).isEqualTo(ChangeSource.OPTION);
            assertThat(event.getChangedFieldCount()).isEqualTo(2);
            assertThat(event.hasFieldChanged("options")).isTrue();
            assertThat(event.hasFieldChanged("stock")).isTrue();
        }

        @Test
        @DisplayName("상세 크롤링 변경 시나리오")
        void shouldHandleDetailCrawling() {
            // Given: 상세 크롤링으로 모듈 변경
            Map<String, FieldChange> changes = new HashMap<>();
            changes.put("productInfo", new FieldChange("productInfo", null, "new info"));
            changes.put("shipping", new FieldChange("shipping", null, "new shipping"));

            // When
            ProductChangeEvent event = new ProductChangeEvent(
                PRODUCT_ID,
                ChangeSource.DETAIL,
                changes,
                OCCURRED_AT
            );

            // Then
            assertThat(event.getSource()).isEqualTo(ChangeSource.DETAIL);
            assertThat(event.getChangedFieldCount()).isEqualTo(2);
            assertThat(event.hasFieldChanged("productInfo")).isTrue();
            assertThat(event.hasFieldChanged("shipping")).isTrue();
        }

        @Test
        @DisplayName("EventHandler가 이벤트를 처리하는 시나리오")
        void shouldBeProcessableByEventHandler() {
            // Given
            ProductChangeEvent event = createValidEvent();

            // When (EventHandler 시뮬레이션)
            Long productId = event.getProductId();
            ChangeSource source = event.getSource();
            Map<String, FieldChange> changes = event.getChangedFields();

            // Then
            assertThat(productId).isNotNull();
            assertThat(source).isNotNull();
            assertThat(changes).isNotEmpty();

            // EventHandler는 source별로 다른 처리 로직 적용
            switch (source) {
                case MINI_SHOP:
                    // 이미지, 상품명, 가격 업데이트
                    break;
                case OPTION:
                    // 옵션, 재고 업데이트
                    break;
                case DETAIL:
                    // 모듈 업데이트
                    break;
            }
        }
    }

    private ProductChangeEvent createValidEvent() {
        return new ProductChangeEvent(
            PRODUCT_ID,
            SOURCE,
            createChangedFields(),
            OCCURRED_AT
        );
    }

    private Map<String, FieldChange> createChangedFields() {
        Map<String, FieldChange> fields = new HashMap<>();
        fields.put("productName", new FieldChange("productName", "기존 상품명", "새 상품명"));
        fields.put("price", new FieldChange("price", 10000L, 15000L));
        return fields;
    }
}
