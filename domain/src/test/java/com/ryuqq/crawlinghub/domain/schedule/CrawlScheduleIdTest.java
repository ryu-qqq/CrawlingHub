package com.ryuqq.crawlinghub.domain.schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CrawlScheduleId 테스트")
class CrawlScheduleIdTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 양수로 CrawlScheduleId 생성")
        void shouldCreateWithValidPositiveValue() {
            // Given
            Long value = 1L;

            // When
            CrawlScheduleId id = new CrawlScheduleId(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 CrawlScheduleId 생성 (허용)")
        void shouldCreateWithNullValue() {
            // Given
            Long value = null;

            // When
            CrawlScheduleId id = new CrawlScheduleId(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("큰 양수로 CrawlScheduleId 생성")
        void shouldCreateWithLargePositiveValue() {
            // Given
            Long value = Long.MAX_VALUE;

            // When
            CrawlScheduleId id = new CrawlScheduleId(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
        @DisplayName("0 또는 음수는 IllegalArgumentException 발생")
        void shouldThrowExceptionForNonPositiveValue(Long value) {
            // When & Then
            assertThatThrownBy(() -> new CrawlScheduleId(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("0으로 생성 시도는 예외 발생")
        void shouldThrowExceptionForZero() {
            // When & Then
            assertThatThrownBy(() -> new CrawlScheduleId(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("유효한 양수로 CrawlScheduleId 생성")
        void shouldCreateWithValidValue() {
            // Given
            Long value = 12345L;

            // When
            CrawlScheduleId id = CrawlScheduleId.of(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("null 값으로 CrawlScheduleId 생성 (허용)")
        void shouldCreateWithNullValue() {
            // Given
            Long value = null;

            // When
            CrawlScheduleId id = CrawlScheduleId.of(value);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isNull();
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -999L})
        @DisplayName("0 또는 음수는 IllegalArgumentException 발생")
        void shouldThrowExceptionForNonPositiveValue(Long value) {
            // When & Then
            assertThatThrownBy(() -> CrawlScheduleId.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("of()와 생성자는 동일한 객체 생성")
        void shouldCreateSameObjectAsConstructor() {
            // Given
            Long value = 100L;

            // When
            CrawlScheduleId id1 = CrawlScheduleId.of(value);
            CrawlScheduleId id2 = new CrawlScheduleId(value);

            // Then
            assertThat(id1).isEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("value() accessor 테스트")
    class ValueAccessorTests {

        @Test
        @DisplayName("value()는 생성자에 전달된 값을 반환")
        void shouldReturnValuePassedToConstructor() {
            // Given
            Long value = 999L;
            CrawlScheduleId id = new CrawlScheduleId(value);

            // When
            Long result = id.value();

            // Then
            assertThat(result).isEqualTo(value);
        }

        @Test
        @DisplayName("null로 생성된 경우 value()는 null 반환")
        void shouldReturnNullWhenCreatedWithNull() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(null);

            // When
            Long result = id.value();

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("value()는 불변 (immutable)")
        void shouldBeImmutable() {
            // Given
            Long originalValue = 123L;
            CrawlScheduleId id = new CrawlScheduleId(originalValue);

            // When
            Long retrievedValue = id.value();

            // Then
            assertThat(retrievedValue).isEqualTo(originalValue);
            // Record는 불변이므로 값을 변경할 수 없음
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("같은 값으로 생성된 두 객체는 동일")
        void shouldBeEqualForSameValue() {
            // Given
            Long value = 100L;
            CrawlScheduleId id1 = new CrawlScheduleId(value);
            CrawlScheduleId id2 = new CrawlScheduleId(value);

            // Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값으로 생성된 두 객체는 다름")
        void shouldNotBeEqualForDifferentValue() {
            // Given
            CrawlScheduleId id1 = new CrawlScheduleId(100L);
            CrawlScheduleId id2 = new CrawlScheduleId(200L);

            // Then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("같은 객체는 자기 자신과 동일")
        void shouldBeEqualToItself() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // Then
            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("null과는 동일하지 않음")
        void shouldNotBeEqualToNull() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // Then
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 타입 객체와는 동일하지 않음")
        void shouldNotBeEqualToDifferentType() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);
            Long otherObject = 100L;

            // Then
            assertThat(id).isNotEqualTo(otherObject);
        }

        @Test
        @DisplayName("hashCode는 일관성 있게 반환")
        void shouldReturnConsistentHashCode() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // When
            int hashCode1 = id.hashCode();
            int hashCode2 = id.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("null 값으로 생성된 두 객체는 동일")
        void shouldBeEqualForNullValues() {
            // Given
            CrawlScheduleId id1 = new CrawlScheduleId(null);
            CrawlScheduleId id2 = new CrawlScheduleId(null);

            // Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("null과 양수는 다름")
        void shouldNotBeEqualForNullAndPositive() {
            // Given
            CrawlScheduleId id1 = new CrawlScheduleId(null);
            CrawlScheduleId id2 = new CrawlScheduleId(100L);

            // Then
            assertThat(id1).isNotEqualTo(id2);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 null이 아님")
        void shouldHaveNonNullToString() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // When
            String result = id.toString();

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("toString()은 비어있지 않음")
        void shouldHaveNonEmptyToString() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // When
            String result = id.toString();

            // Then
            assertThat(result).isNotEmpty();
        }

        @Test
        @DisplayName("toString()은 값을 포함")
        void shouldContainValueInToString() {
            // Given
            Long value = 12345L;
            CrawlScheduleId id = new CrawlScheduleId(value);

            // When
            String result = id.toString();

            // Then
            assertThat(result).contains(value.toString());
        }

        @Test
        @DisplayName("toString()은 클래스명 포함")
        void shouldContainClassNameInToString() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // When
            String result = id.toString();

            // Then
            assertThat(result).contains("CrawlScheduleId");
        }

        @Test
        @DisplayName("null 값의 toString()도 정상 작동")
        void shouldHandleNullInToString() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(null);

            // When
            String result = id.toString();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).contains("null");
        }
    }

    @Nested
    @DisplayName("Record 특성 테스트")
    class RecordCharacteristicsTests {

        @Test
        @DisplayName("Record는 final 클래스")
        void shouldBeFinalClass() {
            // Given
            Class<CrawlScheduleId> clazz = CrawlScheduleId.class;

            // Then
            assertThat(java.lang.reflect.Modifier.isFinal(clazz.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Record는 불변 (immutable)")
        void shouldBeImmutable() {
            // Given
            Long value = 100L;
            CrawlScheduleId id = new CrawlScheduleId(value);

            // When
            Long retrievedValue = id.value();

            // Then
            assertThat(retrievedValue).isEqualTo(value);
            // Record 필드는 final이므로 변경 불가
        }

        @Test
        @DisplayName("Record는 component accessor를 가짐")
        void shouldHaveComponentAccessor() {
            // Given
            CrawlScheduleId id = new CrawlScheduleId(100L);

            // Then
            assertThatCode(() -> id.value()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("DB에서 조회한 ID로 CrawlScheduleId 생성")
        void shouldCreateFromDatabaseId() {
            // Given
            Long dbId = 999L;

            // When
            CrawlScheduleId id = CrawlScheduleId.of(dbId);

            // Then
            assertThat(id.value()).isEqualTo(dbId);
        }

        @Test
        @DisplayName("새 엔티티는 null ID로 시작 가능")
        void shouldAllowNullForNewEntity() {
            // When
            CrawlScheduleId id = CrawlScheduleId.of(null);

            // Then
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("ID 기반 동등성 비교 시나리오")
        void shouldCompareEqualityBasedOnId() {
            // Given
            CrawlScheduleId id1 = CrawlScheduleId.of(100L);
            CrawlScheduleId id2 = CrawlScheduleId.of(100L);
            CrawlScheduleId id3 = CrawlScheduleId.of(200L);

            // Then
            assertThat(id1).isEqualTo(id2);  // 같은 ID
            assertThat(id1).isNotEqualTo(id3);  // 다른 ID
        }

        @Test
        @DisplayName("Map의 키로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            java.util.Map<CrawlScheduleId, String> map = new java.util.HashMap<>();
            CrawlScheduleId id = CrawlScheduleId.of(100L);

            // When
            map.put(id, "Schedule Name");

            // Then
            assertThat(map.get(id)).isEqualTo("Schedule Name");
            assertThat(map.containsKey(CrawlScheduleId.of(100L))).isTrue();
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            java.util.Set<CrawlScheduleId> set = new java.util.HashSet<>();
            CrawlScheduleId id1 = CrawlScheduleId.of(100L);
            CrawlScheduleId id2 = CrawlScheduleId.of(100L);

            // When
            set.add(id1);
            set.add(id2);

            // Then
            assertThat(set).hasSize(1);  // 중복 제거됨
        }

        @Test
        @DisplayName("잘못된 ID 검증 시나리오")
        void shouldValidateInvalidId() {
            // When & Then
            assertThatThrownBy(() -> CrawlScheduleId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 양수여야 합니다");

            assertThatThrownBy(() -> CrawlScheduleId.of(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlSchedule ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("ID 값 추출 시나리오")
        void shouldExtractIdValue() {
            // Given
            Long expectedValue = 12345L;
            CrawlScheduleId id = CrawlScheduleId.of(expectedValue);

            // When
            Long actualValue = id.value();

            // Then
            assertThat(actualValue).isEqualTo(expectedValue);
        }
    }
}
