package com.ryuqq.crawlinghub.application.common.metrics.aspect;

import com.ryuqq.crawlinghub.application.common.metrics.UserAgentMetrics;
import com.ryuqq.crawlinghub.application.common.metrics.annotation.UserAgentMetric;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent 메트릭 수집 Aspect
 *
 * <p>{@link UserAgentMetric} 어노테이션이 적용된 메서드의 메트릭을 자동 수집합니다.
 *
 * <h3>지원 작업</h3>
 *
 * <ul>
 *   <li>consume: UserAgent 토큰 소비 (latency, 성공/실패)
 *   <li>record_result: 크롤링 결과 기록 (성공/실패/429)
 *   <li>recover: SUSPENDED UserAgent 복구
 *   <li>check_circuit_breaker: Circuit Breaker 체크
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Aspect
@Component
public class UserAgentMetricAspect {

    private static final Logger log = LoggerFactory.getLogger(UserAgentMetricAspect.class);

    private final UserAgentMetrics userAgentMetrics;

    public UserAgentMetricAspect(UserAgentMetrics userAgentMetrics) {
        this.userAgentMetrics = userAgentMetrics;
    }

    /**
     * @UserAgentMetric(operation = "consume") 메서드 처리
     *
     * <p>UserAgent 소비에 대한 메트릭 수집:
     *
     * <ul>
     *   <li>consume.total: 소비 시도 횟수
     *   <li>consume.latency: 소비 지연 시간
     *   <li>consume.success.total: 소비 성공 횟수
     *   <li>consume.failure.total: 소비 실패 횟수 (NoAvailableUserAgentException)
     *   <li>circuit_breaker.total: Circuit Breaker 발동 횟수
     * </ul>
     */
    @Around("@annotation(userAgentMetric) && args(..)")
    public Object aroundUserAgentOperation(
            ProceedingJoinPoint joinPoint, UserAgentMetric userAgentMetric) throws Throwable {
        String operation = userAgentMetric.operation();

        switch (operation) {
            case "consume":
                return handleConsume(joinPoint);
            case "record_result":
                return handleRecordResult(joinPoint);
            case "recover":
                return handleRecover(joinPoint);
            case "get_pool_stats":
                return handleGetPoolStats(joinPoint);
            default:
                return joinPoint.proceed();
        }
    }

    private Object handleConsume(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            userAgentMetrics.recordConsume(duration);

            if (result instanceof CachedUserAgent cachedUserAgent) {
                userAgentMetrics.recordConsumeSuccess(cachedUserAgent.userAgentId());
            }

            return result;
        } catch (NoAvailableUserAgentException e) {
            long duration = System.currentTimeMillis() - startTime;
            userAgentMetrics.recordConsume(duration);
            userAgentMetrics.recordConsumeFailure();
            throw e;
        } catch (CircuitBreakerOpenException e) {
            long duration = System.currentTimeMillis() - startTime;
            userAgentMetrics.recordConsume(duration);
            userAgentMetrics.recordCircuitBreakerOpen(0.0);
            throw e;
        }
    }

    private Object handleRecordResult(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();

        Object result = joinPoint.proceed();

        if (args.length > 0 && args[0] instanceof RecordUserAgentResultCommand command) {
            if (command.success()) {
                userAgentMetrics.recordSuccess(command.userAgentId());
            } else if (command.isRateLimited()) {
                userAgentMetrics.recordRateLimited(command.userAgentId());
                userAgentMetrics.recordSuspended(command.userAgentId(), "rate_limited");
            } else {
                userAgentMetrics.recordFailure(command.userAgentId(), command.httpStatusCode());
            }
        }

        return result;
    }

    private Object handleRecover(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof Integer recoveredCount && recoveredCount > 0) {
            userAgentMetrics.recordRecovered(recoveredCount);
        }

        return result;
    }

    private Object handleGetPoolStats(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        if (result instanceof PoolStats stats) {
            userAgentMetrics.updatePoolStats((int) stats.available(), (int) stats.total());
        }

        return result;
    }
}
