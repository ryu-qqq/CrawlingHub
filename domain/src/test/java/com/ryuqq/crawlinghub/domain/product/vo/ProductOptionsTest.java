package com.ryuqq.crawlinghub.domain.product.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@Tag("domain")
@Tag("vo")
@DisplayName("ProductOptions 일급 컬렉션 단위 테스트")
class ProductOptionsTest {

    private ProductOption inStockOption(
            long optionNo, long itemNo, String color, String size, int stock) {
        return ProductOption.of(optionNo, itemNo, color, size, stock, null);
    }

    private ProductOption soldOutOption(long optionNo, long itemNo) {
        return ProductOption.of(optionNo, itemNo, "블랙", "L", 0, null);
    }

    @Nested
    @DisplayName("생성 테스트")
    class CreationTest {

        @Test
        @DisplayName("빈 컬렉션을 생성한다")
        void createEmpty() {
            ProductOptions options = ProductOptions.empty();
            assertThat(options.isEmpty()).isTrue();
            assertThat(options.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("옵션 목록으로 생성한다")
        void createWithOptions() {
            List<ProductOption> optionList =
                    List.of(
                            inStockOption(1L, 100L, "블랙", "M", 5),
                            inStockOption(2L, 100L, "화이트", "L", 3));

            ProductOptions options = ProductOptions.of(optionList);

            assertThat(options.size()).isEqualTo(2);
            assertThat(options.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("from은 of와 동일하다")
        void fromIsAliasForOf() {
            List<ProductOption> optionList = List.of(inStockOption(1L, 100L, "블랙", "M", 5));
            ProductOptions options = ProductOptions.from(optionList);
            assertThat(options.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("옵션 필터링 테스트")
    class FilteringTest {

        @Test
        @DisplayName("재고가 있는 옵션만 반환한다")
        void returnsInStockOptions() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    soldOutOption(2L, 100L)));

            assertThat(options.getInStockOptions()).hasSize(1);
        }

        @Test
        @DisplayName("품절 옵션만 반환한다")
        void returnsSoldOutOptions() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    soldOutOption(2L, 100L)));

            assertThat(options.getSoldOutOptions()).hasSize(1);
        }

        @Test
        @DisplayName("특정 색상의 옵션만 반환한다")
        void returnsByColor() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "M", 3)));

            assertThat(options.getByColor("블랙")).hasSize(1);
            assertThat(options.getByColor("화이트")).hasSize(1);
            assertThat(options.getByColor("레드")).isEmpty();
        }

        @Test
        @DisplayName("색상이 null이면 빈 목록을 반환한다")
        void returnsEmptyWhenColorIsNull() {
            ProductOptions options =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            assertThat(options.getByColor(null)).isEmpty();
        }

        @Test
        @DisplayName("특정 사이즈의 옵션만 반환한다")
        void returnsBySize() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "블랙", "L", 3)));

            assertThat(options.getBySize("M")).hasSize(1);
            assertThat(options.getBySize("XL")).isEmpty();
        }
    }

    @Nested
    @DisplayName("재고 관련 테스트")
    class StockTest {

        @Test
        @DisplayName("총 재고 수량을 반환한다")
        void returnsTotalStock() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            assertThat(options.getTotalStock()).isEqualTo(8);
        }

        @Test
        @DisplayName("모든 옵션이 품절이면 isAllSoldOut()이 true를 반환한다")
        void allSoldOutReturnsTrueWhenAllSoldOut() {
            ProductOptions options =
                    ProductOptions.of(List.of(soldOutOption(1L, 100L), soldOutOption(2L, 100L)));

            assertThat(options.isAllSoldOut()).isTrue();
        }

        @Test
        @DisplayName("재고가 있는 옵션이 있으면 isAllSoldOut()이 false를 반환한다")
        void allSoldOutReturnsFalseWhenSomeInStock() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    soldOutOption(2L, 100L)));

            assertThat(options.isAllSoldOut()).isFalse();
        }

        @Test
        @DisplayName("빈 컬렉션은 isAllSoldOut()이 false를 반환한다")
        void emptyOptionsNotAllSoldOut() {
            ProductOptions options = ProductOptions.empty();
            assertThat(options.isAllSoldOut()).isFalse();
        }
    }

    @Nested
    @DisplayName("옵션 검색 테스트")
    class SearchTest {

        @Test
        @DisplayName("옵션 번호로 옵션을 찾는다")
        void findsByOptionNo() {
            ProductOption target = inStockOption(42L, 100L, "블랙", "M", 5);
            ProductOptions options = ProductOptions.of(List.of(target));

            assertThat(options.findByOptionNo(42L)).isEqualTo(target);
            assertThat(options.findByOptionNo(99L)).isNull();
        }
    }

    @Nested
    @DisplayName("고유 색상/사이즈 조회 테스트")
    class DistinctValuesTest {

        @Test
        @DisplayName("고유 색상 목록을 반환한다")
        void returnsDistinctColors() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "블랙", "L", 3),
                                    inStockOption(3L, 100L, "화이트", "M", 2)));

            List<String> colors = options.getDistinctColors();

            assertThat(colors).hasSize(2);
            assertThat(colors).containsExactlyInAnyOrder("블랙", "화이트");
        }

        @Test
        @DisplayName("고유 사이즈 목록을 반환한다")
        void returnsDistinctSizes() {
            ProductOptions options =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "M", 3),
                                    inStockOption(3L, 100L, "블랙", "L", 2)));

            List<String> sizes = options.getDistinctSizes();

            assertThat(sizes).hasSize(2);
            assertThat(sizes).containsExactlyInAnyOrder("M", "L");
        }
    }

    @Nested
    @DisplayName("hasChanges() 테스트")
    class HasChangesTest {

        @Test
        @DisplayName("같은 옵션이면 변경이 없다")
        void sameOptionsHaveNoChanges() {
            ProductOptions options1 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions options2 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            assertThat(options1.hasChanges(options2)).isFalse();
        }

        @Test
        @DisplayName("재고가 다르면 변경이 있다")
        void differentStockHasChanges() {
            ProductOptions options1 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions options2 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 3)));

            assertThat(options1.hasChanges(options2)).isTrue();
        }

        @Test
        @DisplayName("옵션 수가 다르면 변경이 있다")
        void differentSizeHasChanges() {
            ProductOptions options1 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions options2 =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            assertThat(options1.hasChanges(options2)).isTrue();
        }

        @Test
        @DisplayName("null이면 비어있지 않은 경우 변경이 있다")
        void nullHasChangesWhenNotEmpty() {
            ProductOptions options =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            assertThat(options.hasChanges(null)).isTrue();
        }
    }

    @Nested
    @DisplayName("getAll() 테스트")
    class GetAllTest {

        @Test
        @DisplayName("모든 옵션을 불변 목록으로 반환한다")
        void returnsAllOptionsAsUnmodifiableList() {
            List<ProductOption> optionList =
                    List.of(inStockOption(1L, 100L, "블랙", "M", 5), soldOutOption(2L, 100L));
            ProductOptions options = ProductOptions.of(optionList);

            List<ProductOption> all = options.getAll();

            assertThat(all).hasSize(2);
            assertThatThrownBy(() -> all.add(inStockOption(3L, 100L, "레드", "S", 1)))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("getBySize() null/blank 테스트")
    class GetBySizeNullTest {

        @Test
        @DisplayName("사이즈가 null이면 빈 목록을 반환한다")
        void returnsEmptyWhenSizeIsNull() {
            ProductOptions options =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            assertThat(options.getBySize(null)).isEmpty();
        }

        @Test
        @DisplayName("사이즈가 공백이면 빈 목록을 반환한다")
        void returnsEmptyWhenSizeIsBlank() {
            ProductOptions options =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            assertThat(options.getBySize("   ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getStockChangedOptions() 테스트")
    class GetStockChangedOptionsTest {

        @Test
        @DisplayName("이전 옵션이 null이면 모든 현재 옵션을 반환한다")
        void returnsAllWhenPreviousIsNull() {
            ProductOptions current =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            List<ProductOption> changed = current.getStockChangedOptions(null);

            assertThat(changed).hasSize(2);
        }

        @Test
        @DisplayName("이전 옵션이 비어있으면 모든 현재 옵션을 반환한다")
        void returnsAllWhenPreviousIsEmpty() {
            ProductOptions current =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            List<ProductOption> changed = current.getStockChangedOptions(ProductOptions.empty());

            assertThat(changed).hasSize(1);
        }

        @Test
        @DisplayName("재고가 변경된 옵션만 반환한다")
        void returnsStockChangedOptions() {
            ProductOptions previous =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions current =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 3)));

            List<ProductOption> changed = current.getStockChangedOptions(previous);

            assertThat(changed).hasSize(1);
            assertThat(changed.get(0).stock()).isEqualTo(3);
        }

        @Test
        @DisplayName("재고 변경이 없으면 빈 목록을 반환한다")
        void returnsEmptyWhenNoStockChange() {
            ProductOptions previous =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions current =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            List<ProductOption> changed = current.getStockChangedOptions(previous);

            assertThat(changed).isEmpty();
        }

        @Test
        @DisplayName("새로 추가된 옵션은 변경된 옵션으로 반환한다")
        void returnsNewOptionAsChanged() {
            ProductOptions previous =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions current =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            List<ProductOption> changed = current.getStockChangedOptions(previous);

            assertThat(changed).hasSize(1);
            assertThat(changed.get(0).optionNo()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("getNewOptions() 테스트")
    class GetNewOptionsTest {

        @Test
        @DisplayName("이전 옵션이 null이면 모든 현재 옵션을 반환한다")
        void returnsAllWhenPreviousIsNull() {
            ProductOptions current =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            List<ProductOption> newOptions = current.getNewOptions(null);

            assertThat(newOptions).hasSize(1);
        }

        @Test
        @DisplayName("이전 옵션이 비어있으면 모든 현재 옵션을 반환한다")
        void returnsAllWhenPreviousIsEmpty() {
            ProductOptions current =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));

            List<ProductOption> newOptions = current.getNewOptions(ProductOptions.empty());

            assertThat(newOptions).hasSize(1);
        }

        @Test
        @DisplayName("이전에 없던 옵션만 반환한다")
        void returnsOnlyNewOptions() {
            ProductOptions previous =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions current =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            List<ProductOption> newOptions = current.getNewOptions(previous);

            assertThat(newOptions).hasSize(1);
            assertThat(newOptions.get(0).optionNo()).isEqualTo(2L);
        }

        @Test
        @DisplayName("모두 기존 옵션이면 빈 목록을 반환한다")
        void returnsEmptyWhenNoNewOptions() {
            ProductOptions previous =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));
            ProductOptions current =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));

            List<ProductOption> newOptions = current.getNewOptions(previous);

            assertThat(newOptions).isEmpty();
        }
    }

    @Nested
    @DisplayName("hasChanges() 빈 컬렉션 테스트")
    class HasChangesEmptyTest {

        @Test
        @DisplayName("빈 컬렉션끼리는 변경이 없다")
        void emptyOptionsHaveNoChanges() {
            ProductOptions empty1 = ProductOptions.empty();
            ProductOptions empty2 = ProductOptions.empty();
            assertThat(empty1.hasChanges(empty2)).isFalse();
        }

        @Test
        @DisplayName("빈 컬렉션에 null 비교는 변경이 없다")
        void emptyOptionsHaveNoChangesWithNull() {
            ProductOptions empty = ProductOptions.empty();
            assertThat(empty.hasChanges(null)).isFalse();
        }

        @Test
        @DisplayName("같은 옵션 번호지만 null인 otherMap에서 변경으로 감지한다")
        void detectsNewOptionAsChange() {
            ProductOptions current =
                    ProductOptions.of(
                            List.of(
                                    inStockOption(1L, 100L, "블랙", "M", 5),
                                    inStockOption(2L, 100L, "화이트", "L", 3)));
            ProductOptions previous =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            assertThat(current.hasChanges(previous)).isTrue();
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 옵션 목록이면 동일하다")
        void sameOptionsAreEqual() {
            List<ProductOption> optionList = List.of(inStockOption(1L, 100L, "블랙", "M", 5));
            ProductOptions options1 = ProductOptions.of(optionList);
            ProductOptions options2 = ProductOptions.of(optionList);

            assertThat(options1).isEqualTo(options2);
            assertThat(options1.hashCode()).isEqualTo(options2.hashCode());
        }

        @Test
        @DisplayName("다른 옵션 목록이면 다르다")
        void differentOptionsAreNotEqual() {
            ProductOptions options1 =
                    ProductOptions.of(List.of(inStockOption(1L, 100L, "블랙", "M", 5)));
            ProductOptions options2 =
                    ProductOptions.of(List.of(inStockOption(2L, 100L, "화이트", "L", 3)));

            assertThat(options1).isNotEqualTo(options2);
        }
    }
}
