package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UserAgent Pool Warm-up Service
 *
 * <p>{@link WarmUpUserAgentUseCase} 구현체
 *
 * <p><strong>Lazy Token Issuance 전략</strong>:
 *
 * <ul>
 *   <li>DB에서 AVAILABLE 상태의 UserAgent를 조회
 *   <li>세션 없이 Redis Pool에 AVAILABLE 상태로 추가
 *   <li>세션은 토큰 소비 시점에 Lazy 발급
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class WarmUpUserAgentService implements WarmUpUserAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(WarmUpUserAgentService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentPoolCacheManager cacheManager;

    public WarmUpUserAgentService(
            UserAgentReadManager readManager, UserAgentPoolCacheManager cacheManager) {
        this.readManager = readManager;
        this.cacheManager = cacheManager;
    }

    @Override
    public int warmUp() {
        log.info("UserAgent Pool Warm-up 시작 (Lazy Token Issuance 전략)");

        List<UserAgent> availableUserAgents = readManager.findAllAvailable();

        if (availableUserAgents.isEmpty()) {
            log.warn("Warm-up 대상 UserAgent 없음");
            return 0;
        }

        List<CachedUserAgent> cachedUserAgents =
                availableUserAgents.stream().map(CachedUserAgent::forNew).toList();

        int addedCount = cacheManager.warmUp(cachedUserAgents);

        log.info("UserAgent Pool Warm-up 완료: {} 건 추가됨 (세션은 소비 시점에 Lazy 발급)", addedCount);

        return addedCount;
    }
}
