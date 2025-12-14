package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.port.out.cache.UserAgentPoolCachePort;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Cache Manager
 *
 * <p><strong>책임</strong>: Redis Pool 캐시 작업 위임
 *
 * <p><strong>규칙</strong>: 단일 CachePort만 의존
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolCacheManager {

    private final UserAgentPoolCachePort cachePort;

    public UserAgentPoolCacheManager(UserAgentPoolCachePort cachePort) {
        this.cachePort = cachePort;
    }

    /**
     * 토큰 소비
     *
     * @return 선택된 CachedUserAgent (Optional)
     */
    public Optional<CachedUserAgent> consumeToken() {
        return cachePort.consumeToken();
    }

    /**
     * 성공 기록
     *
     * @param userAgentId UserAgent ID
     */
    public void recordSuccess(UserAgentId userAgentId) {
        cachePort.recordSuccess(userAgentId);
    }

    /**
     * 실패 기록
     *
     * @param userAgentId UserAgent ID
     * @param httpStatusCode HTTP 상태 코드
     * @return SUSPENDED 여부
     */
    public boolean recordFailure(UserAgentId userAgentId, int httpStatusCode) {
        return cachePort.recordFailure(userAgentId, httpStatusCode);
    }

    /**
     * 세션 만료 처리
     *
     * @param userAgentId UserAgent ID
     */
    public void expireSession(UserAgentId userAgentId) {
        cachePort.expireSession(userAgentId);
    }

    /**
     * Pool에서 제거
     *
     * @param userAgentId UserAgent ID
     */
    public void removeFromPool(UserAgentId userAgentId) {
        cachePort.removeFromPool(userAgentId);
    }

    /**
     * Pool 통계 조회
     *
     * @return Pool 통계
     */
    public PoolStats getPoolStats() {
        return cachePort.getPoolStats();
    }

    /**
     * 복구 가능한 UserAgent 목록 조회
     *
     * @return 복구 가능한 UserAgent ID 목록
     */
    public List<UserAgentId> getRecoverableUserAgents() {
        return cachePort.getRecoverableUserAgents();
    }

    /**
     * Pool에 복원
     *
     * @param userAgentId UserAgent ID
     * @param userAgentValue UserAgent 문자열
     */
    public void restoreToPool(UserAgentId userAgentId, String userAgentValue) {
        cachePort.restoreToPool(userAgentId, userAgentValue);
    }
}
