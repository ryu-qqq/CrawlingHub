package com.ryuqq.crawlinghub.domain.product.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 상품 옵션 일급 컬렉션
 *
 * <p>상품의 모든 옵션을 관리하고 재고, 변경 감지 등의 기능을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public class ProductOptions {

    private final List<ProductOption> options;

    private ProductOptions(List<ProductOption> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    /** 빈 컬렉션 생성 */
    public static ProductOptions empty() {
        return new ProductOptions(Collections.emptyList());
    }

    /** 옵션 목록으로 생성 */
    public static ProductOptions of(List<ProductOption> options) {
        return new ProductOptions(options);
    }

    /** 옵션 목록으로 생성 (of 별칭) */
    public static ProductOptions from(List<ProductOption> options) {
        return of(options);
    }

    /** 모든 옵션 반환 (불변) */
    public List<ProductOption> getAll() {
        return Collections.unmodifiableList(options);
    }

    /** 재고가 있는 옵션만 반환 */
    public List<ProductOption> getInStockOptions() {
        return options.stream().filter(ProductOption::isInStock).collect(Collectors.toList());
    }

    /** 품절 옵션만 반환 */
    public List<ProductOption> getSoldOutOptions() {
        return options.stream().filter(ProductOption::isSoldOut).collect(Collectors.toList());
    }

    /** 특정 색상의 옵션만 반환 */
    public List<ProductOption> getByColor(String color) {
        if (color == null || color.isBlank()) {
            return Collections.emptyList();
        }
        return options.stream()
                .filter(opt -> color.equals(opt.color()))
                .collect(Collectors.toList());
    }

    /** 특정 사이즈의 옵션만 반환 */
    public List<ProductOption> getBySize(String size) {
        if (size == null || size.isBlank()) {
            return Collections.emptyList();
        }
        return options.stream().filter(opt -> size.equals(opt.size())).collect(Collectors.toList());
    }

    /** 옵션 번호로 찾기 */
    public ProductOption findByOptionNo(long optionNo) {
        return options.stream().filter(opt -> opt.optionNo() == optionNo).findFirst().orElse(null);
    }

    /** 총 재고 수량 */
    public int getTotalStock() {
        return options.stream().mapToInt(ProductOption::stock).sum();
    }

    /** 옵션 개수 */
    public int size() {
        return options.size();
    }

    /** 비어있는지 확인 */
    public boolean isEmpty() {
        return options.isEmpty();
    }

    /** 모든 옵션이 품절인지 확인 */
    public boolean isAllSoldOut() {
        return !options.isEmpty() && options.stream().allMatch(ProductOption::isSoldOut);
    }

    /** 고유 색상 목록 반환 */
    public List<String> getDistinctColors() {
        return options.stream()
                .map(ProductOption::color)
                .filter(color -> color != null && !color.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /** 고유 사이즈 목록 반환 */
    public List<String> getDistinctSizes() {
        return options.stream()
                .map(ProductOption::size)
                .filter(size -> size != null && !size.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 옵션 변경 여부 확인
     *
     * @param other 비교 대상
     * @return 변경이 있으면 true
     */
    public boolean hasChanges(ProductOptions other) {
        if (other == null) {
            return !this.isEmpty();
        }

        if (this.size() != other.size()) {
            return true;
        }

        Map<Long, ProductOption> otherMap =
                other.options.stream()
                        .collect(Collectors.toMap(ProductOption::optionNo, Function.identity()));

        for (ProductOption option : this.options) {
            ProductOption otherOption = otherMap.get(option.optionNo());
            if (otherOption == null || option.hasStockChange(otherOption)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 재고가 변경된 옵션 목록 반환
     *
     * @param previous 이전 옵션 목록
     * @return 재고가 변경된 옵션 목록
     */
    public List<ProductOption> getStockChangedOptions(ProductOptions previous) {
        if (previous == null || previous.isEmpty()) {
            return new ArrayList<>(this.options);
        }

        Map<Long, ProductOption> prevMap =
                previous.options.stream()
                        .collect(Collectors.toMap(ProductOption::optionNo, Function.identity()));

        return this.options.stream()
                .filter(
                        opt -> {
                            ProductOption prevOpt = prevMap.get(opt.optionNo());
                            return prevOpt == null || opt.hasStockChange(prevOpt);
                        })
                .collect(Collectors.toList());
    }

    /**
     * 새로 추가된 옵션 반환
     *
     * @param previous 이전 옵션 목록
     * @return 새로 추가된 옵션 목록
     */
    public List<ProductOption> getNewOptions(ProductOptions previous) {
        if (previous == null || previous.isEmpty()) {
            return new ArrayList<>(this.options);
        }

        List<Long> previousOptionNos =
                previous.options.stream().map(ProductOption::optionNo).collect(Collectors.toList());

        return this.options.stream()
                .filter(opt -> !previousOptionNos.contains(opt.optionNo()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductOptions that = (ProductOptions) o;
        return Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options);
    }
}
