package com.ryuqq.crawlinghub.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Crawl Task Business Metrics Recorder
 *
 * <p>크롤링 태스크 비즈니스 메트릭 수집 전담 컴포넌트
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>crawl.task.received.total: 수신된 크롤링 태스크 수
 *   <li>crawl.task.completed.total: 완료된 크롤링 태스크 수
 *   <li>crawl.task.failed.total: 실패한 크롤링 태스크 수
 *   <li>crawl.task.duration: 크롤링 태스크 처리 시간
 *   <li>crawl.task.items.extracted: 추출된 아이템 수
 *   <li>crawl.task.in.progress: 현재 처리 중인 태스크 수 (Gauge)
 * </ul>
 *
 * <h3>Prometheus 쿼리 예시</h3>
 *
 * <pre>{@code
 * # 크롤링 성공률
 * sum(rate(crawl_task_completed_total[5m])) by (site)
 * / sum(rate(crawl_task_received_total[5m])) by (site)
 *
 * # 사이트별 추출된 아이템 수
 * sum(increase(crawl_task_items_extracted_total[1h])) by (site)
 *
 * # 현재 처리 중인 태스크 수
 * crawl_task_in_progress
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskMetrics {

    private static final String METRIC_PREFIX = "crawl.task";

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, AtomicInteger> inProgressCounters;

    public CrawlTaskMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.inProgressCounters = new ConcurrentHashMap<>();

        // 기본 in-progress gauge 등록
        Gauge.builder(
                        METRIC_PREFIX + ".in.progress.total",
                        inProgressCounters,
                        map -> map.values().stream().mapToInt(AtomicInteger::get).sum())
                .description("Total crawl tasks currently in progress")
                .register(meterRegistry);
    }

    /**
     * 크롤링 태스크 수신 기록
     *
     * @param siteName 사이트명
     * @param taskType 태스크 유형 (product, category, price 등)
     */
    public void recordTaskReceived(String siteName, String taskType) {
        Counter.builder(METRIC_PREFIX + ".received.total")
                .description("Total crawl tasks received")
                .tag("site", siteName)
                .tag("type", taskType)
                .register(meterRegistry)
                .increment();

        // In-progress 카운터 증가
        getOrCreateInProgressCounter(siteName).incrementAndGet();
    }

    /**
     * 크롤링 태스크 성공 완료 기록
     *
     * @param siteName 사이트명
     * @param taskType 태스크 유형
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordTaskCompleted(String siteName, String taskType, long durationMs) {
        Counter.builder(METRIC_PREFIX + ".completed.total")
                .description("Total crawl tasks completed successfully")
                .tag("site", siteName)
                .tag("type", taskType)
                .register(meterRegistry)
                .increment();

        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Crawl task processing duration")
                .tag("site", siteName)
                .tag("type", taskType)
                .tag("status", "success")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        // In-progress 카운터 감소
        getOrCreateInProgressCounter(siteName).decrementAndGet();
    }

    /**
     * 크롤링 태스크 실패 기록
     *
     * @param siteName 사이트명
     * @param taskType 태스크 유형
     * @param errorType 에러 유형
     * @param durationMs 소요 시간 (밀리초)
     */
    public void recordTaskFailed(
            String siteName, String taskType, String errorType, long durationMs) {
        Counter.builder(METRIC_PREFIX + ".failed.total")
                .description("Total crawl tasks failed")
                .tag("site", siteName)
                .tag("type", taskType)
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();

        Timer.builder(METRIC_PREFIX + ".duration")
                .description("Crawl task processing duration")
                .tag("site", siteName)
                .tag("type", taskType)
                .tag("status", "failure")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        // In-progress 카운터 감소
        getOrCreateInProgressCounter(siteName).decrementAndGet();
    }

    /**
     * 추출된 아이템 수 기록
     *
     * @param siteName 사이트명
     * @param itemType 아이템 유형 (product, category, image 등)
     * @param count 추출된 아이템 수
     */
    public void recordItemsExtracted(String siteName, String itemType, int count) {
        Counter.builder(METRIC_PREFIX + ".items.extracted.total")
                .description("Total items extracted from crawl tasks")
                .tag("site", siteName)
                .tag("item_type", itemType)
                .register(meterRegistry)
                .increment(count);
    }

    /**
     * 재시도 기록
     *
     * @param siteName 사이트명
     * @param taskType 태스크 유형
     * @param retryCount 재시도 횟수
     */
    public void recordTaskRetry(String siteName, String taskType, int retryCount) {
        Counter.builder(METRIC_PREFIX + ".retry.total")
                .description("Total crawl task retries")
                .tag("site", siteName)
                .tag("type", taskType)
                .tag("retry_count", String.valueOf(retryCount))
                .register(meterRegistry)
                .increment();
    }

    /**
     * DLQ로 이동된 태스크 기록
     *
     * @param siteName 사이트명
     * @param taskType 태스크 유형
     * @param reason DLQ 이동 사유
     */
    public void recordTaskMovedToDlq(String siteName, String taskType, String reason) {
        Counter.builder(METRIC_PREFIX + ".dlq.total")
                .description("Total crawl tasks moved to DLQ")
                .tag("site", siteName)
                .tag("type", taskType)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    /**
     * HTTP 응답 상태 코드 기록
     *
     * @param siteName 사이트명
     * @param statusCode HTTP 상태 코드
     */
    public void recordHttpStatus(String siteName, int statusCode) {
        String statusGroup = getStatusGroup(statusCode);
        Counter.builder(METRIC_PREFIX + ".http.status.total")
                .description("HTTP response status codes from crawl requests")
                .tag("site", siteName)
                .tag("status", String.valueOf(statusCode))
                .tag("status_group", statusGroup)
                .register(meterRegistry)
                .increment();
    }

    private AtomicInteger getOrCreateInProgressCounter(String siteName) {
        return inProgressCounters.computeIfAbsent(
                siteName,
                key -> {
                    AtomicInteger counter = new AtomicInteger(0);
                    Gauge.builder(METRIC_PREFIX + ".in.progress", counter, AtomicInteger::get)
                            .description("Crawl tasks currently in progress")
                            .tag("site", key)
                            .register(meterRegistry);
                    return counter;
                });
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
