package com.ryuqq.crawlinghub.application.common.metric.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.common.metric.annotation.BatchMetric;
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
 * BatchMetricAspect 단위 테스트
 *
 * <p>SimpleMeterRegistry로 실제 메트릭 기록 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("BatchMetricAspect 단위 테스트")
class BatchMetricAspectTest {

    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private BatchMetric batchMetric;

    private MeterRegistry meterRegistry;
    private CrawlHubMetrics metrics;
    private BatchMetricAspect sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new CrawlHubMetrics(meterRegistry);
        sut = new BatchMetricAspect(metrics);
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 성공 케이스")
    class AroundSuccessTest {

        @Test
        @DisplayName("[성공] 정상 처리 시 success 카운터와 타이머를 기록한다")
        void shouldRecordSuccessMetricsWhenProceedSucceeds() throws Throwable {
            // Given
            given(batchMetric.value()).willReturn("crawl_task_outbox");
            given(batchMetric.category()).willReturn("process_pending");
            given(joinPoint.proceed()).willReturn("result");

            // When
            Object result = sut.around(joinPoint, batchMetric);

            // Then
            assertThat(result).isEqualTo("result");

            Timer timer =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_duration_seconds")
                            .tags("category", "process_pending", "outcome", "success")
                            .timer();
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_total")
                            .tags("category", "process_pending", "outcome", "success")
                            .counter();

            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] SchedulerBatchProcessingResult 반환 시 배치 항목 카운터도 기록한다")
        void shouldRecordBatchItemMetricsWhenResultIsSchedulerBatchProcessingResult()
                throws Throwable {
            // Given
            given(batchMetric.value()).willReturn("product_sync_outbox");
            given(batchMetric.category()).willReturn("publish_pending");
            SchedulerBatchProcessingResult batchResult = SchedulerBatchProcessingResult.of(5, 4, 1);
            given(joinPoint.proceed()).willReturn(batchResult);

            // When
            Object result = sut.around(joinPoint, batchMetric);

            // Then
            assertThat(result).isEqualTo(batchResult);

            Counter totalCounter =
                    meterRegistry
                            .find("crawlinghub.product_sync_outbox_items_total")
                            .tags("category", "publish_pending", "status", "total")
                            .counter();
            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.product_sync_outbox_items_total")
                            .tags("category", "publish_pending", "status", "success")
                            .counter();
            Counter failedCounter =
                    meterRegistry
                            .find("crawlinghub.product_sync_outbox_items_total")
                            .tags("category", "publish_pending", "status", "failed")
                            .counter();

            assertThat(totalCounter.count()).isEqualTo(5.0);
            assertThat(successCounter.count()).isEqualTo(4.0);
            assertThat(failedCounter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] SchedulerBatchProcessingResult가 아닌 반환값이면 배치 항목 카운터를 기록하지 않는다")
        void shouldNotRecordBatchItemMetricsWhenResultIsNotBatchProcessingResult()
                throws Throwable {
            // Given
            given(batchMetric.value()).willReturn("crawl_task_outbox");
            given(batchMetric.category()).willReturn("process_pending");
            given(joinPoint.proceed()).willReturn("non-batch-result");

            // When
            sut.around(joinPoint, batchMetric);

            // Then
            Counter itemsCounter =
                    meterRegistry.find("crawlinghub.crawl_task_outbox_items_total").counter();
            assertThat(itemsCounter).isNull();
        }

        @Test
        @DisplayName("[성공] null 반환값도 그대로 반환한다")
        void shouldReturnNullWhenProceedReturnsNull() throws Throwable {
            // Given
            given(batchMetric.value()).willReturn("crawl_task_outbox");
            given(batchMetric.category()).willReturn("process_pending");
            given(joinPoint.proceed()).willReturn(null);

            // When
            Object result = sut.around(joinPoint, batchMetric);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 실패 케이스")
    class AroundFailureTest {

        @Test
        @DisplayName("[실패] 예외 발생 시 error 카운터와 타이머를 기록하고 예외를 전파한다")
        void shouldRecordErrorMetricsAndRethrowException() throws Throwable {
            // Given
            given(batchMetric.value()).willReturn("crawl_task_outbox");
            given(batchMetric.category()).willReturn("process_pending");
            RuntimeException exception = new RuntimeException("배치 처리 실패");
            given(joinPoint.proceed()).willThrow(exception);

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, batchMetric))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("배치 처리 실패");

            Timer errorTimer =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_duration_seconds")
                            .tags("category", "process_pending", "outcome", "error")
                            .timer();
            Counter errorCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_total")
                            .tags("category", "process_pending", "outcome", "error")
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
            given(batchMetric.value()).willReturn("crawl_task_outbox");
            given(batchMetric.category()).willReturn("process_pending");
            given(joinPoint.proceed()).willThrow(new RuntimeException("오류"));

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, batchMetric))
                    .isInstanceOf(RuntimeException.class);

            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_total")
                            .tags("category", "process_pending", "outcome", "success")
                            .counter();
            assertThat(successCounter).isNull();
        }
    }
}
