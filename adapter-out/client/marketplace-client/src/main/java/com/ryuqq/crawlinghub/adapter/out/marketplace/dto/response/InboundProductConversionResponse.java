package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response;

/**
 * 인바운드 상품 변환 결과 응답 DTO
 *
 * <p>MarketPlace POST /api/v1/market/inbound/products 응답에 대응합니다.
 */
public record InboundProductConversionResponse(
        Long inboundProductId, Long internalProductGroupId, String status, String action) {}
