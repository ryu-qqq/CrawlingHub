package com.ryuqq.crawlinghub.domain.useragent.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("id")
@DisplayName("UserAgentId Value Object 단위 테스트")
class UserAgentIdTest {

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("null 값으로 생성한다")
        void createWithNullValue() {
            UserAgentId id = UserAgentId.forNew();
            assertThat(id.value()).isNull();
        }

        @Test
        @DisplayName("isNew()가 true를 반환한다")
        void isNewReturnsTrue() {
            UserAgentId id = UserAgentId.forNew();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfTest {

        @Test
        @DisplayName("유효한 양수 값으로 생성한다")
        void createWithValidValue() {
            UserAgentId id = UserAgentId.of(1L);
            assertThat(id.value()).isEqualTo(1L);
        }

        @Test
        @DisplayName("null이면 예외가 발생한다")
        void nullValueThrowsException() {
            assertThatThrownBy(() -> UserAgentId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("isNew()가 false를 반환한다")
        void isNewReturnsFalse() {
            UserAgentId id = UserAgentId.of(1L);
            assertThat(id.isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("생성 검증 테스트 - 직접 생성자")
    class DirectCreationValidationTest {

        @Test
        @DisplayName("0이면 예외가 발생한다")
        void zeroValueThrowsException() {
            assertThatThrownBy(() -> new UserAgentId(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("음수이면 예외가 발생한다")
        void negativeValueThrowsException() {
            assertThatThrownBy(() -> new UserAgentId(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("null은 허용된다 (forNew 패턴)")
        void nullIsAllowed() {
            UserAgentId id = new UserAgentId(null);
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValueAreEqual() {
            UserAgentId id1 = UserAgentId.of(1L);
            UserAgentId id2 = UserAgentId.of(1L);
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 값이면 다르다")
        void differentValuesAreNotEqual() {
            UserAgentId id1 = UserAgentId.of(1L);
            UserAgentId id2 = UserAgentId.of(2L);
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("forNew()로 생성한 두 ID는 동일하다")
        void twoForNewIdsAreEqual() {
            UserAgentId id1 = UserAgentId.forNew();
            UserAgentId id2 = UserAgentId.forNew();
            assertThat(id1).isEqualTo(id2);
        }
    }
}
