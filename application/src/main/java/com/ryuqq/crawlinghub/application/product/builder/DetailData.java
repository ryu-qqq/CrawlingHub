package com.ryuqq.crawlinghub.application.product.builder;

import com.ryuqq.crawlinghub.domain.product.ProductDetailInfoModule;
import com.ryuqq.crawlinghub.domain.product.ProductInfoModule;
import com.ryuqq.crawlinghub.domain.product.ShippingModule;

/**
 * 상세 데이터 DTO
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record DetailData(
    ProductInfoModule productInfo,
    ShippingModule shipping,
    ProductDetailInfoModule detailInfo
) {}

