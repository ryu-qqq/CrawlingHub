package com.ryuqq.crawlinghub.adapter.out.marketplace.dto.response;

public record CreateProductResponse(
        Long inboundProductId, Long internalProductGroupId, String status, String action) {}
