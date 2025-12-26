package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * Circuit Breaker 리셋 UseCase
 *
 * <p>Circuit Breaker를 강제로 리셋합니다. SUSPENDED 상태의 UserAgent들을 복구하여 CLOSED 상태로 전환합니다.
 *
 * <p><strong>동작</strong>:
 *
 * <ul>
 *   <li>복구 가능한 모든 SUSPENDED UserAgent 복구 (시간 조건 무시)
 *   <li>가용률 임계치 이상이 되면 CLOSED 상태로 전환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ResetCircuitBreakerUseCase {

    /**
     * Circuit Breaker 강제 리셋
     *
     * @return 복구된 UserAgent 수
     */
    int execute();
}
