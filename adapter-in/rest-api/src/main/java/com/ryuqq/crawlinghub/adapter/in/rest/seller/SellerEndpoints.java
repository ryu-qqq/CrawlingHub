package com.ryuqq.crawlinghub.adapter.in.rest.seller;

public final class SellerEndpoints {

    private SellerEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/sellers";
    public static final String BY_ID = "/{id}";
}
