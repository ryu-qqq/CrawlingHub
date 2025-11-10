package com.ryuqq.crawlinghub.application.legacy.token.usecase;

import com.ryuqq.crawlinghub.application.legacy.token.port.DistributedLockPort;
import com.ryuqq.crawlinghub.application.legacy.token.port.UserAgentPoolPort;
import com.ryuqq.crawlinghub.application.legacy.token.port.UserAgentTokenPort;
import com.ryuqq.crawlinghub.domain.token.AcquiredToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Release Token Use Case
 * 토큰 반환 플로우
 *
 * WARNING: This UseCase is currently NOT USED anywhere in the project.
 * Consider removing or documenting the future usage plan.
 *
 * FIXED: 트랜잭션 분리
 * - Redis 작업(락 해제, Pool 반환)은 트랜잭션 밖에서 실행
 * - DB 통계 업데이트만 짧은 독립 트랜잭션으로 처리
 * - AcquireTokenUseCase 패턴과 일관성 유지
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
     * 토큰 반환 (오케스트레이션)
     *
     * ⚠️ 트랜잭션 없음 - Redis 작업과 DB 작업 분리
     *
     * 플로우:
     * 1. 분산 락 해제 (Redis, 비트랜잭션)
     * 2. [트랜잭션] DB 통계 업데이트 (성공/실패)
     * 3. Pool 반환 (Redis, 비트랜잭션)
     *
     * @param acquiredToken 획득한 토큰
     * @param success 요청 성공 여부
     */
    public void execute(AcquiredToken acquiredToken, boolean success) {
        if (acquiredToken == null || !acquiredToken.isAcquired()) {
            log.warn("Invalid acquired token for release");
            return;
        }

        try {
            // 1. 분산 락 해제 (Redis, 트랜잭션 밖)
            if (acquiredToken.hasLock()) {
                lockPort.release(acquiredToken.lockKey(), acquiredToken.lockValue());
            }

            // 2. DB 통계 업데이트 (짧은 독립 트랜잭션)
            recordStatisticsInTransaction(acquiredToken.userAgentId(), success);

            // 3. Pool 반환 (Redis, 트랜잭션 밖)
            poolPort.returnToPool(acquiredToken.userAgentId());

            log.debug("Released User-Agent ID: {} (success: {})", acquiredToken.userAgentId(), success);

        } catch (Exception e) {
            log.error("Failed to release User-Agent ID: {}", acquiredToken.userAgentId(), e);
        }
    }

    /**
     * DB 통계 기록 (트랜잭션 적용)
     *
     * 트랜잭션 특성:
     * - 독립된 트랜잭션
     * - 빠른 실행 (DB 쓰기만)
     * - Redis 작업과 분리
     *
     * @param userAgentId User-Agent ID
     * @param success 요청 성공 여부
     */
    @Transactional
    private void recordStatisticsInTransaction(Long userAgentId, boolean success) {
        if (success) {
            tokenPort.recordSuccess(userAgentId);
        } else {
            tokenPort.recordFailure(userAgentId);
        }
    }
}
