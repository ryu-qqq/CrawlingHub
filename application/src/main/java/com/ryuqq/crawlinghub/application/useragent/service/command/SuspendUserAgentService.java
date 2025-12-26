package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.SuspendUserAgentUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 개별 UserAgent 정지 Service
 *
 * <p>{@link SuspendUserAgentUseCase} 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SuspendUserAgentService implements SuspendUserAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuspendUserAgentService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final UserAgentPoolCacheManager cacheManager;
    private final ClockHolder clockHolder;

    public SuspendUserAgentService(
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            UserAgentPoolCacheManager cacheManager,
            ClockHolder clockHolder) {
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.cacheManager = cacheManager;
        this.clockHolder = clockHolder;
    }

    @Override
    public void execute(long userAgentId) {
        UserAgentId id = UserAgentId.of(userAgentId);

        // 1. DB에서 UserAgent 조회
        UserAgent userAgent =
                readManager.findById(id).orElseThrow(() -> new UserAgentNotFoundException(id));

        // 2. Domain 로직 실행 (AVAILABLE → SUSPENDED)
        userAgent.suspend(clockHolder.getClock());

        // 3. Redis Pool에서 제거
        cacheManager.removeFromPool(id);

        // 4. DB 저장
        transactionManager.persist(userAgent);

        log.info("UserAgent {} 수동 정지 완료", userAgentId);
    }
}
