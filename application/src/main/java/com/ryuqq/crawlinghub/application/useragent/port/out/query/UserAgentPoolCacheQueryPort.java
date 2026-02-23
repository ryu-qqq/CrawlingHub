package com.ryuqq.crawlinghub.application.useragent.port.out.query;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.List;
import java.util.Optional;

/**
 * UserAgent Pool Cache Query Port (Redis 조회)
 *
 * <p>Redis 기반 UserAgent Pool 조회를 위한 Port입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface UserAgentPoolCacheQueryPort {

    /**
     * Pool 통계 조회
     *
     * @return Pool 통계
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
     * 세션 발급 필요한 UserAgent ID 목록 조회
     *
     * @return 세션 필요 UserAgent ID 목록
     */
    List<UserAgentId> getSessionRequiredUserAgents();

    /**
     * 복구 대상 UserAgent ID 목록 조회
     *
     * @return 복구 대상 UserAgent ID 목록
     */
    List<UserAgentId> getRecoverableUserAgents();

    /**
     * 세션 만료 임박 UserAgent ID 목록 조회
     *
     * @param bufferMinutes 만료까지 남은 시간 (분)
     * @return 세션 갱신 필요한 UserAgent ID 목록
     */
    List<UserAgentId> getSessionExpiringUserAgents(int bufferMinutes);

    /**
     * 모든 SUSPENDED 상태의 UserAgent ID 목록 조회
     *
     * @return SUSPENDED 상태 UserAgent ID 목록
     */
    List<UserAgentId> getAllSuspendedUserAgents();

    /**
     * Pool에 등록된 모든 UserAgent ID 조회 (IDLE + SUSPENDED)
     *
     * <p>Cache→DB 동기화 시 전체 캐시 상태를 DB에 반영하기 위해 사용합니다.
     *
     * @return Pool 전체 UserAgent ID 목록
     */
    List<UserAgentId> getAllUserAgentIds();
}
