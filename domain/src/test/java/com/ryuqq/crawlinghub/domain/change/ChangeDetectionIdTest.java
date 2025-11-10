package com.ryuqq.crawlinghub.domain.change;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * ChangeDetectionId 테스트
 */
@DisplayName("ChangeDetectionId 테스트")
class ChangeDetectionIdTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 양수 ID로 ChangeDetectionId 생성")
        void shouldCreateWithValidPositiveId() {
            Long value = 1L;
            ChangeDetectionId id = ChangeDetectionId.of(value);

            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 생성 (null 허용)")
        void shouldCreateWithNullValue() {
            ChangeDetectionId id = ChangeDetectionId.of(null);

            assertThat(id).isNotNull();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("0 값으로 생성 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForZeroValue() {
            Long value = 0L;

            assertThatThrownBy(() -> ChangeDetectionId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangeDetection ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 값으로 생성 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForNegativeValue() {
            Long value = -1L;

            assertThatThrownBy(() -> ChangeDetectionId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangeDetection ID는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {-100L, -50L, -10L, -5L, -2L, -1L, 0L})
        @DisplayName("모든 비양수 값은 IllegalArgumentException 발생")
        void shouldThrowExceptionForAllNonPositiveValues(Long value) {
            assertThatThrownBy(() -> ChangeDetectionId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ChangeDetection ID는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 10L, 100L, 1000L, Long.MAX_VALUE})
        @DisplayName("모든 양수 값은 정상 생성")
        void shouldCreateForAllPositiveValues(Long value) {
            assertThatCode(() -> {
                ChangeDetectionId id = ChangeDetectionId.of(value);
                assertThat(id.value()).isEqualTo(value);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Long.MAX_VALUE로 생성 가능")
        void shouldCreateWithMaxLongValue() {
            Long maxValue = Long.MAX_VALUE;
            ChangeDetectionId id = ChangeDetectionId.of(maxValue);

            assertThat(id.value()).isEqualTo(maxValue);
        }
    }

    @Nested
    @DisplayName("Record 특성 테스트")
    class RecordCharacteristicsTests {

        @Test
        @DisplayName("Record는 final 클래스")
        void shouldBeFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(ChangeDetectionId.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Record는 java.lang.Record를 상속")
        void shouldExtendRecord() {
            assertThat(Record.class.isAssignableFrom(ChangeDetectionId.class)).isTrue();
        }

        @Test
        @DisplayName("Record의 필드는 자동으로 private final")
        void shouldHavePrivateFinalField() throws NoSuchFieldException {
            java.lang.reflect.Field valueField = ChangeDetectionId.class.getDeclaredField("value");

            assertThat(java.lang.reflect.Modifier.isPrivate(valueField.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isFinal(valueField.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Record는 자동으로 public accessor 제공")
        void shouldProvidePublicAccessor() throws NoSuchMethodException {
            java.lang.reflect.Method valueMethod = ChangeDetectionId.class.getMethod("value");

            assertThat(java.lang.reflect.Modifier.isPublic(valueMethod.getModifiers())).isTrue();
            assertThat(valueMethod.getReturnType()).isEqualTo(Long.class);
        }
    }

    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 value를 가진 두 인스턴스는 equals")
        void shouldBeEqualForSameValue() {
            ChangeDetectionId id1 = ChangeDetectionId.of(100L);
            ChangeDetectionId id2 = ChangeDetectionId.of(100L);

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("다른 value를 가진 두 인스턴스는 not equals")
        void shouldNotBeEqualForDifferentValue() {
            ChangeDetectionId id1 = ChangeDetectionId.of(100L);
            ChangeDetectionId id2 = ChangeDetectionId.of(200L);

            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("같은 value를 가진 두 인스턴스는 같은 hashCode")
        void shouldHaveSameHashCodeForSameValue() {
            ChangeDetectionId id1 = ChangeDetectionId.of(100L);
            ChangeDetectionId id2 = ChangeDetectionId.of(100L);

            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("null value를 가진 두 인스턴스도 equals")
        void shouldBeEqualForNullValue() {
            ChangeDetectionId id1 = ChangeDetectionId.of(null);
            ChangeDetectionId id2 = ChangeDetectionId.of(null);

            assertThat(id1).isEqualTo(id2);
        }

        @Test
        @DisplayName("null value와 non-null value는 not equals")
        void shouldNotBeEqualForNullAndNonNull() {
            ChangeDetectionId idNull = ChangeDetectionId.of(null);
            ChangeDetectionId idNonNull = ChangeDetectionId.of(100L);

            assertThat(idNull).isNotEqualTo(idNonNull);
        }

        @Test
        @DisplayName("자기 자신과는 항상 equals")
        void shouldBeEqualToItself() {
            ChangeDetectionId id = ChangeDetectionId.of(100L);
            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("null과는 not equals")
        void shouldNotBeEqualToNull() {
            ChangeDetectionId id = ChangeDetectionId.of(100L);
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입과는 not equals")
        void shouldNotBeEqualToDifferentType() {
            ChangeDetectionId id = ChangeDetectionId.of(100L);
            Object other = new Object();

            assertThat(id).isNotEqualTo(other);
        }
    }

    @Nested
    @DisplayName("toString 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 Record 표준 형식 반환")
        void shouldReturnStandardRecordFormat() {
            ChangeDetectionId id = ChangeDetectionId.of(100L);
            String result = id.toString();

            assertThat(result).contains("ChangeDetectionId");
            assertThat(result).contains("value=100");
        }

        @Test
        @DisplayName("null value도 toString() 정상 동작")
        void shouldHandleNullValueInToString() {
            ChangeDetectionId id = ChangeDetectionId.of(null);
            String result = id.toString();

            assertThat(result).contains("ChangeDetectionId");
            assertThat(result).contains("value=null");
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            java.util.Map<ChangeDetectionId, String> map = new java.util.HashMap<>();

            ChangeDetectionId id = ChangeDetectionId.of(100L);
            map.put(id, "Detection 100");

            String result = map.get(ChangeDetectionId.of(100L));
            assertThat(result).isEqualTo("Detection 100");
        }

        @Test
        @DisplayName("Set에서 중복 제거")
        void shouldEliminateDuplicatesInSet() {
            java.util.Set<ChangeDetectionId> set = new java.util.HashSet<>();

            set.add(ChangeDetectionId.of(100L));
            set.add(ChangeDetectionId.of(100L));
            set.add(ChangeDetectionId.of(200L));

            assertThat(set).hasSize(2);
            assertThat(set).containsExactlyInAnyOrder(
                ChangeDetectionId.of(100L),
                ChangeDetectionId.of(200L)
            );
        }

        @Test
        @DisplayName("List 정렬 가능")
        void shouldBeSortableInList() {
            java.util.List<ChangeDetectionId> list = new java.util.ArrayList<>();
            list.add(ChangeDetectionId.of(300L));
            list.add(ChangeDetectionId.of(100L));
            list.add(ChangeDetectionId.of(200L));

            list.sort(java.util.Comparator.comparing(ChangeDetectionId::value));

            assertThat(list).extracting(ChangeDetectionId::value)
                .containsExactly(100L, 200L, 300L);
        }

        @Test
        @DisplayName("Stream에서 필터링")
        void shouldWorkInStreamFilter() {
            java.util.List<ChangeDetectionId> ids = java.util.List.of(
                ChangeDetectionId.of(100L),
                ChangeDetectionId.of(200L),
                ChangeDetectionId.of(300L)
            );

            java.util.List<ChangeDetectionId> filtered = ids.stream()
                .filter(id -> id.value() > 150L)
                .toList();

            assertThat(filtered).hasSize(2);
            assertThat(filtered).extracting(ChangeDetectionId::value)
                .containsExactly(200L, 300L);
        }

        @Test
        @DisplayName("Optional과 함께 사용")
        void shouldWorkWithOptional() {
            java.util.Optional<ChangeDetectionId> optionalId =
                java.util.Optional.of(ChangeDetectionId.of(100L));

            assertThat(optionalId).isPresent();
            assertThat(optionalId.get().value()).isEqualTo(100L);
        }
    }
}
