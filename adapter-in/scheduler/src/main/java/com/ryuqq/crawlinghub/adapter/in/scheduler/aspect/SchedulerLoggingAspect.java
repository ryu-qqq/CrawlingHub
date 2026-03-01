package com.ryuqq.crawlinghub.adapter.in.scheduler.aspect;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
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
    private static final String METRIC_PREFIX = "crawlinghub.";

    private final MeterRegistry meterRegistry;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "MeterRegistry는 Spring IoC 컨테이너가 관리하는 싱글톤이며, 외부 변경 위험 없음")
    public SchedulerLoggingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

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
                recordBatchItemsMetric(jobName, batchResult);
            } else {
                log.info("[{}] 스케줄러 작업 완료: elapsed={}ms", jobName, elapsed);
            }

            recordTimerMetric(jobName, elapsed, "success");
            recordCounterMetric(jobName, "success");

            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error(
                    "[{}] 스케줄러 작업 실패: error={}, elapsed={}ms", jobName, e.getMessage(), elapsed, e);
            recordTimerMetric(jobName, elapsed, "error");
            recordCounterMetric(jobName, "error");
            throw e;
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }

    private void recordTimerMetric(String jobName, long elapsedMs, String outcome) {
        Timer.builder(METRIC_PREFIX + "scheduler_job_duration_seconds")
                .tags("job_name", jobName, "outcome", outcome)
                .register(meterRegistry)
                .record(elapsedMs, TimeUnit.MILLISECONDS);
    }

    private void recordCounterMetric(String jobName, String outcome) {
        Counter.builder(METRIC_PREFIX + "scheduler_job_total")
                .tags("job_name", jobName, "outcome", outcome)
                .register(meterRegistry)
                .increment();
    }

    private void recordBatchItemsMetric(String jobName, SchedulerBatchProcessingResult result) {
        Counter.builder(METRIC_PREFIX + "scheduler_job_batch_items_total")
                .tags("job_name", jobName, "result", "total")
                .register(meterRegistry)
                .increment(result.total());
        Counter.builder(METRIC_PREFIX + "scheduler_job_batch_items_total")
                .tags("job_name", jobName, "result", "success")
                .register(meterRegistry)
                .increment(result.success());
        Counter.builder(METRIC_PREFIX + "scheduler_job_batch_items_total")
                .tags("job_name", jobName, "result", "failed")
                .register(meterRegistry)
                .increment(result.failed());
    }
}
