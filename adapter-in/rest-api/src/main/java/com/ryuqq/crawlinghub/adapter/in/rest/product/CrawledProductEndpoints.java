package com.ryuqq.crawlinghub.adapter.in.rest.product;

public final class CrawledProductEndpoints {

    private CrawledProductEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/crawled-products";
    public static final String BY_ID = "/{id}";
    public static final String SYNC = "/{id}/sync";
}
