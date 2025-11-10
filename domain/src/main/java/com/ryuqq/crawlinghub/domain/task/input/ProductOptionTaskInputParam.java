package com.ryuqq.crawlinghub.domain.task.input;

import java.util.Objects;

/**
 * PRODUCT_OPTION Task Input Parameter (타입 안전)
 *
 * <p>상품 옵션 크롤링용 파라미터
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public class ProductOptionTaskInputParam extends TaskInputParam {

    private final Long itemNo;

    private ProductOptionTaskInputParam(Long itemNo) {
        if (itemNo == null || itemNo <= 0) {
            throw new IllegalArgumentException("itemNo는 필수이며 양수여야 합니다");
        }
        this.itemNo = itemNo;
    }

    public static ProductOptionTaskInputParam of(Long itemNo) {
        return new ProductOptionTaskInputParam(itemNo);
    }

    public Long getItemNo() {
        return itemNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductOptionTaskInputParam that)) return false;
        return Objects.equals(itemNo, that.itemNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemNo);
    }

    @Override
    public String toString() {
        return "ProductOptionTaskInputParam{" +
               "itemNo=" + itemNo +
               '}';
    }
}
