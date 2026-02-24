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
}
