package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * COOLDOWN 만료 UserAgent 복구 UseCase
 *
 * <p>COOLDOWN 상태이면서 cooldownUntil이 현재 시각 이전인 UserAgent를 IDLE 또는 SESSION_REQUIRED 상태로 복구합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverCooldownUserAgentUseCase {

    /**
     * COOLDOWN 만료 UserAgent 일괄 복구
     *
     * @return 복구된 UserAgent 건수
     */
    int execute();
}
