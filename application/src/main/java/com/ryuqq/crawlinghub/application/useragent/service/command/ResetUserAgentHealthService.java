package com.ryuqq.crawlinghub.application.useragent.service.command;

import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentTransactionManager;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.port.in.command.ResetUserAgentHealthUseCase;
import com.ryuqq.crawlinghub.domain.common.util.ClockHolder;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.UserAgentNotFoundException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UserAgent Health Score 리셋 Service
 *
 * <p>{@link ResetUserAgentHealthUseCase} 구현체
 *
 * <p>Health Score를 100으로 초기화합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class ResetUserAgentHealthService implements ResetUserAgentHealthUseCase {

    private static final Logger log = LoggerFactory.getLogger(ResetUserAgentHealthService.class);
    private static final int INITIAL_HEALTH_SCORE = 100;

    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final UserAgentPoolCacheManager cacheManager;
    private final ClockHolder clockHolder;

    public ResetUserAgentHealthService(
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

        // 2. Domain 로직 실행 (Health Score → 100)
        userAgent.resetHealth(clockHolder.getClock());

        // 3. Redis Pool 업데이트 (Pool에 있는 경우만)
        cacheManager.updateHealthScore(id, INITIAL_HEALTH_SCORE);

        // 4. DB 저장
        transactionManager.persist(userAgent);

        log.info("UserAgent {} Health Score 리셋 완료 (100)", userAgentId);
    }
}
