package com.ryuqq.crawlinghub.adapter.in.rest.task;

public final class CrawlTaskOutboxEndpoints {

    private CrawlTaskOutboxEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/outbox";
    public static final String REPUBLISH = "/{crawlTaskId}/republish";
}
