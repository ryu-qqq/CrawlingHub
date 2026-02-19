package com.ryuqq.crawlinghub.adapter.in.rest.execution;

public final class CrawlExecutionEndpoints {

    private CrawlExecutionEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/executions";
    public static final String BY_ID = "/{id}";
}
