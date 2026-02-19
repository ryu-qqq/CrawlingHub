package com.ryuqq.crawlinghub.adapter.in.rest.webhook;

public final class WebhookEndpoints {

    private WebhookEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/webhook";
    public static final String IMAGE_UPLOAD = "/image-upload";
}
