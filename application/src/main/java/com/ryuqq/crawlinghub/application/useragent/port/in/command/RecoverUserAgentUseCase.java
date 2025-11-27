package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * UserAgent 복구 UseCase
 *
 * <p>SUSPENDED 상태의 UserAgent를 복구합니다.
 *
 * <p><strong>복구 조건</strong>:
 *
 * <ul>
 *   <li>SUSPENDED 상태
 *   <li>1시간 경과
 *   <li>Health Score ≥ 30
 * </ul>
 *
 * <p><strong>복구 동작</strong>:
 *
 * <ul>
 *   <li>Health Score → 70 리셋
 *   <li>tokens → 80 리셋
 *   <li>상태 → AVAILABLE
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverUserAgentUseCase {

    /**
     * 조건을 만족하는 모든 SUSPENDED UserAgent 복구
     *
     * @return 복구된 UserAgent 수
     */
    int recoverAll();
}
