package com.ryuqq.crawlinghub.application.useragent.port.out.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * UserAgent Pool Cache Command Port (Redis 쓰기)
 *
 * <p>Redis 기반 UserAgent Pool 상태 변경을 위한 Port입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentPoolCacheCommandPort {

    /**
     * 토큰 소비 (Lua Script - atomic)
     *
     * <p>IDLE 상태 + tokens > 0인 UserAgent를 선택하고 토큰을 차감합니다.
     *
     * @return 선택된 UserAgent (없으면 empty)
     */
    Optional<CachedUserAgent> consumeToken();

    /**
     * Pool에 UserAgent 추가
     *
     * @param cachedUserAgent 추가할 UserAgent
     */
    void addToPool(CachedUserAgent cachedUserAgent);

    /**
     * Pool에서 UserAgent 제거 (SUSPENDED로 전환)
     *
     * @param userAgentId UserAgent ID
     */
    void removeFromPool(UserAgentId userAgentId);

    /**
     * 세션 정보 업데이트
     *
     * @param userAgentId UserAgent ID
     * @param sessionToken 발급받은 세션 토큰
     * @param nid nid 쿠키 값
     * @param mustitUid mustit_uid 쿠키 값
     * @param sessionExpiresAt 세션 만료 시간
     */
    void updateSession(
            UserAgentId userAgentId,
            String sessionToken,
            String nid,
            String mustitUid,
            Instant sessionExpiresAt);

    /**
     * 세션 만료 처리
     *
     * @param userAgentId UserAgent ID
     */
    void expireSession(UserAgentId userAgentId);

    /**
     * Pool에 UserAgent 복구
     *
     * @param userAgentId UserAgent ID
     * @param userAgentValue User-Agent 헤더 값
     */
    void restoreToPool(UserAgentId userAgentId, String userAgentValue);

    /**
     * Rate Limit (429) 발생 시 세션 만료 + SUSPENDED 전환을 원자적으로 수행 (Lua Script)
     *
     * @param userAgentId UserAgent ID
     */
    void suspendForRateLimit(UserAgentId userAgentId);

    /** Pool 초기화 (모든 UserAgent 삭제) */
    void clearPool();

    /**
     * Pool에 여러 UserAgent 추가 (WarmUp)
     *
     * @param cachedUserAgents 추가할 UserAgent 목록
     * @return 추가된 개수
     */
    int warmUp(List<CachedUserAgent> cachedUserAgents);

    /**
     * UserAgent borrow (IDLE -> BORROWED, Lua Script atomic)
     *
     * <p>IDLE 상태 + tokens > 0인 UserAgent를 선택하고 BORROWED 상태로 전환합니다.
     *
     * @return borrow된 UserAgent (없으면 empty)
     */
    Optional<CachedUserAgent> borrow();

    /**
     * UserAgent 반납 (BORROWED -> IDLE/COOLDOWN/SUSPENDED, Lua Script atomic)
     *
     * <p>크롤링 결과에 따라 상태를 전환합니다.
     *
     * <ul>
     *   <li>return 0: IDLE로 복귀
     *   <li>return 1: COOLDOWN으로 전환
     *   <li>return 2: SUSPENDED로 전환
     * </ul>
     *
     * @param userAgentId UserAgent ID
     * @param success 성공 여부
     * @param httpStatusCode HTTP 상태 코드
     * @param healthDelta Health Score 변경량
     * @param cooldownUntil COOLDOWN 만료 시각 (epoch millis, nullable)
     * @param consecutiveRateLimits 연속 429 횟수
     * @return 전환된 상태 코드 (0: IDLE, 1: COOLDOWN, 2: SUSPENDED)
     */
    int returnAgent(
            long userAgentId,
            boolean success,
            int httpStatusCode,
            int healthDelta,
            Long cooldownUntil,
            int consecutiveRateLimits);

    /**
     * COOLDOWN 만료 일괄 복구 (Housekeeper용)
     *
     * <p>COOLDOWN 상태이면서 cooldownUntil이 현재 시각 이전인 UserAgent를 IDLE 또는 SESSION_REQUIRED로 일괄 전환합니다.
     *
     * @return 복구된 UserAgent 수
     */
    int recoverExpiredCooldowns();

    /**
     * Leak Detection (BORROWED 상태 오래된 것)
     *
     * <p>BORROWED 상태로 지정된 시간 이상 머문 UserAgent ID 목록을 반환합니다.
     *
     * @param leakThresholdMillis Leak 판정 기준 시간 (밀리초)
     * @return Leak된 UserAgent ID 목록
     */
    List<Long> detectLeakedAgents(long leakThresholdMillis);

    /**
     * Pool WarmUp 완료 여부 확인
     *
     * @return WarmUp 완료 플래그가 설정되어 있으면 true
     */
    boolean isPoolInitialized();

    /**
     * Pool WarmUp 완료 플래그 설정
     *
     * <p>WarmUp 성공 후 호출하여 다른 인스턴스의 중복 WarmUp을 방지합니다.
     */
    void markPoolInitialized();

    /**
     * WarmUp 분산 락 획득 시도 (SETNX)
     *
     * <p>여러 ECS 인스턴스 중 최초 1개만 WarmUp을 실행하도록 보장합니다.
     *
     * @return 락 획득 성공 시 true
     */
    boolean tryAcquireWarmUpLock();
}
