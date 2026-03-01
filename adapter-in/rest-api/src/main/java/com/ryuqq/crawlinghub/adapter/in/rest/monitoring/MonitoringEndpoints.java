package com.ryuqq.crawlinghub.adapter.in.rest.monitoring;

public final class MonitoringEndpoints {

    private MonitoringEndpoints() {}

    private static final String BASE = "/api/v1/monitoring";
    public static final String DASHBOARD = BASE + "/dashboard";
    public static final String CRAWL_TASKS_SUMMARY = BASE + "/crawl-tasks/summary";
    public static final String OUTBOX_SUMMARY = BASE + "/outbox/summary";
    public static final String CRAWLED_RAW_SUMMARY = BASE + "/crawled-raw/summary";
    public static final String EXTERNAL_SYSTEMS_HEALTH = BASE + "/external-systems/health";
    public static final String PRODUCT_SYNC_FAILURES = BASE + "/product-sync/failures";
    public static final String CRAWL_EXECUTIONS_SUMMARY = BASE + "/crawl-executions/summary";
}
