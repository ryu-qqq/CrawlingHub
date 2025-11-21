package com.ryuqq.crawlinghub.domain.seller.vo;

public record SellerQueryCriteria(
        MustItSellerName mustItSellerName,
        SellerName sellerName,
        SellerStatus status,
        Integer page,
        Integer size) {}
