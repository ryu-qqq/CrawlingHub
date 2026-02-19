package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.ResetCircuitBreakerUseCase;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Circuit Breaker 리셋 Service
 *
 * <p>{@link ResetCircuitBreakerUseCase} 구현체
 *
 * <p>모든 SUSPENDED UserAgent를 즉시 복구합니다 (시간 조건 무시).
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ResetCircuitBreakerService implements ResetCircuitBreakerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetCircuitBreakerService.class);

    private final UserAgentPoolCacheManager cacheManager;
    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final TimeProvider timeProvider;

    public ResetCircuitBreakerService(
            UserAgentPoolCacheManager cacheManager,
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            TimeProvider timeProvider) {
        this.cacheManager = cacheManager;
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.timeProvider = timeProvider;
    }

    @Override
    public int execute() {
        // 1. 모든 SUSPENDED UserAgent ID 조회 (시간 조건 무시)
        List<UserAgentId> suspendedIds = cacheManager.getAllSuspendedUserAgents();

        if (suspendedIds.isEmpty()) {
            log.info("Circuit Breaker 리셋 - SUSPENDED UserAgent 없음");
            return 0;
        }

        // 2. 각 UserAgent 복구
        int recoveredCount = 0;
        for (UserAgentId id : suspendedIds) {
            if (recoverSingleUserAgent(id)) {
                recoveredCount++;
            }
        }

        log.info("Circuit Breaker 리셋 완료: {} / {} 건 복구", recoveredCount, suspendedIds.size());
        return recoveredCount;
    }

    private boolean recoverSingleUserAgent(UserAgentId userAgentId) {
        try {
            UserAgent userAgent = readManager.findById(userAgentId).orElse(null);
            if (userAgent == null) {
                log.warn("UserAgent {} DB에서 찾을 수 없음", userAgentId.value());
                return false;
            }

            // Domain 로직 실행
            userAgent.recover(timeProvider.now());

            // Redis Pool 복구
            String userAgentValue = userAgent.getUserAgentString().value();
            cacheManager.restoreToPool(userAgentId, userAgentValue);

            // DB 저장
            transactionManager.persist(userAgent);

            return true;
        } catch (Exception e) {
            log.error("UserAgent {} 복구 실패: {}", userAgentId.value(), e.getMessage());
            return false;
        }
    }
}
