package com.ryuqq.crawlinghub.application.common.metrics.aspect;

import com.ryuqq.crawlinghub.application.common.metrics.CrawlTaskMetrics;
import com.ryuqq.crawlinghub.application.common.metrics.annotation.CrawlTaskMetric;
import com.ryuqq.crawlinghub.application.execution.dto.command.ExecuteCrawlTaskCommand;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * CrawlTask 메트릭 수집 Aspect
 *
 * <p>{@link CrawlTaskMetric} 어노테이션이 적용된 메서드의 메트릭을 자동 수집합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>crawl.task.received.total: 수신된 태스크 수
 *   <li>crawl.task.completed.total: 완료된 태스크 수
 *   <li>crawl.task.failed.total: 실패한 태스크 수
 *   <li>crawl.task.duration: 처리 시간
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Aspect
@Component
public class CrawlTaskMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(CrawlTaskMetricAspect.class);
    private static final String DEFAULT_SITE = "unknown";

    private final CrawlTaskMetrics crawlTaskMetrics;

    public CrawlTaskMetricAspect(CrawlTaskMetrics crawlTaskMetrics) {
        this.crawlTaskMetrics = crawlTaskMetrics;
    }

    /**
     * @CrawlTaskMetric(operation = "execute") 메서드 처리
     *
     * <p>CrawlTask 실행에 대한 메트릭 수집:
     *
     * <ul>
     *   <li>received.total: 태스크 수신 시 증가
     *   <li>completed.total: 태스크 성공 시 증가
     *   <li>failed.total: 태스크 실패 시 증가
     *   <li>duration: 처리 시간 기록
     * </ul>
     */
    @Around("@annotation(crawlTaskMetric)")
    public Object aroundCrawlTaskExecution(
            ProceedingJoinPoint joinPoint, CrawlTaskMetric crawlTaskMetric) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ExecuteCrawlTaskCommand command = extractCommand(args);

        if (command == null) {
            return joinPoint.proceed();
        }

        String siteName = extractSiteName(command);
        String taskType = command.taskType();

        crawlTaskMetrics.recordTaskReceived(siteName, taskType);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            crawlTaskMetrics.recordTaskCompleted(siteName, taskType, durationMs);

            return result;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;

            String errorType = extractErrorType(e);
            crawlTaskMetrics.recordTaskFailed(siteName, taskType, errorType, durationMs);

            throw e;
        }
    }

    private ExecuteCrawlTaskCommand extractCommand(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof ExecuteCrawlTaskCommand command) {
                return command;
            }
        }

        return null;
    }

    private String extractSiteName(ExecuteCrawlTaskCommand command) {
        String endpoint = command.endpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return DEFAULT_SITE;
        }

        try {
            java.net.URI uri = java.net.URI.create(endpoint);
            String host = uri.getHost();
            if (host != null) {
                return host.replaceFirst("^www\\.", "")
                        .replaceFirst("\\.com$", "")
                        .replaceFirst("\\.co\\.kr$", "");
            }
        } catch (Exception e) {
            log.debug("Failed to extract site name from endpoint: {}", endpoint);
        }

        return DEFAULT_SITE;
    }

    private String extractErrorType(Exception e) {
        String className = e.getClass().getSimpleName();
        return className.replace("Exception", "").toLowerCase();
    }
}
