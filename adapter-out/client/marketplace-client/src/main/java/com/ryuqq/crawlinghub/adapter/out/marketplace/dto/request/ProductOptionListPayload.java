package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.request;

import java.util.List;

/**
 * 상품 옵션 목록 래퍼
 *
 * <p>옵션/재고 등록/수정 API에서 독립적으로 사용 가능한 래퍼 객체. 받는 쪽에서 옵션 목록 단위로 밸리데이션하기 용이합니다.
 *
 * @param options 옵션 목록
 * @param totalStock 총 재고 수량
 * @author development-team
 * @since 1.0.0
 */
public record ProductOptionListPayload(List<ProductOptionPayload> options, int totalStock) {

    public ProductOptionListPayload(List<ProductOptionPayload> options, int totalStock) {
        this.options = options == null ? List.of() : List.copyOf(options);
        this.totalStock = totalStock;
    }
}
