package com.ryuqq.crawlinghub.adapter.redis.circuit;

import com.ryuqq.crawlinghub.application.token.port.CircuitBreakerPort;
import org.springframework.stereotype.Component;

/**
 * Circuit Breaker Adapter
 * <p>
 * Redis 기반 Circuit Breaker 패턴
 * - 외부 API 장애 격리
 * </p>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
@Component
public class CircuitBreakerAdapter implements CircuitBreakerPort {

    private final CircuitBreakerManager circuitBreakerManager;

    public CircuitBreakerAdapter(CircuitBreakerManager circuitBreakerManager) {
        this.circuitBreakerManager = circuitBreakerManager;
    }

    /**
     * Circuit Breaker 열림 여부 확인
     *
     * @param identifier 식별자 (User-Agent String)
     * @return 열려있으면 true
     */
    @Override
    public boolean isOpen(String identifier) {
        // identifier(UserAgent String)를 키로 사용
        // 간단하게 hashCode를 Long으로 변환
        Long key = (long) identifier.hashCode();
        CircuitBreakerManager.CircuitState state = circuitBreakerManager.getState(key);
        return state.getStatus() == CircuitBreakerManager.CircuitStatus.OPEN;
    }

    /**
     * 성공 기록
     *
     * @param identifier 식별자
     */
    @Override
    public void recordSuccess(String identifier) {
        Long key = (long) identifier.hashCode();
        circuitBreakerManager.recordSuccess(key);
    }

    /**
     * 실패 기록
     *
     * @param identifier 식별자
     */
    @Override
    public void recordFailure(String identifier) {
        Long key = (long) identifier.hashCode();
        circuitBreakerManager.recordFailure(key);
    }
}
