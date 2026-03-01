package com.ryuqq.crawlinghub.application.common.metric;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class CrawlHubMetrics {

    private static final String PREFIX = "crawlinghub.";

    private final MeterRegistry meterRegistry;

    public CrawlHubMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String name, String... tags) {
        sample.stop(Timer.builder(PREFIX + name).tags(tags).register(meterRegistry));
    }

    public void incrementCounter(String name, String... tags) {
        Counter.builder(PREFIX + name).tags(tags).register(meterRegistry).increment();
    }

    public void recordBatchResult(
            String name, String category, SchedulerBatchProcessingResult result) {
        Counter.builder(PREFIX + name + "_items_total")
                .tags("category", category, "status", "total")
                .register(meterRegistry)
                .increment(result.total());
        Counter.builder(PREFIX + name + "_items_total")
                .tags("category", category, "status", "success")
                .register(meterRegistry)
                .increment(result.success());
        Counter.builder(PREFIX + name + "_items_total")
                .tags("category", category, "status", "failed")
                .register(meterRegistry)
                .increment(result.failed());
    }

    public void recordDuration(String name, Duration duration, String... tags) {
        Timer.builder(PREFIX + name).tags(tags).register(meterRegistry).record(duration);
    }

    public void incrementCounterWithStatusCode(String name, int statusCode, String... tags) {
        String statusGroup = groupStatusCode(statusCode);
        String[] allTags =
                appendTags(
                        tags,
                        "status_code",
                        String.valueOf(statusCode),
                        "status_group",
                        statusGroup);
        Counter.builder(PREFIX + name).tags(allTags).register(meterRegistry).increment();
    }

    private String groupStatusCode(int code) {
        if (code == 0) return "none";
        if (code == 429) return "rate_limited";
        if (code >= 200 && code < 300) return "2xx";
        if (code >= 400 && code < 500) return "4xx";
        if (code >= 500) return "5xx";
        return "other";
    }

    private String[] appendTags(String[] base, String... extra) {
        String[] result = new String[base.length + extra.length];
        System.arraycopy(base, 0, result, 0, base.length);
        System.arraycopy(extra, 0, result, base.length, extra.length);
        return result;
    }
}
