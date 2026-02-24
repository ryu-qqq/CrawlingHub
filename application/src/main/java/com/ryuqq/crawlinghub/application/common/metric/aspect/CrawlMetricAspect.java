package com.ryuqq.crawlinghub.application.common.metric.aspect;

import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CrawlMetricAspect {

    private final CrawlHubMetrics metrics;

    public CrawlMetricAspect(CrawlHubMetrics metrics) {
        this.metrics = metrics;
    }

    @Around("@annotation(crawlMetric)")
    public Object around(ProceedingJoinPoint joinPoint, CrawlMetric crawlMetric) throws Throwable {
        String metricName = crawlMetric.value();
        String operation = crawlMetric.operation();

        Timer.Sample sample = metrics.startTimer();
        try {
            Object result = joinPoint.proceed();
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "operation",
                    operation,
                    "outcome",
                    "success");
            metrics.incrementCounter(
                    metricName + "_total", "operation", operation, "outcome", "success");
            return result;
        } catch (Exception e) {
            metrics.stopTimer(
                    sample,
                    metricName + "_duration_seconds",
                    "operation",
                    operation,
                    "outcome",
                    "error");
            metrics.incrementCounter(
                    metricName + "_total", "operation", operation, "outcome", "error");
            metrics.incrementCounter(
                    metricName + "_errors_total",
                    "operation",
                    operation,
                    "exception",
                    e.getClass().getSimpleName());
            throw e;
        }
    }
}
