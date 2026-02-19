package com.ryuqq.crawlinghub.application.useragent.service.query;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse;
import com.ryuqq.crawlinghub.application.useragent.dto.response.UserAgentDetailResponse.PoolInfo;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.query.GetUserAgentByIdUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * 개별 UserAgent 상세 조회 Service
 *
 * <p>{@link GetUserAgentByIdUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetUserAgentByIdService implements GetUserAgentByIdUseCase {

    private final UserAgentReadManager readManager;
    private final UserAgentPoolCacheManager cacheManager;
    private final TimeProvider timeProvider;

    public GetUserAgentByIdService(
            UserAgentReadManager readManager,
            UserAgentPoolCacheManager cacheManager,
            TimeProvider timeProvider) {
        this.readManager = readManager;
        this.cacheManager = cacheManager;
        this.timeProvider = timeProvider;
    }

    @Override
    public UserAgentDetailResponse execute(long userAgentId) {
        UserAgentId id = UserAgentId.of(userAgentId);

        // 1. DB에서 UserAgent 조회
        UserAgent userAgent =
                readManager.findById(id).orElseThrow(() -> new UserAgentNotFoundException(id));

        // 2. Redis Pool에서 캐시 정보 조회
        PoolInfo poolInfo =
                cacheManager.findById(id).map(this::toPoolInfo).orElse(PoolInfo.notInPool());

        // 3. Response 생성
        return UserAgentDetailResponse.withPoolInfo(
                userAgent.getId().value(),
                userAgent.getUserAgentString().value(),
                userAgent.getDeviceType(),
                userAgent.getStatus(),
                userAgent.getHealthScoreValue(),
                userAgent.getRequestsPerDay(),
                userAgent.getLastUsedAt(),
                userAgent.getCreatedAt(),
                userAgent.getUpdatedAt(),
                poolInfo);
    }

    private PoolInfo toPoolInfo(CachedUserAgent cached) {
        Instant now = timeProvider.now();
        boolean hasValidSession = cached.hasValidSession(now);
        return PoolInfo.of(cached.remainingTokens(), hasValidSession, cached.sessionExpiresAt());
    }
}
