package com.ryuqq.crawlinghub.domain.product;

import java.util.List;

/**
 * 외부 동기화용 전체 상품 데이터 DTO
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record FullProductData(
    Long mustItItemNo,
    String productName,
    Long price,
    String mainImageUrl,
    List<ProductOption> options,
    Integer totalStock,
    ProductInfoModule productInfo,
    ShippingModule shipping,
    ProductDetailInfoModule detailInfo
) {}

