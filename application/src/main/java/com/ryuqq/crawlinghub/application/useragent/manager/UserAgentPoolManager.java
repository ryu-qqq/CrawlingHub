package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.validator.UserAgentPoolValidator;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.CooldownPolicy;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Manager
 *
 * <p>Redis Pool 기반 UserAgent 관리의 핵심 비즈니스 로직을 담당합니다.
 *
 * <p><strong>주요 기능</strong>:
 *
 * <ul>
 *   <li>borrow / returnAgent (HikariCP getConnection/close 패턴)
 *   <li>토큰 소비 (consume) - 하위 호환용, 내부적으로 borrow 위임
 *   <li>결과 기록 (recordResult)
 *   <li>SUSPENDED UserAgent 복구
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolManager {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolManager.class);

    private final UserAgentPoolValidator poolValidator;
    private final UserAgentPoolCacheCommandManager cacheCommandManager;
    private final UserAgentPoolCacheStateManager cacheStateManager;
    private final UserAgentPoolCacheQueryManager cacheQueryManager;
    private final UserAgentReadManager readManager;
    private final UserAgentCommandManager transactionManager;

    public UserAgentPoolManager(
            UserAgentPoolValidator poolValidator,
            UserAgentPoolCacheCommandManager cacheCommandManager,
            UserAgentPoolCacheStateManager cacheStateManager,
            UserAgentPoolCacheQueryManager cacheQueryManager,
            UserAgentReadManager readManager,
            UserAgentCommandManager transactionManager) {
        this.poolValidator = poolValidator;
        this.cacheCommandManager = cacheCommandManager;
        this.cacheStateManager = cacheStateManager;
        this.cacheQueryManager = cacheQueryManager;
        this.readManager = readManager;
        this.transactionManager = transactionManager;
    }

    /**
     * UserAgent borrow (HikariCP getConnection() 대응)
     *
     * <ol>
     *   <li>Circuit Breaker 체크 (Validator)
     *   <li>Redis에서 IDLE -> BORROWED 전환 (Lua Script)
     * </ol>
     *
     * @return BorrowedUserAgent (크롤링에 필요한 최소 정보)
     * @throws NoAvailableUserAgentException IDLE 상태의 UserAgent가 없을 때
     */
    public BorrowedUserAgent borrow() {
        poolValidator.validateAvailability();
        CachedUserAgent cached =
                cacheCommandManager.borrow().orElseThrow(NoAvailableUserAgentException::new);
        return BorrowedUserAgent.from(cached);
    }

    /**
     * UserAgent 반납 (HikariCP connection.close() 대응)
     *
     * <p>크롤링 결과에 따라 BORROWED -> IDLE/COOLDOWN/SUSPENDED 전환
     *
     * @param userAgentId UserAgent ID
     * @param success 성공 여부
     * @param httpStatusCode HTTP 상태 코드 (성공 시 무시)
     * @param consecutiveRateLimits 현재 연속 429 횟수
     */
    public void returnAgent(
            long userAgentId, boolean success, int httpStatusCode, int consecutiveRateLimits) {
        int healthDelta =
                success ? HealthScore.successIncrement() : -HealthScore.penaltyFor(httpStatusCode);
        Long cooldownUntil = null;
        int newConsecutive = consecutiveRateLimits;

        if (!success && httpStatusCode == HealthScore.RATE_LIMIT_STATUS_CODE) {
            CooldownPolicy policy = CooldownPolicy.escalate(consecutiveRateLimits, Instant.now());
            cooldownUntil = policy.cooldownUntil().toEpochMilli();
            newConsecutive = policy.consecutiveRateLimits();
        }

        try {
            int result =
                    cacheCommandManager.returnAgent(
                            userAgentId,
                            success,
                            httpStatusCode,
                            healthDelta,
                            cooldownUntil,
                            newConsecutive);

            if (result == 2) {
                syncSuspendedToDb(userAgentId, httpStatusCode);
            }
        } catch (Exception e) {
            log.warn("Redis return 실패 - DB에 직접 기록: userAgentId={}", userAgentId, e);
            syncResultToDb(userAgentId, success, httpStatusCode);
        }
    }

    /**
     * 토큰 소비 (하위 호환용)
     *
     * <p>내부적으로 borrow()를 호출합니다.
     *
     * <ol>
     *   <li>Circuit Breaker 체크 (Validator)
     *   <li>Redis에서 토큰 소비 (Lua Script)
     * </ol>
     *
     * @return 선택된 CachedUserAgent
     */
    public CachedUserAgent consume() {
        poolValidator.validateAvailability();
        return cacheCommandManager.consumeToken().orElseThrow(NoAvailableUserAgentException::new);
    }

    /**
     * 결과 기록
     *
     * @param userAgentId UserAgent ID
     * @param success 성공 여부
     * @param httpStatusCode HTTP 상태 코드 (성공 시 무시)
     */
    public void recordResult(long userAgentId, boolean success, int httpStatusCode) {
        UserAgentId id = UserAgentId.of(userAgentId);

        if (success) {
            cacheStateManager.applyHealthDelta(id, HealthScore.successIncrement());
            log.debug("UserAgent {} 성공 기록 완료", userAgentId);
        } else if (httpStatusCode == HealthScore.RATE_LIMIT_STATUS_CODE) {
            handleRateLimited(id);
        } else {
            handleFailure(id, httpStatusCode);
        }
    }

    private void handleFailure(UserAgentId userAgentId, int httpStatusCode) {
        int penalty = HealthScore.penaltyFor(httpStatusCode);
        boolean suspended = cacheStateManager.applyHealthDelta(userAgentId, -penalty);

        if (suspended) {
            readManager
                    .findById(userAgentId)
                    .ifPresent(
                            userAgent -> {
                                userAgent.recordFailure(httpStatusCode, Instant.now());
                                transactionManager.persist(userAgent);
                            });
            log.warn("UserAgent {} Health Score 부족으로 SUSPENDED 처리", userAgentId.value());
        } else {
            log.debug("UserAgent {} 실패 기록: HTTP {}", userAgentId.value(), httpStatusCode);
        }
    }

    private void handleRateLimited(UserAgentId userAgentId) {
        cacheCommandManager.suspendForRateLimit(userAgentId);

        readManager
                .findById(userAgentId)
                .ifPresent(
                        userAgent -> {
                            userAgent.recordFailure(
                                    HealthScore.RATE_LIMIT_STATUS_CODE, Instant.now());
                            transactionManager.persist(userAgent);
                        });

        log.warn("UserAgent {} Rate Limited - SUSPENDED 처리, 세션 만료", userAgentId.value());
    }

    /**
     * SUSPENDED UserAgent 복구
     *
     * @return 복구된 UserAgent 수
     */
    public int recoverSuspendedUserAgents() {
        List<UserAgentId> recoverableIds = cacheQueryManager.getRecoverableUserAgents();

        if (recoverableIds.isEmpty()) {
            log.debug("복구 대상 UserAgent 없음");
            return 0;
        }

        int recoveredCount = 0;
        for (UserAgentId userAgentId : recoverableIds) {
            if (recoverSingleUserAgent(userAgentId)) {
                recoveredCount++;
            }
        }

        log.info("UserAgent 복구 완료: {} / {} 건", recoveredCount, recoverableIds.size());
        return recoveredCount;
    }

    private boolean recoverSingleUserAgent(UserAgentId userAgentId) {
        Optional<UserAgent> userAgentOptional = readManager.findById(userAgentId);

        if (userAgentOptional.isEmpty()) {
            log.warn("복구 대상 UserAgent {} 조회 실패", userAgentId.value());
            return false;
        }

        UserAgent userAgent = userAgentOptional.get();
        userAgent.recover(Instant.now());

        String userAgentValue = userAgent.getUserAgentStringValue();
        cacheCommandManager.restoreToPool(userAgentId, userAgentValue);
        transactionManager.persist(userAgent);

        log.debug("UserAgent {} 복구 완료 (SESSION_REQUIRED 상태)", userAgentId.value());
        return true;
    }

    /**
     * Pool 상태 조회
     *
     * @return Pool 통계
     */
    public PoolStats getPoolStats() {
        return cacheQueryManager.getPoolStats();
    }

    private void syncSuspendedToDb(long userAgentId, int httpStatusCode) {
        UserAgentId id = UserAgentId.of(userAgentId);
        readManager
                .findById(id)
                .ifPresent(
                        userAgent -> {
                            userAgent.recordFailure(httpStatusCode, Instant.now());
                            transactionManager.persist(userAgent);
                        });
        log.warn("UserAgent {} SUSPENDED -> DB 동기화", userAgentId);
    }

    private void syncResultToDb(long userAgentId, boolean success, int httpStatusCode) {
        UserAgentId id = UserAgentId.of(userAgentId);
        readManager
                .findById(id)
                .ifPresent(
                        userAgent -> {
                            if (success) {
                                userAgent.recordSuccess(Instant.now());
                            } else {
                                userAgent.recordFailure(httpStatusCode, Instant.now());
                            }
                            transactionManager.persist(userAgent);
                        });
    }
}
