package com.ryuqq.crawlinghub.application.common.metrics.aspect;

import com.ryuqq.crawlinghub.application.common.metrics.DownstreamMetrics;
import com.ryuqq.crawlinghub.application.common.metrics.annotation.DownstreamMetric;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Downstream 의존성 메트릭 수집 Aspect
 *
 * <p>{@link DownstreamMetric} 어노테이션이 적용된 메서드의 latency를 자동 측정합니다.
 *
 * <h3>지원 서비스</h3>
 *
 * <ul>
 *   <li>redis: Redis 작업 latency
 *   <li>db: Database 쿼리 latency
 *   <li>sqs: SQS 메시지 발행/소비 latency
 *   <li>external_api: 외부 API 호출 latency
 *   <li>crawl_http: 크롤링 HTTP 요청 latency
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Aspect
@Component
public class DownstreamMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(DownstreamMetricAspect.class);

    private final DownstreamMetrics downstreamMetrics;

    public DownstreamMetricAspect(DownstreamMetrics downstreamMetrics) {
        this.downstreamMetrics = downstreamMetrics;
    }

    /**
     * @DownstreamMetric 어노테이션 메서드 처리
     *
     * <p>서비스 유형에 따라 적절한 메트릭 기록
     */
    @Around("@annotation(downstreamMetric)")
    public Object aroundDownstreamCall(
            ProceedingJoinPoint joinPoint, DownstreamMetric downstreamMetric) throws Throwable {
        String service = downstreamMetric.service();
        String operation = downstreamMetric.operation();

        if (operation.isEmpty()) {
            operation = joinPoint.getSignature().getName();
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            recordMetric(service, operation, durationMs);

            return result;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;

            recordMetric(service, operation + "_error", durationMs);

            throw e;
        }
    }

    private void recordMetric(String service, String operation, long durationMs) {
        switch (service) {
            case "redis":
                downstreamMetrics.recordRedisLatency(operation, durationMs);
                break;
            case "sqs":
                if (operation.contains("publish") || operation.contains("send")) {
                    downstreamMetrics.recordSqsPublishLatency(operation, durationMs);
                } else {
                    downstreamMetrics.recordSqsConsumeLatency(operation, durationMs);
                }
                break;
            case "external_api":
                downstreamMetrics.recordExternalApiLatency(service, operation, durationMs);
                break;
            case "crawl_http":
                downstreamMetrics.recordCrawlHttpLatency("unknown", operation, durationMs);
                break;
            case "db":
                downstreamMetrics.recordDbLatency(operation, "unknown", durationMs);
                break;
            default:
                log.debug("Unknown downstream service: {}", service);
        }
    }
}
