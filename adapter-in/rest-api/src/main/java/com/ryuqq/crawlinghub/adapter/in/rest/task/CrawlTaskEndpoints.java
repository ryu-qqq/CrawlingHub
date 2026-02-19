package com.ryuqq.crawlinghub.adapter.in.rest.task;

public final class CrawlTaskEndpoints {

    private CrawlTaskEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/tasks";
    public static final String BY_ID = "/{id}";
    public static final String RETRY = "/{id}/retry";
}
