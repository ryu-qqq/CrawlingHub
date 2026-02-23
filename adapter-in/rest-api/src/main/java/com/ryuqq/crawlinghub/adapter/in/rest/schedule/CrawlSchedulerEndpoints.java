package com.ryuqq.crawlinghub.adapter.in.rest.schedule;

public final class CrawlSchedulerEndpoints {

    private CrawlSchedulerEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/schedules";
    public static final String BY_ID = "/{id}";
    public static final String TRIGGER = "/{id}/trigger";
}
