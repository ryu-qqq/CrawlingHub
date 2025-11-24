package com.ryuqq.crawlinghub.application.seller.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Seller Event Metrics Recorder
 *
 * <p>Seller 관련 이벤트 메트릭 수집 전담 컴포넌트
 *
 * <h3>수집 메트릭</h3>
 *
 * <ul>
 *   <li>seller.deactivated.event.handled: 비활성화 이벤트 처리 횟수
 *   <li>seller.deactivated.event.duration: 비활성화 이벤트 처리 시간
 *   <li>seller.deactivated.schedulers.count: 비활성화된 Scheduler 수 (sellerId별)
 * </ul>
 *
 * <h3>장점</h3>
 *
 * <ul>
 *   <li>재사용성: 여러 Handler에서 동일 메트릭 수집
 *   <li>테스트 용이성: Mock으로 메트릭 수집 검증
 *   <li>관심사 분리: Handler는 비즈니스 로직만, Metrics는 측정만
 *   <li>타입 안전성: 메서드 시그니처로 파라미터 타입 보장
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SellerEventMetrics {

    private final Counter deactivationEventCounter;
    private final Timer deactivationEventTimer;
    private final MeterRegistry meterRegistry;

    public SellerEventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // 이벤트 처리 횟수
        this.deactivationEventCounter =
                Counter.builder("seller.deactivated.event.handled")
                        .description("Number of seller deactivation events handled")
                        .register(meterRegistry);

        // 이벤트 처리 시간
        this.deactivationEventTimer =
                Timer.builder("seller.deactivated.event.duration")
                        .description("Duration of seller deactivation event handling")
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .register(meterRegistry);
    }

    /**
     * 비활성화 이벤트 처리 메트릭 기록
     *
     * <p>횟수와 처리 시간을 자동으로 측정
     *
     * <h3>사용 예시</h3>
     *
     * <pre>{@code
     * sellerEventMetrics.recordDeactivationEvent(() -> {
     *     // 비즈니스 로직
     *     deactivateSchedulers(sellerId);
     * });
     * }</pre>
     *
     * @param action 측정할 비즈니스 로직
     */
    public void recordDeactivationEvent(Runnable action) {
        deactivationEventCounter.increment();
        deactivationEventTimer.record(action);
    }

    /**
     * 비활성화된 Scheduler 수 기록
     *
     * <p>동적 태그를 사용하여 Seller별 메트릭 기록
     *
     * <p>Cardinality Explosion 방지: MeterRegistry의 내부 캐싱 활용
     *
     * <h3>Prometheus 쿼리 예시</h3>
     *
     * <pre>{@code
     * # Seller별 비활성화된 Scheduler 수 (Top 10)
     * topk(10, seller_deactivated_schedulers_count{seller_id!=""})
     *
     * # 특정 Seller의 누적 비활성화 수
     * seller_deactivated_schedulers_count{seller_id="123"}
     * }</pre>
     *
     * @param sellerId Seller ID
     * @param count 비활성화된 Scheduler 수
     */
    public void recordSchedulersDeactivated(Long sellerId, long count) {
        // 동적 태그로 Seller별 Counter 생성 (MeterRegistry가 내부적으로 캐싱)
        Counter.builder("seller.deactivated.schedulers.count")
                .description("Number of schedulers deactivated per seller")
                .tag("seller_id", String.valueOf(sellerId))
                .register(meterRegistry)
                .increment(count);
    }
}
