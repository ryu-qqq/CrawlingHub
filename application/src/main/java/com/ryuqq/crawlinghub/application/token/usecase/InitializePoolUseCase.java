package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.RateLimiterPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Initialize Pool Use Case
 * Pool 초기화 플로우
 *
 * @author crawlinghub
 */
@Service
public class InitializePoolUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitializePoolUseCase.class);

    private final UserAgentPoolPort poolPort;
    private final UserAgentTokenPort tokenPort;
    private final RateLimiterPort rateLimiterPort;

    public InitializePoolUseCase(
            UserAgentPoolPort poolPort,
            UserAgentTokenPort tokenPort,
            RateLimiterPort rateLimiterPort) {
        this.poolPort = poolPort;
        this.tokenPort = tokenPort;
        this.rateLimiterPort = rateLimiterPort;
    }

    /**
     * Pool 초기화
     *
     * 플로우:
     * 1. DB에서 활성 User-Agent 조회
     * 2. Redis Pool 초기화
     * 3. Pool에 등록
     * 4. Rate Limiter 초기화
     *
     * @return 초기화된 User-Agent 수
     */
    @Transactional(readOnly = true)
    public int execute() {
        log.info("Initializing User-Agent Pool...");

        // 1. 기존 Pool 초기화
        poolPort.clearPool();

        // 2. DB에서 활성 User-Agent 조회
        List<Long> activeUserAgents = tokenPort.findAllActiveUserAgents();

        // 3. Redis Pool에 등록 및 Rate Limiter 초기화
        int initializedCount = 0;
        for (Long userAgentId : activeUserAgents) {
            poolPort.addToPool(userAgentId);
            rateLimiterPort.initialize(userAgentId);
            initializedCount++;
        }

        log.info("User-Agent Pool initialized with {} agents", initializedCount);
        return initializedCount;
    }
}
