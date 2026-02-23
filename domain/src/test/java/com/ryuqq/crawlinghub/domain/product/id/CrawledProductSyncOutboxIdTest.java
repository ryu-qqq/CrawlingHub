package com.ryuqq.crawlinghub.domain.product.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("id")
@DisplayName("CrawledProductSyncOutboxId 단위 테스트")
class CrawledProductSyncOutboxIdTest {

    @Nested
    @DisplayName("forNew() 팩토리 메서드 테스트")
    class ForNewTest {

        @Test
        @DisplayName("미할당 ID를 생성한다 (value = null)")
        void createUnassignedId() {
            // when
            CrawledProductSyncOutboxId id = CrawledProductSyncOutboxId.forNew();

            // then
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("of() 팩토리 메서드 테스트")
    class OfTest {

        @Test
        @DisplayName("유효한 양수 값으로 ID를 생성한다")
        void createWithValidValue() {
            // when
            CrawledProductSyncOutboxId id = CrawledProductSyncOutboxId.of(1L);

            // then
            assertThat(id.value()).isEqualTo(1L);
            assertThat(id.isNew()).isFalse();
        }

        @Test
        @DisplayName("큰 양수 값으로 ID를 생성한다")
        void createWithLargeValue() {
            // when
            CrawledProductSyncOutboxId id = CrawledProductSyncOutboxId.of(Long.MAX_VALUE);

            // then
            assertThat(id.value()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("null 값으로 생성하면 예외가 발생한다")
        void throwWhenValueIsNull() {
            assertThatThrownBy(() -> CrawledProductSyncOutboxId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("Compact Constructor 검증 테스트")
    class CompactConstructorTest {

        @Test
        @DisplayName("0 값으로 생성하면 예외가 발생한다")
        void throwWhenValueIsZero() {
            assertThatThrownBy(() -> new CrawledProductSyncOutboxId(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("음수 값으로 생성하면 예외가 발생한다")
        void throwWhenValueIsNegative() {
            assertThatThrownBy(() -> new CrawledProductSyncOutboxId(-1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("양수");
        }

        @Test
        @DisplayName("null 값으로 직접 생성하면 성공한다 (forNew 패턴)")
        void createWithNullDirectly() {
            // when
            CrawledProductSyncOutboxId id = new CrawledProductSyncOutboxId(null);

            // then
            assertThat(id.value()).isNull();
            assertThat(id.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("isNew() 메서드 테스트")
    class IsNewTest {

        @Test
        @DisplayName("value가 null이면 isNew()는 true이다")
        void isNewWhenValueIsNull() {
            assertThat(CrawledProductSyncOutboxId.forNew().isNew()).isTrue();
        }

        @Test
        @DisplayName("value가 있으면 isNew()는 false이다")
        void isNewWhenValueIsPresent() {
            assertThat(CrawledProductSyncOutboxId.of(1L).isNew()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 value이면 동일하다")
        void sameValueAreEqual() {
            // given
            CrawledProductSyncOutboxId id1 = CrawledProductSyncOutboxId.of(1L);
            CrawledProductSyncOutboxId id2 = CrawledProductSyncOutboxId.of(1L);

            // then
            assertThat(id1).isEqualTo(id2);
            assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        }

        @Test
        @DisplayName("다른 value이면 다르다")
        void differentValueAreNotEqual() {
            // given
            CrawledProductSyncOutboxId id1 = CrawledProductSyncOutboxId.of(1L);
            CrawledProductSyncOutboxId id2 = CrawledProductSyncOutboxId.of(2L);

            // then
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("두 forNew() ID는 동일하다 (null == null)")
        void twoForNewIdsAreEqual() {
            // given
            CrawledProductSyncOutboxId id1 = CrawledProductSyncOutboxId.forNew();
            CrawledProductSyncOutboxId id2 = CrawledProductSyncOutboxId.forNew();

            // then
            assertThat(id1).isEqualTo(id2);
        }
    }
}
