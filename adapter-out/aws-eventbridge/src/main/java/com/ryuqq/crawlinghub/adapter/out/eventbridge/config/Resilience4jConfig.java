package com.ryuqq.crawlinghub.adapter.out.eventbridge.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 설정
 * <p>
 * EventBridge Executor에 적용할 Circuit Breaker 및 Retry 정책을 정의합니다.
 * </p>
 * <p>
 * Circuit Breaker 전략:
 * <ul>
 *   <li>Sliding Window: 10개 요청 기준</li>
 *   <li>Failure Rate: 50% 이상 실패 시 OPEN</li>
 *   <li>Wait Duration: 60초 후 HALF_OPEN</li>
 *   <li>Slow Call: 5초 이상 걸리면 slow로 간주</li>
 * </ul>
 * </p>
 * <p>
 * Retry 전략:
 * <ul>
 *   <li>Max Attempts: 0 (Orchestrator가 retry 관리)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Configuration
public class Resilience4jConfig {

    /**
     * CircuitBreaker Registry Bean
     * <p>
     * EventBridge용 Circuit Breaker 정책을 등록합니다.
     * </p>
     *
     * @return CircuitBreakerRegistry
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                // Sliding Window 방식: 10개 요청 기준
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                // 실패율 50% 이상 → OPEN
                .failureRateThreshold(50.0f)
                // OPEN → HALF_OPEN 전환 대기 시간
                .waitDurationInOpenState(Duration.ofSeconds(60))
                // HALF_OPEN 상태에서 테스트할 요청 수
                .permittedNumberOfCallsInHalfOpenState(3)
                // Slow Call 기준: 5초 이상
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .slowCallRateThreshold(50.0f)
                // 최소 요청 수: 5개 이상부터 통계 계산
                .minimumNumberOfCalls(5)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        // "eventbridge" 이름으로 Circuit Breaker 등록
        registry.circuitBreaker("eventbridge");

        return registry;
    }

    /**
     * Retry Registry Bean
     * <p>
     * Orchestrator가 retry를 관리하므로 maxAttempts=0으로 설정합니다.
     * </p>
     *
     * @return RetryRegistry
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(0)  // Orchestrator가 retry 관리
                .build();

        RetryRegistry registry = RetryRegistry.of(config);

        // "eventbridge" 이름으로 Retry 등록
        registry.retry("eventbridge");

        return registry;
    }
}
