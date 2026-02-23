package com.ryuqq.crawlinghub.application.common.metric.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.common.metric.annotation.CrawlMetric;
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
 * CrawlMetricAspect 단위 테스트
 *
 * <p>SimpleMeterRegistry로 실제 메트릭 기록 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("CrawlMetricAspect 단위 테스트")
class CrawlMetricAspectTest {

    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private CrawlMetric crawlMetric;

    private MeterRegistry meterRegistry;
    private CrawlHubMetrics metrics;
    private CrawlMetricAspect sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new CrawlHubMetrics(meterRegistry);
        sut = new CrawlMetricAspect(metrics);
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 성공 케이스")
    class AroundSuccessTest {

        @Test
        @DisplayName("[성공] 정상 처리 시 success 카운터와 타이머를 기록한다")
        void shouldRecordSuccessMetricsWhenProceedSucceeds() throws Throwable {
            // Given
            given(crawlMetric.value()).willReturn("crawl_task");
            given(crawlMetric.operation()).willReturn("execute");
            given(joinPoint.proceed()).willReturn("result");

            // When
            Object result = sut.around(joinPoint, crawlMetric);

            // Then
            assertThat(result).isEqualTo("result");

            Timer timer =
                    meterRegistry
                            .find("crawlinghub.crawl_task_duration_seconds")
                            .tags("operation", "execute", "outcome", "success")
                            .timer();
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_total")
                            .tags("operation", "execute", "outcome", "success")
                            .counter();

            assertThat(timer).isNotNull();
            assertThat(timer.count()).isEqualTo(1);
            assertThat(counter).isNotNull();
            assertThat(counter.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[성공] joinPoint 반환값을 그대로 반환한다")
        void shouldReturnProceedResult() throws Throwable {
            // Given
            given(crawlMetric.value()).willReturn("crawl_task");
            given(crawlMetric.operation()).willReturn("retry");
            String expectedResult = "retry-result";
            given(joinPoint.proceed()).willReturn(expectedResult);

            // When
            Object result = sut.around(joinPoint, crawlMetric);

            // Then
            assertThat(result).isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("[성공] null 반환값도 그대로 반환한다")
        void shouldReturnNullWhenProceedReturnsNull() throws Throwable {
            // Given
            given(crawlMetric.value()).willReturn("crawl_task");
            given(crawlMetric.operation()).willReturn("trigger");
            given(joinPoint.proceed()).willReturn(null);

            // When
            Object result = sut.around(joinPoint, crawlMetric);

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
            given(crawlMetric.value()).willReturn("crawl_task");
            given(crawlMetric.operation()).willReturn("execute");
            RuntimeException exception = new RuntimeException("처리 실패");
            given(joinPoint.proceed()).willThrow(exception);

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, crawlMetric))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("처리 실패");

            Timer errorTimer =
                    meterRegistry
                            .find("crawlinghub.crawl_task_duration_seconds")
                            .tags("operation", "execute", "outcome", "error")
                            .timer();
            Counter errorCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_total")
                            .tags("operation", "execute", "outcome", "error")
                            .counter();
            Counter errorsTotal =
                    meterRegistry
                            .find("crawlinghub.crawl_task_errors_total")
                            .tags("operation", "execute", "exception", "RuntimeException")
                            .counter();

            assertThat(errorTimer).isNotNull();
            assertThat(errorTimer.count()).isEqualTo(1);
            assertThat(errorCounter).isNotNull();
            assertThat(errorCounter.count()).isEqualTo(1.0);
            assertThat(errorsTotal).isNotNull();
            assertThat(errorsTotal.count()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("[실패] 예외 발생 시 success 카운터는 기록되지 않는다")
        void shouldNotRecordSuccessCounterOnException() throws Throwable {
            // Given
            given(crawlMetric.value()).willReturn("crawl_task");
            given(crawlMetric.operation()).willReturn("execute");
            given(joinPoint.proceed()).willThrow(new RuntimeException("오류"));

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, crawlMetric))
                    .isInstanceOf(RuntimeException.class);

            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.crawl_task_total")
                            .tags("operation", "execute", "outcome", "success")
                            .counter();
            assertThat(successCounter).isNull();
        }
    }
}
