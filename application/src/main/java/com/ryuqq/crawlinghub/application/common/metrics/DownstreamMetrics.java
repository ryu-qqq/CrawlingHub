package com.ryuqq.crawlinghub.application.common.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Downstream Latency Metrics Recorder
 *
 * <p>외부 의존성(Redis, DB, SQS, HTTP API) 호출 latency를 측정하여 병목 감지
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>downstream.redis.latency: Redis 작업 latency
 *   <li>downstream.db.latency: Database 쿼리 latency
 *   <li>downstream.sqs.publish.latency: SQS 메시지 발행 latency
 *   <li>downstream.external.api.latency: 외부 API 호출 latency
 * </ul>
 *
 * <h3>Prometheus 쿼리 예시</h3>
 *
 * <pre>{@code
 * # Redis 평균 latency
 * rate(downstream_redis_latency_seconds_sum[5m])
 * / rate(downstream_redis_latency_seconds_count[5m])
 *
 * # DB 쿼리 latency by table
 * sum by (table) (rate(downstream_db_latency_seconds_sum[5m]))
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class DownstreamMetrics {

    private static final String METRIC_PREFIX = "downstream";

    private final MeterRegistry meterRegistry;

    public DownstreamMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Redis 작업 latency 기록
     *
     * @param operation 작업 유형 (get, set, delete, lock 등)
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordRedisLatency(String operation, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".redis.latency")
                .description("Redis operation latency")
                .tag("operation", operation)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Database 쿼리 latency 기록
     *
     * @param operation 작업 유형 (select, insert, update, delete)
     * @param table 테이블명
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordDbLatency(String operation, String table, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".db.latency")
                .description("Database query latency")
                .tag("operation", operation)
                .tag("table", table)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * SQS 메시지 발행 latency 기록
     *
     * @param queue 큐 이름
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordSqsPublishLatency(String queue, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".sqs.publish.latency")
                .description("SQS message publish latency")
                .tag("queue", queue)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * SQS 메시지 소비 latency 기록
     *
     * @param queue 큐 이름
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordSqsConsumeLatency(String queue, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".sqs.consume.latency")
                .description("SQS message consume latency")
                .tag("queue", queue)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 외부 API 호출 latency 기록
     *
     * @param service 서비스명 (예: product-hub, seller-service)
     * @param endpoint 엔드포인트 (예: /api/v1/products)
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordExternalApiLatency(String service, String endpoint, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".external.api.latency")
                .description("External API call latency")
                .tag("service", service)
                .tag("endpoint", endpoint)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 외부 API 호출 latency 기록 (HTTP 상태 코드 포함)
     *
     * @param service 서비스명
     * @param endpoint 엔드포인트
     * @param statusCode HTTP 상태 코드
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordExternalApiLatency(String service, String endpoint, int statusCode, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".external.api.latency")
                .description("External API call latency")
                .tag("service", service)
                .tag("endpoint", endpoint)
                .tag("status", String.valueOf(statusCode))
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * HTTP 클라이언트 요청 latency 기록 (크롤링용)
     *
     * @param targetSite 대상 사이트명
     * @param operation 작업 유형 (fetch, parse 등)
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordCrawlHttpLatency(String targetSite, String operation, long durationMs) {
        Timer.builder(METRIC_PREFIX + ".crawl.http.latency")
                .description("Crawl HTTP request latency")
                .tag("site", targetSite)
                .tag("operation", operation)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
