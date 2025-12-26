package com.ryuqq.crawlinghub.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Business Metrics Recorder
 *
 * <p>UserAgent Pool 관련 비즈니스 메트릭 수집 전담 컴포넌트
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>useragent.consume.total: UserAgent 소비 횟수
 *   <li>useragent.consume.latency: UserAgent 소비 지연 시간
 *   <li>useragent.success.total: 성공 횟수
 *   <li>useragent.failure.total: 실패 횟수 (by HTTP status)
 *   <li>useragent.rate_limited.total: 429 응답 횟수
 *   <li>useragent.suspended.total: SUSPENDED 전환 횟수
 *   <li>useragent.recovered.total: 복구 횟수
 *   <li>useragent.circuit_breaker.total: Circuit Breaker 발동 횟수
 *   <li>useragent.session.issued.total: 세션 발급 횟수
 *   <li>useragent.session.issue.latency: 세션 발급 지연 시간
 *   <li>useragent.pool.available: 가용 UserAgent 수 (Gauge)
 *   <li>useragent.pool.available_rate: 가용률 (Gauge)
 * </ul>
 *
 * <h3>Prometheus 쿼리 예시</h3>
 *
 * <pre>{@code
 * # UserAgent 소비 성공률
 * sum(rate(useragent_success_total[5m]))
 * / sum(rate(useragent_consume_total[5m]))
 *
 * # 429 Rate Limit 비율
 * sum(rate(useragent_rate_limited_total[5m]))
 * / sum(rate(useragent_consume_total[5m]))
 *
 * # Circuit Breaker 발동 추이
 * sum(increase(useragent_circuit_breaker_total[1h]))
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentMetrics {

    private static final String METRIC_PREFIX = "useragent";

    private final MeterRegistry meterRegistry;
    private final AtomicInteger availableCount;
    private final AtomicInteger totalCount;

    public UserAgentMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.availableCount = new AtomicInteger(0);
        this.totalCount = new AtomicInteger(0);

        registerGauges();
    }

    private void registerGauges() {
        Gauge.builder(METRIC_PREFIX + ".pool.available", availableCount, AtomicInteger::get)
                .description("Number of available UserAgents in pool")
                .register(meterRegistry);

        Gauge.builder(METRIC_PREFIX + ".pool.total", totalCount, AtomicInteger::get)
                .description("Total number of UserAgents in pool")
                .register(meterRegistry);

        Gauge.builder(
                        METRIC_PREFIX + ".pool.available_rate",
                        this,
                        metrics -> {
                            int total = metrics.totalCount.get();
                            if (total == 0) {
                                return 0.0;
                            }
                            return (double) metrics.availableCount.get() / total * 100;
                        })
                .description("Percentage of available UserAgents in pool")
                .register(meterRegistry);
    }

    /**
     * Pool 통계 업데이트
     *
     * @param available 가용 UserAgent 수
     * @param total 전체 UserAgent 수
     */
    public void updatePoolStats(int available, int total) {
        this.availableCount.set(available);
        this.totalCount.set(total);
    }

    /**
     * UserAgent 소비 기록
     *
     * @param durationMs 소비 지연 시간 (밀리초)
     */
    public void recordConsume(long durationMs) {
        Counter.builder(METRIC_PREFIX + ".consume.total")
                .description("Total UserAgent consume attempts")
                .register(meterRegistry)
                .increment();

        Timer.builder(METRIC_PREFIX + ".consume.latency")
                .description("UserAgent consume latency")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * UserAgent 소비 성공 기록
     *
     * @param userAgentId UserAgent ID
     */
    public void recordConsumeSuccess(Long userAgentId) {
        Counter.builder(METRIC_PREFIX + ".consume.success.total")
                .description("Total successful UserAgent consume")
                .register(meterRegistry)
                .increment();
    }

    /** UserAgent 소비 실패 기록 (NoAvailableUserAgentException) */
    public void recordConsumeFailure() {
        Counter.builder(METRIC_PREFIX + ".consume.failure.total")
                .description("Total failed UserAgent consume (no available)")
                .register(meterRegistry)
                .increment();
    }

    /**
     * UserAgent 성공 기록 (크롤링 성공)
     *
     * @param userAgentId UserAgent ID
     */
    public void recordSuccess(Long userAgentId) {
        Counter.builder(METRIC_PREFIX + ".success.total")
                .description("Total successful crawl results with UserAgent")
                .register(meterRegistry)
                .increment();
    }

    /**
     * UserAgent 실패 기록 (크롤링 실패)
     *
     * @param userAgentId UserAgent ID
     * @param httpStatusCode HTTP 상태 코드
     */
    public void recordFailure(Long userAgentId, int httpStatusCode) {
        String statusGroup = getStatusGroup(httpStatusCode);

        Counter.builder(METRIC_PREFIX + ".failure.total")
                .description("Total failed crawl results with UserAgent")
                .tag("status", String.valueOf(httpStatusCode))
                .tag("status_group", statusGroup)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Rate Limited (429) 기록
     *
     * @param userAgentId UserAgent ID
     */
    public void recordRateLimited(Long userAgentId) {
        Counter.builder(METRIC_PREFIX + ".rate_limited.total")
                .description("Total 429 rate limited responses")
                .register(meterRegistry)
                .increment();
    }

    /**
     * UserAgent SUSPENDED 전환 기록
     *
     * @param userAgentId UserAgent ID
     * @param reason 사유 (rate_limited, health_score_low)
     */
    public void recordSuspended(Long userAgentId, String reason) {
        Counter.builder(METRIC_PREFIX + ".suspended.total")
                .description("Total UserAgents suspended")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * UserAgent 복구 기록
     *
     * @param recoveredCount 복구된 UserAgent 수
     */
    public void recordRecovered(int recoveredCount) {
        Counter.builder(METRIC_PREFIX + ".recovered.total")
                .description("Total UserAgents recovered from suspended state")
                .register(meterRegistry)
                .increment(recoveredCount);
    }

    /**
     * Circuit Breaker 발동 기록
     *
     * @param availableRate 발동 시점의 가용률
     */
    public void recordCircuitBreakerOpen(double availableRate) {
        Counter.builder(METRIC_PREFIX + ".circuit_breaker.total")
                .description("Total circuit breaker triggers")
                .register(meterRegistry)
                .increment();
    }

    /**
     * 세션 발급 기록
     *
     * @param userAgentId UserAgent ID
     * @param durationMs 발급 지연 시간 (밀리초)
     * @param success 성공 여부
     */
    public void recordSessionIssue(Long userAgentId, long durationMs, boolean success) {
        String status = success ? "success" : "failure";

        Counter.builder(METRIC_PREFIX + ".session.issued.total")
                .description("Total session issuance attempts")
                .tag("status", status)
                .register(meterRegistry)
                .increment();

        Timer.builder(METRIC_PREFIX + ".session.issue.latency")
                .description("Session issuance latency")
                .tag("status", status)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Callback을 사용한 시간 측정
     *
     * @param metricName 메트릭 이름 (prefix 제외)
     * @param operation 수행할 작업
     * @param <T> 반환 타입
     * @return 작업 결과
     */
    public <T> T recordTimed(String metricName, Supplier<T> operation) {
        long startTime = System.currentTimeMillis();
        try {
            return operation.get();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Timer.builder(METRIC_PREFIX + "." + metricName + ".latency")
                    .description(metricName + " operation latency")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Callback을 사용한 시간 측정 (void 버전)
     *
     * @param metricName 메트릭 이름 (prefix 제외)
     * @param operation 수행할 작업
     */
    public void recordTimed(String metricName, Runnable operation) {
        long startTime = System.currentTimeMillis();
        try {
            operation.run();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            Timer.builder(METRIC_PREFIX + "." + metricName + ".latency")
                    .description(metricName + " operation latency")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.MILLISECONDS);
        }
    }

    private String getStatusGroup(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "2xx";
        } else if (statusCode >= 300 && statusCode < 400) {
            return "3xx";
        } else if (statusCode >= 400 && statusCode < 500) {
            return "4xx";
        } else if (statusCode >= 500) {
            return "5xx";
        }
        return "unknown";
    }
}
