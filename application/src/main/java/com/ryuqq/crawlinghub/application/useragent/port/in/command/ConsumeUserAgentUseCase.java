package com.ryuqq.crawlinghub.application.useragent.port.in.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;

/**
 * UserAgent 토큰 소비 UseCase
 *
 * <p>Redis Pool에서 사용 가능한 UserAgent를 선택하고 토큰을 소비합니다.
 *
 * <p><strong>동작 흐름</strong>:
 *
 * <ol>
 *   <li>Circuit Breaker 체크 (가용률 < 20% 시 예외)
 *   <li>Redis에서 tokens > 0인 UserAgent 선택 (Lua Script)
 *   <li>tokens-- (atomic decrement)
 *   <li>최초 사용 시 windowEnd = now + 1h 설정
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface ConsumeUserAgentUseCase {

    /**
     * UserAgent 토큰 소비
     *
     * @return 선택된 CachedUserAgent (토큰 정보 포함)
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException 사용 가능한
     *     UserAgent가 없을 때
     * @throws com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException 가용률 <
     *     20%일 때
     */
    CachedUserAgent execute();
}
