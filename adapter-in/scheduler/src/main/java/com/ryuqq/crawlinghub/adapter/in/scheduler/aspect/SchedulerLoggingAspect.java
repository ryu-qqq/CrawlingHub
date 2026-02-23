package com.ryuqq.crawlinghub.adapter.in.scheduler.aspect;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * 스케줄러 작업 로깅 AOP
 *
 * <p><strong>기능</strong>:
 *
 * <ul>
 *   <li>TraceId MDC 자동 주입
 *   <li>시작/종료 로깅
 *   <li>SchedulerBatchProcessingResult 인식하여 결과 로깅
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Aspect
@Component
public class SchedulerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(SchedulerLoggingAspect.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Around("@annotation(schedulerJob)")
    public Object around(ProceedingJoinPoint joinPoint, SchedulerJob schedulerJob)
            throws Throwable {
        String jobName = schedulerJob.value();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        MDC.put(TRACE_ID_KEY, traceId);
        long startTime = System.currentTimeMillis();

        try {
            log.info("[{}] 스케줄러 작업 시작", jobName);

            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - startTime;

            if (result instanceof SchedulerBatchProcessingResult batchResult) {
                if (batchResult.hasFailures()) {
                    log.warn(
                            "[{}] 스케줄러 작업 완료 (일부 실패): total={}, success={}, failed={},"
                                    + " elapsed={}ms",
                            jobName,
                            batchResult.total(),
                            batchResult.success(),
                            batchResult.failed(),
                            elapsed);
                } else {
                    log.info(
                            "[{}] 스케줄러 작업 완료: total={}, success={}, elapsed={}ms",
                            jobName,
                            batchResult.total(),
                            batchResult.success(),
                            elapsed);
                }
            } else {
                log.info("[{}] 스케줄러 작업 완료: elapsed={}ms", jobName, elapsed);
            }

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error(
                    "[{}] 스케줄러 작업 실패: error={}, elapsed={}ms", jobName, e.getMessage(), elapsed, e);
            throw e;
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
