package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.WarmUpUserAgentPoolUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UserAgent Pool WarmUp Service
 *
 * <p>DB에서 AVAILABLE 상태의 UserAgent를 조회하여 Redis Pool에 SESSION_REQUIRED 상태로 등록합니다.
 *
 * <p>분산 환경(ECS 다중 인스턴스)에서 Redis SETNX 기반으로 최초 1회만 실행을 보장합니다.
 *
 * <p><strong>WarmUp 흐름</strong>:
 *
 * <ol>
 *   <li>Redis에 WarmUp 완료 플래그 확인 (이미 완료 → skip)
 *   <li>Redis Pool에 데이터 존재 여부 확인 (이미 있음 → skip + 플래그 설정)
 *   <li>SETNX로 분산 락 획득 시도 (다른 인스턴스가 진행 중 → skip)
 *   <li>DB에서 AVAILABLE UserAgent 조회
 *   <li>Redis Pool에 SESSION_REQUIRED 상태로 일괄 등록
 *   <li>WarmUp 완료 플래그 설정
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class WarmUpUserAgentPoolService implements WarmUpUserAgentPoolUseCase {

    private static final Logger log = LoggerFactory.getLogger(WarmUpUserAgentPoolService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentPoolCacheCommandManager cacheCommandManager;

    public WarmUpUserAgentPoolService(
            UserAgentReadManager readManager,
            UserAgentPoolCacheCommandManager cacheCommandManager) {
        this.readManager = readManager;
        this.cacheCommandManager = cacheCommandManager;
    }

    @Override
    public int execute() {
        if (cacheCommandManager.isPoolInitialized()) {
            log.info("[WarmUp] 이미 초기화 완료 → skip");
            return 0;
        }

        if (!cacheCommandManager.tryAcquireWarmUpLock()) {
            log.info("[WarmUp] 다른 인스턴스가 진행 중 → skip");
            return 0;
        }

        try {
            List<UserAgent> availableAgents = readManager.findAllAvailable();
            if (availableAgents.isEmpty()) {
                log.warn("[WarmUp] DB에 AVAILABLE UserAgent 없음");
                cacheCommandManager.markPoolInitialized();
                return 0;
            }

            List<CachedUserAgent> cachedAgents =
                    availableAgents.stream().map(CachedUserAgent::forNew).toList();

            int warmedUp = cacheCommandManager.warmUp(cachedAgents);
            cacheCommandManager.markPoolInitialized();

            log.info(
                    "[WarmUp] 완료: DB {}건 → Redis Pool {}건 (SESSION_REQUIRED)",
                    availableAgents.size(),
                    warmedUp);
            return warmedUp;
        } catch (Exception e) {
            log.error("[WarmUp] 실패", e);
            return 0;
        }
    }
}
