package com.ryuqq.crawlinghub.application.useragent.port.in.command;

/**
 * UserAgent Cache→DB 동기화 UseCase
 *
 * <p>Redis 캐시에 저장된 UserAgent 상태(Health Score, Status)를 DB에 주기적으로 동기화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SyncUserAgentCacheUseCase {

    /**
     * Redis 캐시 상태를 DB에 동기화
     *
     * @return 동기화된 UserAgent 건수
     */
    int execute();
}
