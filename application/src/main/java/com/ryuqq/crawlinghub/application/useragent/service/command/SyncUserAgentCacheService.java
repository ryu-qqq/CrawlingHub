package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.SyncUserAgentCacheUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UserAgent Cache→DB 동기화 Service
 *
 * <p>Redis 캐시에 저장된 Health Score, Status를 DB에 반영합니다.
 *
 * <p><strong>동기화 로직</strong>:
 *
 * <ol>
 *   <li>Redis에서 전체 UserAgent ID 조회 (IDLE + SUSPENDED sets)
 *   <li>각 ID별 CachedUserAgent 조회 (Redis) → healthScore, status 추출
 *   <li>DB UserAgent 배치 로드
 *   <li>Redis 상태와 DB 상태 비교, 차이 있으면 도메인 메서드로 동기화
 *   <li>배치 저장
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SyncUserAgentCacheService implements SyncUserAgentCacheUseCase {

    private static final Logger log = LoggerFactory.getLogger(SyncUserAgentCacheService.class);

    private final UserAgentPoolCacheQueryManager cacheQueryManager;
    private final UserAgentReadManager readManager;
    private final UserAgentCommandManager commandManager;

    public SyncUserAgentCacheService(
            UserAgentPoolCacheQueryManager cacheQueryManager,
            UserAgentReadManager readManager,
            UserAgentCommandManager commandManager) {
        this.cacheQueryManager = cacheQueryManager;
        this.readManager = readManager;
        this.commandManager = commandManager;
    }

    @Override
    public int execute() {
        List<UserAgentId> cachedIds = cacheQueryManager.getAllUserAgentIds();
        if (cachedIds.isEmpty()) {
            log.debug("동기화 대상 없음: Redis에 캐시된 UserAgent가 없습니다.");
            return 0;
        }

        Map<Long, CachedUserAgent> cachedMap =
                cachedIds.stream()
                        .map(id -> cacheQueryManager.findById(id).orElse(null))
                        .filter(cached -> cached != null)
                        .collect(
                                Collectors.toMap(
                                        CachedUserAgent::userAgentId, Function.identity()));

        if (cachedMap.isEmpty()) {
            return 0;
        }

        List<UserAgent> dbUserAgents = readManager.findByIds(cachedIds);
        Map<Long, UserAgent> dbMap =
                dbUserAgents.stream()
                        .collect(Collectors.toMap(ua -> ua.getIdValue(), Function.identity()));

        List<UserAgent> toUpdate = new ArrayList<>();
        Instant now = Instant.now();

        for (Map.Entry<Long, CachedUserAgent> entry : cachedMap.entrySet()) {
            Long id = entry.getKey();
            CachedUserAgent cached = entry.getValue();
            UserAgent dbUserAgent = dbMap.get(id);

            if (dbUserAgent == null) {
                log.warn("DB에 없는 UserAgent: id={}", id);
                continue;
            }

            if (needsSync(cached, dbUserAgent)) {
                syncState(cached, dbUserAgent, now);
                toUpdate.add(dbUserAgent);
            }
        }

        if (!toUpdate.isEmpty()) {
            commandManager.persistAll(toUpdate);
        }

        log.info("Cache→DB 동기화: 전체={}, 변경={}건", cachedMap.size(), toUpdate.size());
        return toUpdate.size();
    }

    private boolean needsSync(CachedUserAgent cached, UserAgent dbUserAgent) {
        boolean healthDiff = cached.healthScore() != dbUserAgent.getHealthScoreValue();
        boolean statusDiff = cached.status() != dbUserAgent.getStatus();
        return healthDiff || statusDiff;
    }

    private void syncState(CachedUserAgent cached, UserAgent dbUserAgent, Instant now) {
        if (cached.status().isSuspended() && !dbUserAgent.isSuspended()) {
            dbUserAgent.recordFailure(0, now);
        } else if (cached.status().isAvailable() && dbUserAgent.isSuspended()) {
            dbUserAgent.recover(now);
        }
    }
}
