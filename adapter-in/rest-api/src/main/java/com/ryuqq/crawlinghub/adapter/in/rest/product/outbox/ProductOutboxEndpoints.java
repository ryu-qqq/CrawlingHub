package com.ryuqq.crawlinghub.adapter.in.rest.product.outbox;

public final class ProductOutboxEndpoints {

    private ProductOutboxEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/product-outbox";
    public static final String SYNC = "/sync";
    public static final String IMAGE = "/image";
    public static final String SYNC_RETRY = "/sync/{id}/retry";
    public static final String IMAGE_RETRY = "/image/{id}/retry";
}
