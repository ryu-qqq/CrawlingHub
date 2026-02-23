package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand;
import com.ryuqq.crawlinghub.application.useragent.dto.command.IssueUserAgentSessionCommand.SessionIssueType;
import com.ryuqq.crawlinghub.application.useragent.dto.session.SessionToken;
import com.ryuqq.crawlinghub.application.useragent.manager.SessionDbStatusManager;
import com.ryuqq.crawlinghub.application.useragent.manager.SessionTokenManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.IssueUserAgentSessionUseCase;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UserAgent 세션 발급 서비스
 *
 * <p>세션 만료 임박 갱신(RENEW) 및 신규 세션 발급(NEW)을 수행합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class IssueUserAgentSessionService implements IssueUserAgentSessionUseCase {

    private static final Logger log = LoggerFactory.getLogger(IssueUserAgentSessionService.class);

    private final UserAgentPoolCacheQueryManager cacheQueryManager;
    private final UserAgentPoolCacheCommandManager cacheCommandManager;
    private final SessionTokenManager sessionTokenManager;
    private final SessionDbStatusManager dbStatusManager;

    public IssueUserAgentSessionService(
            UserAgentPoolCacheQueryManager cacheQueryManager,
            UserAgentPoolCacheCommandManager cacheCommandManager,
            SessionTokenManager sessionTokenManager,
            SessionDbStatusManager dbStatusManager) {
        this.cacheQueryManager = cacheQueryManager;
        this.cacheCommandManager = cacheCommandManager;
        this.sessionTokenManager = sessionTokenManager;
        this.dbStatusManager = dbStatusManager;
    }

    @Override
    public int execute(IssueUserAgentSessionCommand command) {
        List<UserAgentId> targetIds = resolveTargetIds(command);
        if (targetIds.isEmpty()) {
            return 0;
        }

        String operationType = command.issueType() == SessionIssueType.RENEW ? "선제적 갱신" : "신규 발급";
        log.info("[{}] 세션 발급 대상: {}건", operationType, targetIds.size());

        List<UserAgentId> successIds = new ArrayList<>();
        int processedCount = 0;

        for (UserAgentId userAgentId : targetIds) {
            if (processedCount >= command.maxBatchSize()) {
                break;
            }

            if (issueSessionForUserAgent(userAgentId)) {
                successIds.add(userAgentId);
            }
            processedCount++;

            if (command.sessionDelayMillis() > 0) {
                try {
                    Thread.sleep(command.sessionDelayMillis());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (!successIds.isEmpty()) {
            try {
                dbStatusManager.updateStatusToIdle(successIds);
            } catch (Exception e) {
                log.error("[{}] DB 상태 동기화 실패", operationType, e);
            }
        }

        log.info("[{}] 완료: 성공={}/{}", operationType, successIds.size(), processedCount);
        return successIds.size();
    }

    private List<UserAgentId> resolveTargetIds(IssueUserAgentSessionCommand command) {
        if (command.issueType() == SessionIssueType.RENEW) {
            return cacheQueryManager.getSessionExpiringUserAgents(command.renewalBufferMinutes());
        }
        return cacheQueryManager.getSessionRequiredUserAgents();
    }

    private boolean issueSessionForUserAgent(UserAgentId userAgentId) {
        try {
            Optional<CachedUserAgent> cachedOpt = cacheQueryManager.findById(userAgentId);
            if (cachedOpt.isEmpty()) {
                return false;
            }

            CachedUserAgent cached = cachedOpt.get();
            if (cached.userAgentValue() == null || cached.userAgentValue().isBlank()) {
                return false;
            }

            Optional<SessionToken> sessionTokenOpt =
                    sessionTokenManager.issueSessionToken(cached.userAgentValue());

            if (sessionTokenOpt.isPresent()) {
                SessionToken sessionToken = sessionTokenOpt.get();
                cacheCommandManager.updateSession(
                        userAgentId,
                        sessionToken.token(),
                        sessionToken.nid(),
                        sessionToken.mustitUid(),
                        sessionToken.expiresAt());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("세션 발급 오류: userAgentId={}", userAgentId.value(), e);
            return false;
        }
    }
}
