package com.ryuqq.crawlinghub.application.token.port;

/**
 * Circuit Breaker Port (Outbound)
 *
 * @author crawlinghub
 */
public interface CircuitBreakerPort {

    /**
     * Circuit Breaker OPEN 상태 확인
     *
     * @param userAgentId User-Agent ID
     * @return OPEN 상태 여부
     */
    boolean isOpen(Long userAgentId);

    /**
     * 요청 허용 여부 확인 (상태별 로직 적용)
     * - CLOSED: 항상 허용 (return true)
     * - OPEN: timeout 체크 후 HALF_OPEN 전이 또는 차단
     * - HALF_OPEN: 테스트 요청 1개만 허용 (consecutive_successes == 0)
     *
     * @param userAgentId User-Agent ID
     * @return 요청 허용 여부
     */
    boolean allowRequest(Long userAgentId);

    /**
     * 성공 기록
     * - CLOSED: 실패 카운터 리셋
     * - HALF_OPEN: 성공 카운트 증가, 임계값 도달 시 CLOSED 전환
     *
     * @param userAgentId User-Agent ID
     */
    void recordSuccess(Long userAgentId);

    /**
     * 실패 기록 (429 에러 등)
     * - CLOSED: 실패 카운트 증가, 임계값 도달 시 OPEN 전환
     * - HALF_OPEN: 즉시 OPEN으로 재전환
     *
     * @param userAgentId User-Agent ID
     */
    void recordFailure(Long userAgentId);

    /**
     * Circuit Breaker 수동 리셋 (관리자 기능)
     * - 모든 카운터 리셋
     * - CLOSED 상태로 전환
     *
     * @param userAgentId User-Agent ID
     * @param reason 리셋 사유
     */
    void reset(Long userAgentId, String reason);
}
