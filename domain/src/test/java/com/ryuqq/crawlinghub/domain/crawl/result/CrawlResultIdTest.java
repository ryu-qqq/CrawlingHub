package com.ryuqq.crawlinghub.domain.crawl.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CrawlResultId 테스트")
class CrawlResultIdTest {

    private static final Long VALID_ID = 100L;

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("유효한 양수 ID로 생성 성공")
        void shouldCreateWithValidPositiveId() {
            // When
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // Then
            assertThat(id.value()).isEqualTo(VALID_ID);
        }

        @Test
        @DisplayName("ID 값 1로 생성 가능")
        void shouldCreateWithMinimumId() {
            // When
            CrawlResultId id = new CrawlResultId(1L);

            // Then
            assertThat(id.value()).isEqualTo(1L);
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("null ID는 예외 발생")
        void shouldThrowExceptionWhenIdIsNull(Long nullId) {
            // When & Then
            assertThatThrownBy(() -> new CrawlResultId(nullId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlResult ID는 양수여야 합니다");
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, -1L, -100L})
        @DisplayName("0 또는 음수 ID는 예외 발생")
        void shouldThrowExceptionWhenIdIsNotPositive(Long invalidId) {
            // When & Then
            assertThatThrownBy(() -> new CrawlResultId(invalidId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlResult ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfFactoryMethodTests {

        @Test
        @DisplayName("of()로 생성 성공")
        void shouldCreateUsingOfMethod() {
            // When
            CrawlResultId id = CrawlResultId.of(VALID_ID);

            // Then
            assertThat(id).isNotNull();
            assertThat(id.value()).isEqualTo(VALID_ID);
        }

        @Test
        @DisplayName("of()와 생성자는 동일한 객체 생성")
        void shouldCreateSameObjectViaOfAndConstructor() {
            // Given
            CrawlResultId directId = new CrawlResultId(VALID_ID);
            CrawlResultId factoryId = CrawlResultId.of(VALID_ID);

            // Then
            assertThat(factoryId).isEqualTo(directId);
        }

        @Test
        @DisplayName("of()도 null 검증")
        void shouldValidateNullInOfMethod() {
            // When & Then
            assertThatThrownBy(() -> CrawlResultId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlResult ID는 양수여야 합니다");
        }

        @Test
        @DisplayName("of()도 양수 검증")
        void shouldValidatePositiveInOfMethod() {
            // When & Then
            assertThatThrownBy(() -> CrawlResultId.of(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CrawlResult ID는 양수여야 합니다");
        }
    }

    @Nested
    @DisplayName("value() 메서드 테스트")
    class ValueMethodTests {

        @Test
        @DisplayName("value()는 정확한 ID 값 반환")
        void shouldReturnCorrectValue() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // When
            Long result = id.value();

            // Then
            assertThat(result).isEqualTo(VALID_ID);
        }

        @Test
        @DisplayName("다양한 ID 값 반환 확인")
        void shouldReturnVariousValues() {
            // Given
            CrawlResultId id1 = new CrawlResultId(1L);
            CrawlResultId id999 = new CrawlResultId(999L);
            CrawlResultId idMax = new CrawlResultId(Long.MAX_VALUE);

            // Then
            assertThat(id1.value()).isEqualTo(1L);
            assertThat(id999.value()).isEqualTo(999L);
            assertThat(idMax.value()).isEqualTo(Long.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("equals() 및 hashCode() 테스트")
    class EqualityTests {

        @Test
        @DisplayName("같은 값을 가진 ID는 동일하다")
        void shouldBeEqualWhenSameValue() {
            // Given
            CrawlResultId id1 = new CrawlResultId(VALID_ID);
            CrawlResultId id2 = new CrawlResultId(VALID_ID);

            // Then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ID는 동일하지 않다")
        void shouldNotBeEqualWhenDifferentValue() {
            // Given
            CrawlResultId id1 = new CrawlResultId(100L);
            CrawlResultId id2 = new CrawlResultId(200L);

            // Then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("자기 자신과 비교하면 동일하다")
        void shouldBeEqualToSelf() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // Then
            assertThat(id).isEqualTo(id);
        }

        @Test
        @DisplayName("null과 비교하면 동일하지 않다")
        void shouldNotBeEqualToNull() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // Then
            assertThat(id).isNotEqualTo(null);
        }

        @Test
        @DisplayName("다른 클래스 객체와 비교하면 동일하지 않다")
        void shouldNotBeEqualToDifferentClass() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);
            String otherObject = "100";

            // Then
            assertThat(id).isNotEqualTo(otherObject);
        }
    }

    @Nested
    @DisplayName("toString() 테스트")
    class ToStringTests {

        @Test
        @DisplayName("toString()은 ID 값을 포함")
        void shouldIncludeValueInToString() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // When
            String result = id.toString();

            // Then
            assertThat(result)
                .contains("CrawlResultId")
                .contains(VALID_ID.toString());
        }

        @Test
        @DisplayName("다양한 ID 값의 toString() 확인")
        void shouldHandleVariousValuesInToString() {
            // Given
            CrawlResultId id1 = new CrawlResultId(1L);
            CrawlResultId id999 = new CrawlResultId(999L);

            // When
            String str1 = id1.toString();
            String str999 = id999.toString();

            // Then
            assertThat(str1).contains("1");
            assertThat(str999).contains("999");
        }
    }

    @Nested
    @DisplayName("Record 불변성 테스트")
    class ImmutabilityTests {

        @Test
        @DisplayName("Record는 생성 후 값 변경 불가")
        void shouldBeImmutable() {
            // Given
            CrawlResultId id = new CrawlResultId(VALID_ID);

            // When
            Long originalValue = id.value();

            // Then: Record는 setter가 없으므로 값 변경 불가
            assertThat(id.value()).isEqualTo(originalValue);
        }

        @Test
        @DisplayName("같은 값으로 여러 인스턴스 생성 가능")
        void shouldAllowMultipleInstancesWithSameValue() {
            // When
            CrawlResultId id1 = new CrawlResultId(VALID_ID);
            CrawlResultId id2 = new CrawlResultId(VALID_ID);
            CrawlResultId id3 = CrawlResultId.of(VALID_ID);

            // Then: 모두 동일한 값이지만 독립적인 인스턴스
            assertThat(id1).isEqualTo(id2).isEqualTo(id3);
        }
    }

    @Nested
    @DisplayName("실제 사용 시나리오 테스트")
    class UsageScenarioTests {

        @Test
        @DisplayName("DB에서 조회한 CrawlResult ID 생성 시나리오")
        void shouldHandleDbLoadedId() {
            // Given: DB에서 조회한 ID 값
            Long dbId = 12345L;

            // When: ID 객체 생성
            CrawlResultId resultId = CrawlResultId.of(dbId);

            // Then
            assertThat(resultId.value()).isEqualTo(dbId);
        }

        @Test
        @DisplayName("신규 CrawlResult 생성 후 ID 할당 시나리오")
        void shouldHandleNewResultIdAssignment() {
            // Given: 신규 생성 후 할당받은 ID
            Long newlyAssignedId = 999L;

            // When
            CrawlResultId resultId = new CrawlResultId(newlyAssignedId);

            // Then
            assertThat(resultId).isNotNull();
            assertThat(resultId.value()).isEqualTo(newlyAssignedId);
        }

        @Test
        @DisplayName("Map의 Key로 사용 가능")
        void shouldBeUsableAsMapKey() {
            // Given
            CrawlResultId id1 = new CrawlResultId(100L);
            CrawlResultId id2 = new CrawlResultId(100L); // 같은 값

            // When & Then: equals/hashCode가 올바르면 Map에서 동일한 키로 인식
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("Set에서 중복 제거 가능")
        void shouldBeUsableInSet() {
            // Given
            CrawlResultId id1 = new CrawlResultId(VALID_ID);
            CrawlResultId id2 = new CrawlResultId(VALID_ID); // 같은 값

            // When
            java.util.Set<CrawlResultId> idSet = new java.util.HashSet<>();
            idSet.add(id1);
            idSet.add(id2);

            // Then: 같은 값이므로 Set에는 1개만 저장됨
            assertThat(idSet).hasSize(1);
        }
    }
}
