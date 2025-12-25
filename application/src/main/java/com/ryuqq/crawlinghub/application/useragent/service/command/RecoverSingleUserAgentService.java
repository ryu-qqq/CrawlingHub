package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.RecoverSingleUserAgentUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 개별 UserAgent 복구 Service
 *
 * <p>{@link RecoverSingleUserAgentUseCase} 구현체
 *
 * <p>시간 조건 없이 즉시 복구합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class RecoverSingleUserAgentService implements RecoverSingleUserAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(RecoverSingleUserAgentService.class);

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final UserAgentPoolCacheManager cacheManager;
    private final ClockHolder clockHolder;

    public RecoverSingleUserAgentService(
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

        // 2. Domain 로직 실행 (SUSPENDED → AVAILABLE, Health 70)
        userAgent.recover(clockHolder.getClock());

        // 3. Redis Pool에 복구 (세션은 Lazy 발급)
        String userAgentValue = userAgent.getUserAgentString().value();
        cacheManager.restoreToPool(id, userAgentValue);

        // 4. DB 저장
        transactionManager.persist(userAgent);

        log.info("UserAgent {} 수동 복구 완료", userAgentId);
    }
}
