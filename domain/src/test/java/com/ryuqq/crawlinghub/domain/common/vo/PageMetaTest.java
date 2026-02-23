package com.ryuqq.crawlinghub.domain.common.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("PageMeta Value Object 단위 테스트")
class PageMetaTest {

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("3개 파라미터로 totalPages를 자동 계산하여 생성한다")
        void createWithAutoCalculatedTotalPages() {
            // when
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.page()).isEqualTo(0);
            assertThat(meta.size()).isEqualTo(20);
            assertThat(meta.totalElements()).isEqualTo(150);
            assertThat(meta.totalPages()).isEqualTo(8); // ceil(150/20) = 8
        }

        @Test
        @DisplayName("4개 파라미터로 직접 지정하여 생성한다")
        void createWithExplicitValues() {
            // when
            PageMeta meta = PageMeta.of(2, 20, 150, 8);

            // then
            assertThat(meta.page()).isEqualTo(2);
            assertThat(meta.size()).isEqualTo(20);
            assertThat(meta.totalElements()).isEqualTo(150);
            assertThat(meta.totalPages()).isEqualTo(8);
        }

        @Test
        @DisplayName("empty()로 빈 PageMeta를 생성한다")
        void createEmpty() {
            // when
            PageMeta meta = PageMeta.empty();

            // then
            assertThat(meta.page()).isEqualTo(0);
            assertThat(meta.size()).isEqualTo(PageMeta.DEFAULT_SIZE);
            assertThat(meta.totalElements()).isEqualTo(0);
            assertThat(meta.totalPages()).isEqualTo(0);
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("크기를 지정하여 빈 PageMeta를 생성한다")
        void createEmptyWithSize() {
            // when
            PageMeta meta = PageMeta.empty(50);

            // then
            assertThat(meta.size()).isEqualTo(50);
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("음수 페이지는 0으로 정규화된다")
        void normalizeNegativePage() {
            // when
            PageMeta meta = PageMeta.of(-1, 20, 100);

            // then
            assertThat(meta.page()).isEqualTo(0);
        }

        @Test
        @DisplayName("0 이하 size는 DEFAULT_SIZE로 정규화된다")
        void normalizeInvalidSize() {
            // when
            PageMeta meta = PageMeta.of(0, 0, 100);

            // then
            assertThat(meta.size()).isEqualTo(PageMeta.DEFAULT_SIZE);
        }

        @Test
        @DisplayName("음수 totalElements는 0으로 정규화된다")
        void normalizeNegativeTotalElements() {
            // when
            PageMeta meta = new PageMeta(0, 20, -1, 0);

            // then
            assertThat(meta.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("페이지 탐색 메서드 테스트")
    class NavigationTest {

        @Test
        @DisplayName("hasNext - 다음 페이지가 있으면 true")
        void hasNextWhenNotLastPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150); // 8 pages total

            // then
            assertThat(meta.hasNext()).isTrue();
        }

        @Test
        @DisplayName("hasNext - 마지막 페이지이면 false")
        void hasNextWhenLastPage() {
            // given
            PageMeta meta = PageMeta.of(7, 20, 150); // last page

            // then
            assertThat(meta.hasNext()).isFalse();
        }

        @Test
        @DisplayName("hasPrevious - 이전 페이지가 있으면 true")
        void hasPreviousWhenNotFirstPage() {
            // given
            PageMeta meta = PageMeta.of(1, 20, 150);

            // then
            assertThat(meta.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("hasPrevious - 첫 페이지이면 false")
        void hasPreviousWhenFirstPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("isFirst - 첫 페이지이면 true")
        void isFirstWhenFirstPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.isFirst()).isTrue();
        }

        @Test
        @DisplayName("isLast - 마지막 페이지이면 true")
        void isLastWhenLastPage() {
            // given
            PageMeta meta = PageMeta.of(7, 20, 150); // last page

            // then
            assertThat(meta.isLast()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 결과가 없으면 true")
        void isEmptyWhenNoResults() {
            // given
            PageMeta meta = PageMeta.empty();

            // then
            assertThat(meta.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty - 결과가 있으면 false")
        void isEmptyWhenHasResults() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 10);

            // then
            assertThat(meta.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("요소 범위 메서드 테스트")
    class ElementRangeTest {

        @Test
        @DisplayName("startElement - 첫 페이지의 시작 번호는 1이다")
        void startElementOnFirstPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.startElement()).isEqualTo(1);
        }

        @Test
        @DisplayName("startElement - 두 번째 페이지의 시작 번호는 21이다")
        void startElementOnSecondPage() {
            // given
            PageMeta meta = PageMeta.of(1, 20, 150);

            // then
            assertThat(meta.startElement()).isEqualTo(21);
        }

        @Test
        @DisplayName("startElement - 빈 결과이면 0이다")
        void startElementWhenEmpty() {
            // given
            PageMeta meta = PageMeta.empty();

            // then
            assertThat(meta.startElement()).isEqualTo(0);
        }

        @Test
        @DisplayName("endElement - 첫 페이지의 끝 번호는 20이다")
        void endElementOnFirstPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.endElement()).isEqualTo(20);
        }

        @Test
        @DisplayName("endElement - 마지막 페이지는 전체 요소 수로 제한된다")
        void endElementOnLastPage() {
            // given
            PageMeta meta = PageMeta.of(7, 20, 150); // 141-150번째

            // then
            assertThat(meta.endElement()).isEqualTo(150);
        }

        @Test
        @DisplayName("offset - 첫 페이지의 offset은 0이다")
        void offsetOnFirstPage() {
            // given
            PageMeta meta = PageMeta.of(0, 20, 150);

            // then
            assertThat(meta.offset()).isEqualTo(0);
        }

        @Test
        @DisplayName("offset - 두 번째 페이지의 offset은 20이다")
        void offsetOnSecondPage() {
            // given
            PageMeta meta = PageMeta.of(1, 20, 150);

            // then
            assertThat(meta.offset()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값이면 동일하다")
        void sameValuesAreEqual() {
            // given
            PageMeta meta1 = PageMeta.of(0, 20, 150, 8);
            PageMeta meta2 = PageMeta.of(0, 20, 150, 8);

            // then
            assertThat(meta1).isEqualTo(meta2);
            assertThat(meta1.hashCode()).isEqualTo(meta2.hashCode());
        }
    }
}
