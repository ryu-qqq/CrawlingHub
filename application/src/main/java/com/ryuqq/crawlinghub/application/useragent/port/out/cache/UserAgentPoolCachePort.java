package com.ryuqq.crawlinghub.application.useragent.port.out.cache;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * UserAgent Pool Cache Port (Redis)
 *
 * <p>Redis 기반 UserAgent Pool 관리를 위한 Port입니다. 모든 토큰 관련 연산은 Lua Script로 원자성을 보장합니다.
 *
 * <p><strong>상태 흐름</strong>:
 *
 * <pre>
 * SESSION_REQUIRED (세션 필요)
 *       ↓ updateSession()
 *     READY (사용 가능)
 *       ↓ 429 또는 Health < 30
 *   SUSPENDED (일시 정지)
 *       ↓ restoreToPool()
 * SESSION_REQUIRED (복구)
 * </pre>
 *
 * <p><strong>토큰 제한</strong>:
 *
 * <ul>
 *   <li>각 UserAgent당 1시간에 80회 요청
 *   <li>Sliding Window: 최초 사용 시점 기준 1시간
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentPoolCachePort {

    /**
     * 토큰 소비 (Lua Script - atomic)
     *
     * <p><strong>동작</strong>:
     *
     * <ol>
     *   <li>READY 상태 + tokens > 0인 UserAgent 선택
     *   <li>tokens-- (atomic decrement)
     *   <li>최초 사용 시 windowEnd = now + 1h 설정
     * </ol>
     *
     * @return 선택된 UserAgent (없으면 empty)
     */
    Optional<CachedUserAgent> consumeToken();

    /**
     * 성공 기록 (Lua Script - atomic)
     *
     * <p>Health Score +5 (최대 100)
     *
     * @param userAgentId UserAgent ID
     */
    void recordSuccess(UserAgentId userAgentId);

    /**
     * 실패 기록 (Lua Script - atomic)
     *
     * <p><strong>동작</strong>:
     *
     * <ul>
     *   <li>5xx: Health Score -10
     *   <li>기타: Health Score -5
     *   <li>Health Score < 30: SUSPENDED 상태로 변경
     * </ul>
     *
     * <p>429는 별도 처리 (removeFromPool)
     *
     * @param userAgentId UserAgent ID
     * @param httpStatusCode HTTP 응답 상태 코드
     * @return SUSPENDED로 변경되었으면 true
     */
    boolean recordFailure(UserAgentId userAgentId, int httpStatusCode);

    /**
     * Pool에 UserAgent 추가
     *
     * <p>SESSION_REQUIRED 상태로 추가됨 (세션 발급 필요)
     *
     * @param cachedUserAgent 추가할 UserAgent
     */
    void addToPool(CachedUserAgent cachedUserAgent);

    /**
     * Pool에서 UserAgent 제거 (SUSPENDED로 전환)
     *
     * <p>429 응답 시 호출
     *
     * @param userAgentId UserAgent ID
     */
    void removeFromPool(UserAgentId userAgentId);

    /**
     * 세션 정보 업데이트
     *
     * <p>세션 발급 성공 시 호출하여 READY 상태로 전환
     *
     * @param userAgentId UserAgent ID
     * @param sessionToken 발급받은 세션 토큰
     * @param sessionExpiresAt 세션 만료 시간
     */
    void updateSession(UserAgentId userAgentId, String sessionToken, Instant sessionExpiresAt);

    /**
     * 세션 만료 처리
     *
     * <p>세션이 만료된 UserAgent를 SESSION_REQUIRED 상태로 변경
     *
     * @param userAgentId UserAgent ID
     */
    void expireSession(UserAgentId userAgentId);

    /**
     * Pool에 UserAgent 복구
     *
     * <p>Health Score 70, tokens 80 리셋, SESSION_REQUIRED 상태
     *
     * @param userAgentId UserAgent ID
     * @param userAgentValue User-Agent 헤더 값
     */
    void restoreToPool(UserAgentId userAgentId, String userAgentValue);

    /**
     * 세션 발급 필요한 UserAgent ID 목록 조회
     *
     * <p>SESSION_REQUIRED 상태인 UserAgent 목록
     *
     * @return 세션 필요 UserAgent ID 목록
     */
    List<UserAgentId> getSessionRequiredUserAgents();

    /**
     * 복구 대상 UserAgent ID 목록 조회
     *
     * <p><strong>복구 조건</strong>:
     *
     * <ul>
     *   <li>SUSPENDED 상태
     *   <li>1시간 경과
     *   <li>Health Score ≥ 30
     * </ul>
     *
     * @return 복구 대상 UserAgent ID 목록
     */
    List<UserAgentId> getRecoverableUserAgents();

    /**
     * 세션 만료 임박 UserAgent ID 목록 조회
     *
     * <p>READY 상태이면서 세션 만료까지 지정 시간 이내인 UserAgent 조회
     *
     * <p><strong>선제적 갱신 목적</strong>:
     *
     * <ul>
     *   <li>세션 만료 전에 미리 갱신하여 서비스 연속성 보장
     *   <li>세션 만료로 인한 크롤링 실패 방지
     * </ul>
     *
     * @param bufferMinutes 만료까지 남은 시간 (분)
     * @return 세션 갱신 필요한 UserAgent ID 목록
     */
    List<UserAgentId> getSessionExpiringUserAgents(int bufferMinutes);

    /**
     * Pool 통계 조회
     *
     * @return Pool 통계 (총 수, READY 수, SESSION_REQUIRED 수, SUSPENDED 수, Health Score 통계)
     */
    PoolStats getPoolStats();

    /**
     * 특정 UserAgent 조회
     *
     * @param userAgentId UserAgent ID
     * @return CachedUserAgent (없으면 empty)
     */
    Optional<CachedUserAgent> findById(UserAgentId userAgentId);

    /**
     * Pool 초기화 (모든 UserAgent 삭제)
     *
     * <p>테스트 또는 전체 리셋 용도
     */
    void clearPool();
}
