package com.ryuqq.crawlinghub.application.product.builder;

/**
 * 미니샵 데이터 DTO
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public record MiniShopData(
    String productName,
    Long price,
    String mainImageUrl
) {}

