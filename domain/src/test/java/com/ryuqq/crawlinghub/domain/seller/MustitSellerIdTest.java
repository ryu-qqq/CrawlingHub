package com.ryuqq.crawlinghub.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * MustitSellerId 테스트
 */
@DisplayName("MustitSellerId 테스트")
class MustitSellerIdTest {

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 양수 ID로 MustitSellerId 생성")
        void shouldCreateWithValidPositiveId() {
            // given
            Long value = 1L;

            // when
            MustitSellerId id = MustitSellerId.of(value);

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("큰 양수 ID로 생성")
        void shouldCreateWithLargePositiveId() {
            // given
            Long value = Long.MAX_VALUE;

            // when
            MustitSellerId id = MustitSellerId.of(value);

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 생성 (null 허용)")
        void shouldCreateWithNullValue() {
            // when
            MustitSellerId id = MustitSellerId.of(null);

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("0 값으로 생성 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForZeroValue() {
            // given
            Long value = 0L;

            // when & then
            assertThatThrownBy(() -> MustitSellerId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MustitSeller ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("음수 값으로 생성 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForNegativeValue() {
            // given
            Long value = -1L;

            // when & then
            assertThatThrownBy(() -> MustitSellerId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MustitSeller ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("Long.MIN_VALUE로 생성 시 IllegalArgumentException 발생")
        void shouldThrowExceptionForMinValue() {
            // given
            Long value = Long.MIN_VALUE;

            // when & then
            assertThatThrownBy(() -> MustitSellerId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MustitSeller ID는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {-100L, -50L, -10L, -5L, -2L, -1L, 0L})
        @DisplayName("모든 비양수 값은 IllegalArgumentException 발생")
        void shouldThrowExceptionForAllNonPositiveValues(Long value) {
            assertThatThrownBy(() -> MustitSellerId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MustitSeller ID는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {1L, 2L, 5L, 10L, 50L, 100L, 1000L, 10000L})
        @DisplayName("모든 양수 값으로 정상 생성")
        void shouldCreateWithAllPositiveValues(Long value) {
            // when
            MustitSellerId id = MustitSellerId.of(value);

            // then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }
    }

    @Nested
    @DisplayName("value() 메서드 테스트")
    class ValueMethodTests {

        @Test
        @DisplayName("생성 시 전달한 값을 정확히 반환")
        void shouldReturnExactValueProvidedDuringCreation() {
            // given
            Long expectedValue = 12345L;
            MustitSellerId id = MustitSellerId.of(expectedValue);

            // when
            Long actualValue = id.value();

            // then
            assertThat(actualValue).isEqualTo(expectedValue);
        }

        @Test
        @DisplayName("null 값을 저장한 경우 null 반환")
        void shouldReturnNullWhenCreatedWithNull() {
            // given
            MustitSellerId id = MustitSellerId.of(null);

            // when
            Long actualValue = id.value();

            // then
            assertThat(actualValue).isNull();
        }

        @Test
        @DisplayName("value()는 불변이므로 매번 동일한 참조 반환")
        void shouldReturnSameReferenceOnMultipleCalls() {
            // given
            Long value = 100L;
            MustitSellerId id = MustitSellerId.of(value);

            // when
            Long value1 = id.value();
            Long value2 = id.value();

            // then
            assertThat(value1).isSameAs(value2);
        }

        @Test
        @DisplayName("Long.MAX_VALUE 반환 확인")
        void shouldReturnMaxValue() {
            // given
            MustitSellerId id = MustitSellerId.of(Long.MAX_VALUE);

            // when
            Long actualValue = id.value();

            // then
            assertThat(actualValue).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("동일한 ID 값을 가진 MustitSellerId는 equals")
        void shouldBeEqualForSameIdValue() {
            // given
            MustitSellerId id1 = MustitSellerId.of(100L);
            MustitSellerId id2 = MustitSellerId.of(100L);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 ID 값을 가진 MustitSellerId는 not equals")
        void shouldNotBeEqualForDifferentIdValues() {
            // given
            MustitSellerId id1 = MustitSellerId.of(100L);
            MustitSellerId id2 = MustitSellerId.of(200L);

            // when & then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("자기 자신과는 equals")
        void shouldBeEqualToItself() {
            // given
            MustitSellerId id = MustitSellerId.of(100L);

            // when & then
            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("null과는 not equals")
        void shouldNotBeEqualToNull() {
            // given
            MustitSellerId id = MustitSellerId.of(100L);

            // when & then
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 not equals")
        void shouldNotBeEqualToDifferentType() {
            // given
            MustitSellerId id = MustitSellerId.of(100L);
            Object other = "100";

            // when & then
            assertThat(id).isNotEqualTo(other);
        }

        @Test
        @DisplayName("null 값을 가진 두 MustitSellerId는 equals")
        void shouldBeEqualForNullValues() {
            // given
            MustitSellerId id1 = MustitSellerId.of(null);
            MustitSellerId id2 = MustitSellerId.of(null);

            // when & then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("동일한 값으로 여러 번 생성해도 equals")
        void shouldBeEqualWhenCreatedMultipleTimes() {
            // given
            Long value = 999L;

            // when
            MustitSellerId id1 = MustitSellerId.of(value);
            MustitSellerId id2 = MustitSellerId.of(value);
            MustitSellerId id3 = MustitSellerId.of(value);

            // then
            assertThat(id1).isEqualTo(id2).isEqualTo(id3);
            assertThat(id1.hashCode())
                .isEqualTo(id2.hashCode())
                .isEqualTo(id3.hashCode());
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 Record 표준 형식 반환")
        void shouldReturnStandardRecordFormat() {
            // given
            MustitSellerId id = MustitSellerId.of(100L);

            // when
            String result = id.toString();

            // then
            assertThat(result).isEqualTo("MustitSellerId[value=100]");
        }

        @Test
        @DisplayName("null 값일 때 toString() 확인")
        void shouldReturnToStringForNullValue() {
            // given
            MustitSellerId id = MustitSellerId.of(null);

            // when
            String result = id.toString();

            // then
            assertThat(result).isEqualTo("MustitSellerId[value=null]");
        }

        @Test
        @DisplayName("Long.MAX_VALUE 일 때 toString() 확인")
        void shouldReturnToStringForMaxValue() {
            // given
            MustitSellerId id = MustitSellerId.of(Long.MAX_VALUE);

            // when
            String result = id.toString();

            // then
            assertThat(result).isEqualTo("MustitSellerId[value=" + Long.MAX_VALUE + "]");
        }

        @Test
        @DisplayName("작은 값일 때 toString() 확인")
        void shouldReturnToStringForSmallValue() {
            // given
            MustitSellerId id = MustitSellerId.of(1L);

            // when
            String result = id.toString();

            // then
            assertThat(result).isEqualTo("MustitSellerId[value=1]");
        }
    }

    @Nested
    @DisplayName("Record 특성 테스트")
    class RecordCharacteristicsTests {

        @Test
        @DisplayName("Record는 final 클래스")
        void shouldBeFinalClass() {
            assertThat(java.lang.reflect.Modifier.isFinal(MustitSellerId.class.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Record는 java.lang.Record를 상속")
        void shouldExtendRecord() {
            assertThat(Record.class.isAssignableFrom(MustitSellerId.class)).isTrue();
        }

        @Test
        @DisplayName("value 필드는 private final")
        void shouldHavePrivateFinalValueField() throws NoSuchFieldException {
            var field = MustitSellerId.class.getDeclaredField("value");
            assertThat(java.lang.reflect.Modifier.isPrivate(field.getModifiers())).isTrue();
            assertThat(java.lang.reflect.Modifier.isFinal(field.getModifiers())).isTrue();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // given
            java.util.Map<MustitSellerId, String> map = new java.util.HashMap<>();
            MustitSellerId id = MustitSellerId.of(100L);

            // when
            map.put(id, "Seller 100");
            String result = map.get(MustitSellerId.of(100L));

            // then
            assertThat(result).isEqualTo("Seller 100");
        }

        @Test
        @DisplayName("Set에 추가 가능 (중복 제거)")
        void shouldBeUsableInSet() {
            // given
            java.util.Set<MustitSellerId> set = new java.util.HashSet<>();
            MustitSellerId id1 = MustitSellerId.of(100L);
            MustitSellerId id2 = MustitSellerId.of(100L);
            MustitSellerId id3 = MustitSellerId.of(200L);

            // when
            set.add(id1);
            set.add(id2);
            set.add(id3);

            // then
            assertThat(set).hasSize(2);
            assertThat(set).contains(id1, id3);
        }

        @Test
        @DisplayName("리스트에서 contains() 동작 확인")
        void shouldWorkWithListContains() {
            // given
            java.util.List<MustitSellerId> list = new java.util.ArrayList<>();
            MustitSellerId id = MustitSellerId.of(100L);
            list.add(id);

            // when
            boolean contains = list.contains(MustitSellerId.of(100L));

            // then
            assertThat(contains).isTrue();
        }

        @Test
        @DisplayName("Stream filter 동작 확인")
        void shouldWorkWithStreamFilter() {
            // given
            java.util.List<MustitSellerId> ids = java.util.List.of(
                MustitSellerId.of(1L),
                MustitSellerId.of(2L),
                MustitSellerId.of(3L)
            );

            // when
            java.util.List<MustitSellerId> filtered = ids.stream()
                .filter(id -> id.value() != null && id.value() > 1L)
                .toList();

            // then
            assertThat(filtered).hasSize(2);
            assertThat(filtered).extracting(MustitSellerId::value)
                .containsExactly(2L, 3L);
        }

        @Test
        @DisplayName("null 값을 가진 ID도 컬렉션에서 처리 가능")
        void shouldHandleNullValueInCollections() {
            // given
            java.util.List<MustitSellerId> ids = java.util.List.of(
                MustitSellerId.of(1L),
                MustitSellerId.of(null),
                MustitSellerId.of(3L)
            );

            // when
            long nullCount = ids.stream()
                .filter(id -> id.value() == null)
                .count();

            // then
            assertThat(nullCount).isEqualTo(1L);
        }

        @Test
        @DisplayName("정렬 가능 (Comparator 사용)")
        void shouldBeSortableWithComparator() {
            // given
            java.util.List<MustitSellerId> ids = new java.util.ArrayList<>(java.util.List.of(
                MustitSellerId.of(3L),
                MustitSellerId.of(1L),
                MustitSellerId.of(2L)
            ));

            // when
            ids.sort(java.util.Comparator.comparing(
                MustitSellerId::value,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
            ));

            // then
            assertThat(ids).extracting(MustitSellerId::value)
                .containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("동일한 ID를 가진 여러 인스턴스는 equals")
        void shouldHaveSameEqualityAcrossInstances() {
            // given
            Long value = 12345L;

            // when
            MustitSellerId id1 = MustitSellerId.of(value);
            MustitSellerId id2 = MustitSellerId.of(value);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("ID 값 비교 로직 검증")
        void shouldCompareIdValuesCorrectly() {
            // given
            MustitSellerId id1 = MustitSellerId.of(100L);
            MustitSellerId id2 = MustitSellerId.of(200L);

            // when
            boolean isSame = id1.value().equals(id2.value());

            // then
            assertThat(isSame).isFalse();
            assertThat(id1.value()).isLessThan(id2.value());
        }

        @Test
        @DisplayName("큰 숫자 ID 처리 확인")
        void shouldHandleLargeNumberIds() {
            // given
            Long largeValue = 999_999_999_999L;

            // when
            MustitSellerId id = MustitSellerId.of(largeValue);

            // then
            assertThat(id.value()).isEqualTo(largeValue);
            assertThat(id.toString()).contains(largeValue.toString());
        }

        @Test
        @DisplayName("ID 불변성 검증")
        void shouldBeImmutable() {
            // given
            Long originalValue = 100L;
            MustitSellerId id = MustitSellerId.of(originalValue);

            // when
            Long retrievedValue = id.value();

            // then
            assertThat(retrievedValue).isEqualTo(originalValue);
            // Record는 불변이므로 value 필드를 변경할 수 없음
        }
    }
}
