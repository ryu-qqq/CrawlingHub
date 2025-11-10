package com.ryuqq.crawlinghub.application.product.builder;

import com.ryuqq.crawlinghub.domain.product.ProductOption;

import java.util.List;

/**
 * 옵션 데이터 DTO
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record OptionData(
    List<ProductOption> options,
    Integer totalStock
) {}

