package com.ryuqq.crawlinghub.adapter.in.scheduler.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.adapter.in.scheduler.annotation.SchedulerJob;
import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * SchedulerLoggingAspect 단위 테스트
 *
 * <p>SimpleMeterRegistry로 메트릭 기록 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("SchedulerLoggingAspect 단위 테스트")
class SchedulerLoggingAspectTest {

    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private SchedulerJob schedulerJob;

    private MeterRegistry meterRegistry;
    private SchedulerLoggingAspect sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new SchedulerLoggingAspect(meterRegistry);
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 성공 케이스")
    class AroundSuccessTest {

        @Test
        @DisplayName("[성공] 정상 처리 시 success 타이머와 카운터를 기록한다")
        void shouldRecordSuccessMetricsWhenProceedSucceeds() throws Throwable {
            // Given
            given(schedulerJob.value()).willReturn("test-job");
            given(joinPoint.proceed()).willReturn("result");

            // When
            Object result = sut.around(joinPoint, schedulerJob);

            // Then
            assertThat(result).isEqualTo("result");

            Timer timer =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_duration_seconds")
                            .tags("job_name", "test-job", "outcome", "success")
                            .timer();
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_total")
                            .tags("job_name", "test-job", "outcome", "success")
                            .counter();

            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] SchedulerBatchProcessingResult 반환 시 배치 아이템 카운터도 기록한다")
        void shouldRecordBatchItemMetricsWhenResultIsSchedulerBatchProcessingResult()
                throws Throwable {
            // Given
            given(schedulerJob.value()).willReturn("batch-job");
            SchedulerBatchProcessingResult batchResult =
                    SchedulerBatchProcessingResult.of(10, 9, 1);
            given(joinPoint.proceed()).willReturn(batchResult);

            // When
            Object result = sut.around(joinPoint, schedulerJob);

            // Then
            assertThat(result).isEqualTo(batchResult);

            Counter totalCounter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_batch_items_total")
                            .tags("job_name", "batch-job", "result", "total")
                            .counter();
            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_batch_items_total")
                            .tags("job_name", "batch-job", "result", "success")
                            .counter();
            Counter failedCounter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_batch_items_total")
                            .tags("job_name", "batch-job", "result", "failed")
                            .counter();

            assertThat(totalCounter.count()).isEqualTo(10.0);
            assertThat(successCounter.count()).isEqualTo(9.0);
            assertThat(failedCounter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] null 반환값도 그대로 반환한다")
        void shouldReturnNullWhenProceedReturnsNull() throws Throwable {
            // Given
            given(schedulerJob.value()).willReturn("void-job");
            given(joinPoint.proceed()).willReturn(null);

            // When
            Object result = sut.around(joinPoint, schedulerJob);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 실패 케이스")
    class AroundFailureTest {

        @Test
        @DisplayName("[실패] 예외 발생 시 error 타이머와 카운터를 기록하고 예외를 전파한다")
        void shouldRecordErrorMetricsAndRethrowException() throws Throwable {
            // Given
            given(schedulerJob.value()).willReturn("failing-job");
            RuntimeException exception = new RuntimeException("스케줄러 오류");
            given(joinPoint.proceed()).willThrow(exception);

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, schedulerJob))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("스케줄러 오류");

            Timer errorTimer =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_duration_seconds")
                            .tags("job_name", "failing-job", "outcome", "error")
                            .timer();
            Counter errorCounter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_total")
                            .tags("job_name", "failing-job", "outcome", "error")
                            .counter();

            assertThat(errorTimer).isNotNull();
            assertThat(errorTimer.count()).isEqualTo(1);
            assertThat(errorCounter).isNotNull();
            assertThat(errorCounter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[실패] 예외 발생 시 success 카운터는 기록되지 않는다")
        void shouldNotRecordSuccessCounterOnException() throws Throwable {
            // Given
            given(schedulerJob.value()).willReturn("failing-job");
            given(joinPoint.proceed()).willThrow(new RuntimeException("오류"));

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, schedulerJob))
                    .isInstanceOf(RuntimeException.class);

            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.scheduler_job_total")
                            .tags("job_name", "failing-job", "outcome", "success")
                            .counter();
            assertThat(successCounter).isNull();
        }
    }
}
