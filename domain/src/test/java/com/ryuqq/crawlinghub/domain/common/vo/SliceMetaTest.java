package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("SliceMeta Value Object 단위 테스트")
class SliceMetaTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("커서 없이 생성한다")
        void createWithoutCursor() {
            // when
            SliceMeta meta = SliceMeta.of(20, true);

            // then
            assertThat(meta.size()).isEqualTo(20);
            assertThat(meta.hasNext()).isTrue();
            assertThat(meta.cursor()).isNull();
            assertThat(meta.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("count를 포함하여 커서 없이 생성한다")
        void createWithoutCursorWithCount() {
            // when
            SliceMeta meta = SliceMeta.of(20, true, 15);

            // then
            assertThat(meta.size()).isEqualTo(20);
            assertThat(meta.hasNext()).isTrue();
            assertThat(meta.count()).isEqualTo(15);
        }

        @Test
        @DisplayName("String 커서를 포함하여 생성한다")
        void createWithStringCursor() {
            // when
            SliceMeta meta = SliceMeta.withCursor("cursor-abc", 20, true);

            // then
            assertThat(meta.cursor()).isEqualTo("cursor-abc");
            assertThat(meta.hasNext()).isTrue();
            assertThat(meta.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("String 커서와 count를 포함하여 생성한다")
        void createWithStringCursorAndCount() {
            // when
            SliceMeta meta = SliceMeta.withCursor("cursor-abc", 20, true, 18);

            // then
            assertThat(meta.cursor()).isEqualTo("cursor-abc");
            assertThat(meta.count()).isEqualTo(18);
        }

        @Test
        @DisplayName("Long ID 커서를 포함하여 생성한다")
        void createWithLongCursor() {
            // when
            SliceMeta meta = SliceMeta.withCursor(12345L, 20, true);

            // then
            assertThat(meta.cursor()).isEqualTo("12345");
            assertThat(meta.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("null Long 커서는 null 커서로 생성된다")
        void createWithNullLongCursor() {
            // when
            SliceMeta meta = SliceMeta.withCursor((Long) null, 20, false);

            // then
            assertThat(meta.cursor()).isNull();
            assertThat(meta.hasCursor()).isFalse();
        }

        @Test
        @DisplayName("Long ID 커서와 count를 포함하여 생성한다")
        void createWithLongCursorAndCount() {
            // when
            SliceMeta meta = SliceMeta.withCursor(12345L, 20, true, 20);

            // then
            assertThat(meta.cursor()).isEqualTo("12345");
            assertThat(meta.count()).isEqualTo(20);
        }

        @Test
        @DisplayName("empty()로 빈 SliceMeta를 생성한다")
        void createEmpty() {
            // when
            SliceMeta meta = SliceMeta.empty();

            // then
            assertThat(meta.size()).isEqualTo(SliceMeta.DEFAULT_SIZE);
            assertThat(meta.hasNext()).isFalse();
            assertThat(meta.cursor()).isNull();
            assertThat(meta.count()).isEqualTo(0);
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("크기를 지정하여 빈 SliceMeta를 생성한다")
        void createEmptyWithSize() {
            // when
            SliceMeta meta = SliceMeta.empty(50);

            // then
            assertThat(meta.size()).isEqualTo(50);
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("0 이하 size는 DEFAULT_SIZE로 정규화된다")
        void normalizeInvalidSize() {
            // when
            SliceMeta meta = SliceMeta.of(0, true);

            // then
            assertThat(meta.size()).isEqualTo(SliceMeta.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("음수 count는 0으로 정규화된다")
        void normalizeNegativeCount() {
            // when
            SliceMeta meta = new SliceMeta(20, true, null, -5);

            // then
            assertThat(meta.count()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("상태 확인 메서드 테스트")
    class StateCheckTest {

        @Test
        @DisplayName("hasCursor - 커서가 있으면 true")
        void hasCursorWhenCursorPresent() {
            // given
            SliceMeta meta = SliceMeta.withCursor("abc", 20, true);

            // then
            assertThat(meta.hasCursor()).isTrue();
        }

        @Test
        @DisplayName("hasCursor - 커서가 빈 문자열이면 false")
        void hasCursorWhenCursorEmpty() {
            // given
            SliceMeta meta = SliceMeta.withCursor("", 20, true);

            // then
            assertThat(meta.hasCursor()).isFalse();
        }

        @Test
        @DisplayName("isLast - 다음 슬라이스가 없으면 true")
        void isLastWhenNoNext() {
            // given
            SliceMeta meta = SliceMeta.of(20, false);

            // then
            assertThat(meta.isLast()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - count가 0이면 true")
        void isEmptyWhenCountIsZero() {
            // given
            SliceMeta meta = SliceMeta.of(20, false, 0);

            // then
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - count가 있으면 false")
        void isEmptyWhenCountIsPositive() {
            // given
            SliceMeta meta = SliceMeta.of(20, true, 15);

            // then
            assertThat(meta.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("cursorAsLong() 메서드 테스트")
    class CursorAsLongTest {

        @Test
        @DisplayName("숫자 커서를 Long으로 변환한다")
        void convertNumericCursorToLong() {
            // given
            SliceMeta meta = SliceMeta.withCursor("12345", 20, true);

            // then
            assertThat(meta.cursorAsLong()).isEqualTo(12345L);
        }

        @Test
        @DisplayName("커서가 없으면 null을 반환한다")
        void returnNullWhenNoCursor() {
            // given
            SliceMeta meta = SliceMeta.of(20, false);

            // then
            assertThat(meta.cursorAsLong()).isNull();
        }

        @Test
        @DisplayName("숫자가 아닌 커서는 null을 반환한다")
        void returnNullWhenNonNumericCursor() {
            // given
            SliceMeta meta = SliceMeta.withCursor("abc-cursor", 20, true);

            // then
            assertThat(meta.cursorAsLong()).isNull();
        }
    }

    @Nested
    @DisplayName("next() 메서드 테스트")
    class NextTest {

        @Test
        @DisplayName("String 커서로 다음 SliceMeta를 생성한다")
        void nextWithStringCursor() {
            // given
            SliceMeta meta = SliceMeta.of(20, true);

            // when
            SliceMeta nextMeta = meta.next("next-cursor", true);

            // then
            assertThat(nextMeta.cursor()).isEqualTo("next-cursor");
            assertThat(nextMeta.size()).isEqualTo(20);
            assertThat(nextMeta.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Long 커서로 다음 SliceMeta를 생성한다")
        void nextWithLongCursor() {
            // given
            SliceMeta meta = SliceMeta.of(20, true);

            // when
            SliceMeta nextMeta = meta.next(99999L, false);

            // then
            assertThat(nextMeta.cursor()).isEqualTo("99999");
            assertThat(nextMeta.hasNext()).isFalse();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            // given
            SliceMeta meta1 = SliceMeta.withCursor("abc", 20, true, 15);
            SliceMeta meta2 = SliceMeta.withCursor("abc", 20, true, 15);

            // then
            assertThat(meta1).isEqualTo(meta2);
            assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode());
        }
    }
}
