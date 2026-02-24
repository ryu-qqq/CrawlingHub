package com.ryuqq.crawlinghub.application.common.metric.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ryuqq.crawlinghub.application.common.metric.CrawlHubMetrics;
import com.ryuqq.crawlinghub.application.common.metric.annotation.OutboundClientMetric;
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
 * OutboundClientMetricAspect 단위 테스트
 *
 * <p>SimpleMeterRegistry로 실제 메트릭 기록 동작을 검증합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Tag("unit")
@Tag("application")
@ExtendWith(MockitoExtension.class)
@DisplayName("OutboundClientMetricAspect 단위 테스트")
class OutboundClientMetricAspectTest {

    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private OutboundClientMetric outboundClientMetric;

    private MeterRegistry meterRegistry;
    private CrawlHubMetrics metrics;
    private OutboundClientMetricAspect sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new CrawlHubMetrics(meterRegistry);
        sut = new OutboundClientMetricAspect(metrics);
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 성공 케이스")
    class AroundSuccessTest {

        @Test
        @DisplayName("[성공] 정상 처리 시 success 카운터와 타이머를 기록한다")
        void shouldRecordSuccessMetricsWhenProceedSucceeds() throws Throwable {
            // Given
            given(outboundClientMetric.system()).willReturn("marketplace");
            given(outboundClientMetric.operation()).willReturn("fetchProduct");
            given(joinPoint.proceed()).willReturn("response");

            // When
            Object result = sut.around(joinPoint, outboundClientMetric);

            // Then
            assertThat(result).isEqualTo("response");

            Timer timer =
                    meterRegistry
                            .find("crawlinghub.outbound_client_duration_seconds")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "outcome", "success")
                            .timer();
            Counter counter =
                    meterRegistry
                            .find("crawlinghub.outbound_client_total")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "outcome", "success")
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
            given(outboundClientMetric.system()).willReturn("sqs");
            given(outboundClientMetric.operation()).willReturn("sendMessage");
            given(joinPoint.proceed()).willReturn(Boolean.TRUE);

            // When
            Object result = sut.around(joinPoint, outboundClientMetric);

            // Then
            assertThat(result).isEqualTo(Boolean.TRUE);
        }
    }

    @Nested
    @DisplayName("around() 메서드 테스트 - 실패 케이스")
    class AroundFailureTest {

        @Test
        @DisplayName("[실패] 예외 발생 시 error 카운터와 타이머를 기록하고 예외를 전파한다")
        void shouldRecordErrorMetricsAndRethrowException() throws Throwable {
            // Given
            given(outboundClientMetric.system()).willReturn("marketplace");
            given(outboundClientMetric.operation()).willReturn("fetchProduct");
            RuntimeException exception = new RuntimeException("외부 클라이언트 오류");
            given(joinPoint.proceed()).willThrow(exception);

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, outboundClientMetric))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("외부 클라이언트 오류");

            Timer errorTimer =
                    meterRegistry
                            .find("crawlinghub.outbound_client_duration_seconds")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "outcome", "error")
                            .timer();
            Counter errorCounter =
                    meterRegistry
                            .find("crawlinghub.outbound_client_total")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "outcome", "error")
                            .counter();
            Counter errorsTotal =
                    meterRegistry
                            .find("crawlinghub.outbound_client_errors_total")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "exception", "RuntimeException")
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
            given(outboundClientMetric.system()).willReturn("marketplace");
            given(outboundClientMetric.operation()).willReturn("fetchProduct");
            given(joinPoint.proceed()).willThrow(new RuntimeException("오류"));

            // When & Then
            assertThatThrownBy(() -> sut.around(joinPoint, outboundClientMetric))
                    .isInstanceOf(RuntimeException.class);

            Counter successCounter =
                    meterRegistry
                            .find("crawlinghub.outbound_client_total")
                            .tags(
                                    "system", "marketplace",
                                    "operation", "fetchProduct",
                                    "outcome", "success")
                            .counter();
            assertThat(successCounter).isNull();
        }
    }
}
