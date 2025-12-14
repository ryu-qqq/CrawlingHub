package com.ryuqq.crawlinghub.application.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Scheduler Job Metrics Recorder
 *
 * <p>스케줄러 Job 실행 메트릭 수집 전담 컴포넌트
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>scheduler.job.runs.total: Job 실행 총 횟수
 *   <li>scheduler.job.success.total: Job 성공 횟수
 *   <li>scheduler.job.failure.total: Job 실패 횟수
 *   <li>scheduler.job.duration: Job 실행 소요 시간
 *   <li>scheduler.job.items.processed: Job에서 처리한 항목 수
 * </ul>
 *
 * <h3>Prometheus 쿼리 예시</h3>
 *
 * <pre>{@code
 * # Job 성공률
 * sum(rate(scheduler_job_success_total[5m])) by (job)
 * / sum(rate(scheduler_job_runs_total[5m])) by (job)
 *
 * # Job 평균 실행 시간
 * rate(scheduler_job_duration_seconds_sum[5m])
 * / rate(scheduler_job_duration_seconds_count[5m])
 *
 * # Job 실패 알림 (5분간 3회 이상 실패)
 * sum(increase(scheduler_job_failure_total[5m])) by (job) > 3
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SchedulerMetrics {

    private static final String METRIC_PREFIX = "scheduler";

    private final MeterRegistry meterRegistry;

    public SchedulerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Job 실행 시작
     *
     * <p>Job 실행 횟수를 증가시키고 Timer를 시작합니다.
     *
     * <h3>사용 예시</h3>
     *
     * <pre>{@code
     * Timer.Sample sample = schedulerMetrics.startJob("outbox-retry");
     * try {
     *     // Job 로직
     *     schedulerMetrics.recordJobSuccess("outbox-retry", sample);
     * } catch (Exception e) {
     *     schedulerMetrics.recordJobFailure("outbox-retry", sample, e.getClass().getSimpleName());
     *     throw e;
     * }
     * }</pre>
     *
     * @param jobName Job 이름
     * @return Timer.Sample 타이머 샘플 (완료 시 사용)
     */
    public Timer.Sample startJob(String jobName) {
        Counter.builder(METRIC_PREFIX + ".job.runs.total")
                .description("Total job executions")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();

        return Timer.start(meterRegistry);
    }

    /**
     * Job 성공 완료 기록
     *
     * @param jobName Job 이름
     * @param sample 시작 시 반환된 Timer.Sample
     */
    public void recordJobSuccess(String jobName, Timer.Sample sample) {
        Counter.builder(METRIC_PREFIX + ".job.success.total")
                .description("Successful job executions")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment();

        sample.stop(
                Timer.builder(METRIC_PREFIX + ".job.duration")
                        .description("Job execution duration")
                        .tag("job", jobName)
                        .tag("status", "success")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Job 실패 기록
     *
     * @param jobName Job 이름
     * @param sample 시작 시 반환된 Timer.Sample
     * @param errorType 에러 타입 (예외 클래스명)
     */
    public void recordJobFailure(String jobName, Timer.Sample sample, String errorType) {
        Counter.builder(METRIC_PREFIX + ".job.failure.total")
                .description("Failed job executions")
                .tag("job", jobName)
                .tag("error", errorType)
                .register(meterRegistry)
                .increment();

        sample.stop(
                Timer.builder(METRIC_PREFIX + ".job.duration")
                        .description("Job execution duration")
                        .tag("job", jobName)
                        .tag("status", "failure")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry));
    }

    /**
     * Job 처리 항목 수 기록
     *
     * @param jobName Job 이름
     * @param count 처리된 항목 수
     */
    public void recordJobItemsProcessed(String jobName, int count) {
        Counter.builder(METRIC_PREFIX + ".job.items.processed")
                .description("Number of items processed by job")
                .tag("job", jobName)
                .register(meterRegistry)
                .increment(count);
    }

    /**
     * Job 스킵 기록 (실행 조건 미충족 시)
     *
     * @param jobName Job 이름
     * @param reason 스킵 사유
     */
    public void recordJobSkipped(String jobName, String reason) {
        Counter.builder(METRIC_PREFIX + ".job.skipped.total")
                .description("Skipped job executions")
                .tag("job", jobName)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }
}
