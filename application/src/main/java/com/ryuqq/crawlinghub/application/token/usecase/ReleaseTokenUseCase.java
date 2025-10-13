package com.ryuqq.crawlinghub.application.token.usecase;

import com.ryuqq.crawlinghub.application.token.port.DistributedLockPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.application.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Release Token Use Case
 * 토큰 반환 플로우
 *
 * @author crawlinghub
 */
@Service
public class ReleaseTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReleaseTokenUseCase.class);

    private final UserAgentPoolPort poolPort;
    private final UserAgentTokenPort tokenPort;
    private final DistributedLockPort lockPort;

    public ReleaseTokenUseCase(
            UserAgentPoolPort poolPort,
            UserAgentTokenPort tokenPort,
            DistributedLockPort lockPort) {
        this.poolPort = poolPort;
        this.tokenPort = tokenPort;
        this.lockPort = lockPort;
    }

    /**
     * 토큰 반환
     *
     * 플로우:
     * 1. 분산 락 해제
     * 2. DB 통계 업데이트 (성공/실패)
     * 3. Pool 반환
     *
     * @param acquiredToken 획득한 토큰
     * @param success 요청 성공 여부
     */
    @Transactional
    public void execute(AcquiredToken acquiredToken, boolean success) {
        if (acquiredToken == null || !acquiredToken.isAcquired()) {
            log.warn("Invalid acquired token for release");
            return;
        }

        try {
            // 1. 분산 락 해제
            if (acquiredToken.hasLock()) {
                lockPort.release(acquiredToken.lockKey(), acquiredToken.lockValue());
            }

            // 2. DB 통계 업데이트
            if (success) {
                tokenPort.recordSuccess(acquiredToken.userAgentId());
            } else {
                tokenPort.recordFailure(acquiredToken.userAgentId());
            }

            // 3. Pool 반환
            poolPort.returnToPool(acquiredToken.userAgentId());

            log.debug("Released User-Agent ID: {} (success: {})", acquiredToken.userAgentId(), success);

        } catch (Exception e) {
            log.error("Failed to release User-Agent ID: {}", acquiredToken.userAgentId(), e);
        }
    }
}
