package com.ryuqq.crawlinghub.application.common.metric;

import static org.assertj.core.api.Assertions.assertThat;

import com.ryuqq.crawlinghub.application.common.dto.result.SchedulerBatchProcessingResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * CrawlHubMetrics 단위 테스트
 *
 * <p>SimpleMeterRegistry를 사용하여 실제 메트릭 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@DisplayName("CrawlHubMetrics 단위 테스트")
class CrawlHubMetricsTest {

    private MeterRegistry meterRegistry;
    private CrawlHubMetrics sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new CrawlHubMetrics(meterRegistry);
    }

    @Nested
    @DisplayName("startTimer() 메서드 테스트")
    class StartTimerTest {

        @Test
        @DisplayName("[성공] Timer.Sample 인스턴스를 반환한다")
        void shouldReturnTimerSample() {
            // When
            Timer.Sample sample = sut.startTimer();

            // Then
            assertThat(sample).isNotNull();
        }
    }

    @Nested
    @DisplayName("stopTimer() 메서드 테스트")
    class StopTimerTest {

        @Test
        @DisplayName("[성공] 타이머를 등록하고 기록을 완료한다")
        void shouldRegisterAndRecordTimer() {
            // Given
            Timer.Sample sample = sut.startTimer();

            // When
            sut.stopTimer(
                    sample, "test_duration_seconds", "operation", "test", "outcome", "success");

            // Then
            Timer timer =
                    meterRegistry
                            .find("crawlinghub.test_duration_seconds")
                            .tags("operation", "test", "outcome", "success")
                            .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("[성공] 동일 메트릭에 여러 번 기록해도 count가 누적된다")
        void shouldAccumulateCountForSameMetric() {
            // Given
            Timer.Sample sample1 = sut.startTimer();
            Timer.Sample sample2 = sut.startTimer();

            // When
            sut.stopTimer(
                    sample1, "test_duration_seconds", "operation", "op1", "outcome", "success");
            sut.stopTimer(
                    sample2, "test_duration_seconds", "operation", "op1", "outcome", "success");

            // Then
            Timer timer =
                    meterRegistry
                            .find("crawlinghub.test_duration_seconds")
                            .tags("operation", "op1", "outcome", "success")
                            .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("incrementCounter() 메서드 테스트")
    class IncrementCounterTest {

        @Test
        @DisplayName("[성공] 카운터를 1 증가시킨다")
        void shouldIncrementCounterByOne() {
            // When
            sut.incrementCounter("test_total", "operation", "test", "outcome", "success");

            // Then
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.test_total")
                            .tags("operation", "test", "outcome", "success")
                            .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] 여러 번 호출 시 카운터가 누적된다")
        void shouldAccumulateCounterOnMultipleCalls() {
            // When
            sut.incrementCounter("test_total", "outcome", "success");
            sut.incrementCounter("test_total", "outcome", "success");
            sut.incrementCounter("test_total", "outcome", "success");

            // Then
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.test_total")
                            .tags("outcome", "success")
                            .counter();
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(3.0);
        }

        @Test
        @DisplayName("[성공] 다른 태그로 호출 시 별도의 카운터가 생성된다")
        void shouldCreateSeparateCountersForDifferentTags() {
            // When
            sut.incrementCounter("test_total", "outcome", "success");
            sut.incrementCounter("test_total", "outcome", "error");

            // Then
            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.test_total")
                            .tags("outcome", "success")
                            .counter();
            Counter errorCounter =
                    meterRegistry.find("crawlinghub.test_total").tags("outcome", "error").counter();
            assertThat(successCounter.count()).isEqualTo(1.0);
            assertThat(errorCounter.count()).isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("recordBatchResult() 메서드 테스트")
    class RecordBatchResultTest {

        @Test
        @DisplayName("[성공] 배치 결과를 total/success/failed 카운터로 분리 기록한다")
        void shouldRecordBatchResultInSeparateCounters() {
            // Given
            SchedulerBatchProcessingResult result = SchedulerBatchProcessingResult.of(10, 8, 2);

            // When
            sut.recordBatchResult("crawl_task_outbox", "process_pending", result);

            // Then
            Counter totalCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_items_total")
                            .tags("category", "process_pending", "status", "total")
                            .counter();
            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_items_total")
                            .tags("category", "process_pending", "status", "success")
                            .counter();
            Counter failedCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_items_total")
                            .tags("category", "process_pending", "status", "failed")
                            .counter();

            assertThat(totalCounter.count()).isEqualTo(10.0);
            assertThat(successCounter.count()).isEqualTo(8.0);
            assertThat(failedCounter.count()).isEqualTo(2.0);
        }

        @Test
        @DisplayName("[성공] 빈 배치 결과를 기록해도 카운터는 0으로 등록된다")
        void shouldRecordZeroCountForEmptyBatchResult() {
            // Given
            SchedulerBatchProcessingResult result = SchedulerBatchProcessingResult.empty();

            // When
            sut.recordBatchResult("crawl_task_outbox", "process_pending", result);

            // Then
            Counter totalCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_outbox_items_total")
                            .tags("category", "process_pending", "status", "total")
                            .counter();
            assertThat(totalCounter.count()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("recordDuration() 메서드 테스트")
    class RecordDurationTest {

        @Test
        @DisplayName("[성공] Duration을 타이머에 기록한다")
        void shouldRecordDurationToTimer() {
            // Given
            Duration duration = Duration.ofMillis(500);

            // When
            sut.recordDuration("crawl_task_duration", duration, "operation", "execute");

            // Then
            Timer timer =
                    meterRegistry
                            .find("crawlinghub.crawl_task_duration")
                            .tags("operation", "execute")
                            .timer();
            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
        }
    }
}
