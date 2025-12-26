package com.ryuqq.crawlinghub.application.common.metrics.aspect;

import com.ryuqq.crawlinghub.application.common.metrics.CrawlTaskMetrics;
import com.ryuqq.crawlinghub.application.common.metrics.DownstreamMetrics;
import com.ryuqq.crawlinghub.application.common.metrics.annotation.CrawlMetric;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlContext;
import com.ryuqq.crawlinghub.application.crawl.dto.CrawlResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 크롤링 실행 메트릭 수집 Aspect
 *
 * <p>{@link CrawlMetric} 어노테이션이 적용된 메서드의 메트릭을 자동 수집합니다.
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>crawl.http.latency: HTTP 요청 latency
 *   <li>crawl.http.status.total: HTTP 상태 코드별 카운트
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Aspect
@Component
public class CrawlMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(CrawlMetricAspect.class);
    private static final String DEFAULT_SITE = "unknown";

    private final CrawlTaskMetrics crawlTaskMetrics;
    private final DownstreamMetrics downstreamMetrics;

    public CrawlMetricAspect(
            CrawlTaskMetrics crawlTaskMetrics, DownstreamMetrics downstreamMetrics) {
        this.crawlTaskMetrics = crawlTaskMetrics;
        this.downstreamMetrics = downstreamMetrics;
    }

    /**
     * @CrawlMetric 어노테이션 메서드 처리
     *
     * <p>크롤링 실행에 대한 메트릭 수집:
     *
     * <ul>
     *   <li>http.latency: HTTP 요청 지연 시간
     *   <li>http.status.total: HTTP 상태 코드별 카운트
     * </ul>
     */
    @Around("@annotation(crawlMetric)")
    public Object aroundCrawlExecution(ProceedingJoinPoint joinPoint, CrawlMetric crawlMetric)
            throws Throwable {
        Object[] args = joinPoint.getArgs();
        CrawlContext context = extractContext(args);

        String crawlerType = crawlMetric.crawlerType();
        if (crawlerType.isEmpty()) {
            crawlerType = extractCrawlerType(joinPoint);
        }

        String siteName = context != null ? extractSiteName(context) : DEFAULT_SITE;

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;

            downstreamMetrics.recordCrawlHttpLatency(siteName, crawlerType, durationMs);

            if (result instanceof CrawlResult crawlResult) {
                Integer statusCode = crawlResult.getHttpStatusCode();
                if (statusCode != null) {
                    crawlTaskMetrics.recordHttpStatus(siteName, statusCode);
                }

                if (crawlResult.isSuccess()) {
                    log.debug(
                            "Crawl success: site={}, type={}, duration={}ms, status={}",
                            siteName,
                            crawlerType,
                            durationMs,
                            statusCode);
                } else {
                    log.debug(
                            "Crawl failure: site={}, type={}, duration={}ms, status={}, error={}",
                            siteName,
                            crawlerType,
                            durationMs,
                            statusCode,
                            crawlResult.getErrorMessage());
                }
            }

            return result;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;

            downstreamMetrics.recordCrawlHttpLatency(siteName, crawlerType + "_error", durationMs);

            log.debug(
                    "Crawl exception: site={}, type={}, duration={}ms, error={}",
                    siteName,
                    crawlerType,
                    durationMs,
                    e.getMessage());

            throw e;
        }
    }

    private CrawlContext extractContext(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg instanceof CrawlContext context) {
                return context;
            }
        }

        return null;
    }

    private String extractCrawlerType(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();

        return className.replace("Crawler", "").toLowerCase();
    }

    private String extractSiteName(CrawlContext context) {
        String endpoint = context.getEndpoint();
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
}
