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
 * WARNING: This UseCase is currently NOT USED anywhere in the project.
 * Consider removing or documenting the future usage plan.
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
     * 1. DB에서 활성 User-Agent 조회 (트랜잭션)
     * 2. Redis Pool 초기화 (비트랜잭션)
     * 3. Pool에 등록 (비트랜잭션)
     * 4. Rate Limiter 초기화 (비트랜잭션)
     *
     * FIXED: 트랜잭션 제거
     * - Redis 작업은 트랜잭션이 필요 없음
     * - DB 커넥션을 불필요하게 점유하지 않도록 수정
     * - DB 조회만 별도 트랜잭션으로 처리
     *
     * @return 초기화된 User-Agent 수
     */
    public int execute() {
        log.info("Initializing User-Agent Pool...");

        // 1. 기존 Pool 초기화 (Redis, 비트랜잭션)
        poolPort.clearPool();

        // 2. DB에서 활성 User-Agent 조회 (짧은 트랜잭션)
        List<Long> activeUserAgents = findActiveUserAgentsWithTransaction();

        // 3. Redis Pool에 등록 및 Rate Limiter 초기화 (비트랜잭션)
        int initializedCount = 0;
        for (Long userAgentId : activeUserAgents) {
            poolPort.addToPool(userAgentId);
            rateLimiterPort.initialize(userAgentId);
            initializedCount++;
        }

        log.info("User-Agent Pool initialized with {} agents", initializedCount);
        return initializedCount;
    }

    /**
     * DB에서 활성 User-Agent 조회 (트랜잭션 적용)
     *
     * @return 활성 User-Agent ID 목록
     */
    @Transactional(readOnly = true)
    private List<Long> findActiveUserAgentsWithTransaction() {
        return tokenPort.findAllActiveUserAgents();
    }
}
