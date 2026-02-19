package com.ryuqq.crawlinghub.adapter.in.rest.useragent;

public final class UserAgentEndpoints {

    private UserAgentEndpoints() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String BASE = "/api/v1/crawling/user-agents";
    public static final String BY_ID = "/{userAgentId}";
    public static final String POOL_STATUS = "/pool-status";
    public static final String RECOVER = "/recover";
    public static final String STATUS = "/status";
    public static final String WARMUP = "/warmup";
}
