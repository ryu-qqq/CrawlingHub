package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.command.UpdateUserAgentStatusCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.UpdateUserAgentStatusUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.UserAgentStatus;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserAgent 상태 일괄 변경 Service
 *
 * <p>{@link UpdateUserAgentStatusUseCase} 구현체
 *
 * <p><strong>처리 순서</strong>:
 *
 * <ol>
 *   <li>요청된 ID 목록으로 UserAgent 일괄 조회
 *   <li>모든 UserAgent에 대해 상태 변경 (Domain 로직)
 *   <li>상태에 따라 Redis Pool 처리 (AVAILABLE: 추가, 그 외: 제거)
 *   <li>DB 일괄 저장
 * </ol>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class UpdateUserAgentStatusService implements UpdateUserAgentStatusUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserAgentStatusService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final UserAgentPoolCacheManager cacheManager;
    private final TimeProvider timeProvider;

    public UpdateUserAgentStatusService(
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            UserAgentPoolCacheManager cacheManager,
            TimeProvider timeProvider) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.cacheManager = cacheManager;
        this.timeProvider = timeProvider;
    }

    @Override
    @Transactional
    public int execute(UpdateUserAgentStatusCommand command) {
        List<UserAgentId> userAgentIds =
                command.userAgentIds().stream().map(UserAgentId::of).toList();

        // 1. 일괄 조회
        List<UserAgent> userAgents = readManager.findByIds(userAgentIds);

        // 2. 조회 결과 검증 (모두 존재해야 함)
        if (userAgents.size() != userAgentIds.size()) {
            List<Long> foundIds = userAgents.stream().map(ua -> ua.getId().value()).toList();
            List<Long> notFoundIds =
                    command.userAgentIds().stream().filter(id -> !foundIds.contains(id)).toList();
            throw new UserAgentNotFoundException(UserAgentId.of(notFoundIds.get(0)));
        }

        Instant now = timeProvider.now();
        UserAgentStatus newStatus = command.status();

        // 3. 상태 변경 (Domain 로직)
        for (UserAgent userAgent : userAgents) {
            userAgent.changeStatus(newStatus, now);
        }

        // 4. DB 일괄 저장
        transactionManager.persistAll(userAgents);

        // 5. Redis Pool 처리 (DB 저장 후 처리)
        if (newStatus.isAvailable()) {
            List<CachedUserAgent> cachedUserAgents =
                    userAgents.stream().map(CachedUserAgent::forNew).toList();
            cacheManager.warmUp(cachedUserAgents);
        } else {
            for (UserAgent userAgent : userAgents) {
                cacheManager.removeFromPool(userAgent.getId());
            }
        }

        log.info("UserAgent {} 개 상태 변경 완료: → {}", userAgents.size(), newStatus);

        return userAgents.size();
    }
}
