package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * SUSPENDED UserAgent 복구 UseCase
 *
 * <p>SUSPENDED 상태의 UserAgent를 SESSION_REQUIRED로 복구합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface RecoverSuspendedPoolUserAgentUseCase {

    /**
     * SUSPENDED UserAgent 일괄 복구
     *
     * @return 복구된 UserAgent 건수
     */
    int execute();
}
