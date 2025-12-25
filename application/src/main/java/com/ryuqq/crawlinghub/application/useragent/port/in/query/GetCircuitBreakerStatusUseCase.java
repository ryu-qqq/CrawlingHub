package com.ryuqq.crawlinghub.application.useragent.port.in.query;

import com.ryuqq.crawlinghub.application.useragent.dto.response.CircuitBreakerStatusResponse;

/**
 * Circuit Breaker 상태 조회 UseCase
 *
 * <p>UserAgent Pool의 Circuit Breaker 상태를 조회합니다.
 *
 * <p><strong>제공 정보</strong>:
 *
 * <ul>
 *   <li>현재 상태 (OPEN/CLOSED)
 *   <li>가용률 (%)
 *   <li>Threshold (%)
 *   <li>마지막 상태 변경 시각
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetCircuitBreakerStatusUseCase {

    /**
     * Circuit Breaker 상태 조회
     *
     * @return Circuit Breaker 상태 정보
     */
    CircuitBreakerStatusResponse execute();
}
