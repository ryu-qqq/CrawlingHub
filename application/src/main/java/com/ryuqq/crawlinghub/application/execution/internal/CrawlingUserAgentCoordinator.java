package com.ryuqq.crawlinghub.application.execution.internal;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.BorrowedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheCommandManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheStateManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolManager;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentReadManager;
import com.ryuqq.crawlinghub.application.useragent.validator.UserAgentPoolValidator;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.id.UserAgentId;
import com.ryuqq.crawlinghub.domain.useragent.vo.AvailableUserAgentPool;
import com.ryuqq.crawlinghub.domain.useragent.vo.HealthScore;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Execution 전용 UserAgent Coordinator
 *
 * <p>HikariCP getConnection/close 패턴 기반의 borrow/return 라이프사이클을 제공합니다.
 *
 * <p><strong>주요 흐름</strong>:
 *
 * <ol>
 *   <li>borrow(): IDLE → BORROWED (Redis Lua Script atomic)
 *   <li>크롤링 수행
 *   <li>returnAgent(): BORROWED → IDLE/COOLDOWN/SUSPENDED (try-finally 보장)
 * </ol>
 *
 * <p><strong>Redis 장애 대응</strong>:
 *
 * <ul>
 *   <li>borrow(): Redis 실패 시 DB에서 healthScore 최고 UserAgent 선택
 *   <li>returnAgent(): Redis 실패 시 DB에 직접 기록 (UserAgentPoolManager 위임)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlingUserAgentCoordinator {

    private static final Logger log = LoggerFactory.getLogger(CrawlingUserAgentCoordinator.class);

    private final UserAgentPoolManager poolManager;
    private final UserAgentPoolValidator poolValidator;
    private final UserAgentPoolCacheCommandManager cacheCommandManager;
    private final UserAgentPoolCacheStateManager cacheStateManager;
    private final UserAgentReadManager readManager;
    private final UserAgentCommandManager commandManager;

    public CrawlingUserAgentCoordinator(
            UserAgentPoolManager poolManager,
            UserAgentPoolValidator poolValidator,
            UserAgentPoolCacheCommandManager cacheCommandManager,
            UserAgentPoolCacheStateManager cacheStateManager,
            UserAgentReadManager readManager,
            UserAgentCommandManager commandManager) {
        this.poolManager = poolManager;
        this.poolValidator = poolValidator;
        this.cacheCommandManager = cacheCommandManager;
        this.cacheStateManager = cacheStateManager;
        this.readManager = readManager;
        this.commandManager = commandManager;
    }

    /**
     * UserAgent borrow (HikariCP getConnection() 대응)
     *
     * <p>Redis에서 IDLE → BORROWED 전환. Redis 장애 시 DB 폴백.
     *
     * @return BorrowedUserAgent (크롤링에 필요한 최소 정보)
     * @throws CircuitBreakerOpenException 가용률 < 20%일 때
     * @throws NoAvailableUserAgentException 사용 가능한 UserAgent가 없을 때
     */
    public BorrowedUserAgent borrow() {
        try {
            return poolManager.borrow();
        } catch (CircuitBreakerOpenException | NoAvailableUserAgentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis borrow 실패, DB 폴백: {}", e.getMessage());
            return borrowFromDb();
        }
    }

    /**
     * UserAgent 반납 (HikariCP connection.close() 대응)
     *
     * <p>반드시 try-finally에서 호출. 크롤링 결과에 따라 BORROWED → IDLE/COOLDOWN/SUSPENDED 전환.
     *
     * @param userAgentId UserAgent ID
     * @param success 성공 여부
     * @param httpStatusCode HTTP 상태 코드 (성공 시 0)
     * @param consecutiveRateLimits 현재 연속 429 횟수
     */
    public void returnAgent(
            long userAgentId, boolean success, int httpStatusCode, int consecutiveRateLimits) {
        try {
            poolManager.returnAgent(userAgentId, success, httpStatusCode, consecutiveRateLimits);
        } catch (Exception e) {
            log.error(
                    "UserAgent 반납 실패: userAgentId={}, success={}, httpStatus={}",
                    userAgentId,
                    success,
                    httpStatusCode,
                    e);
        }
    }

    private BorrowedUserAgent borrowFromDb() {
        List<UserAgent> available = readManager.findAllAvailable();
        AvailableUserAgentPool pool = new AvailableUserAgentPool(available);
        UserAgent selected = pool.selectBest();
        CachedUserAgent cached = CachedUserAgent.forDbFallback(selected);
        return BorrowedUserAgent.from(cached);
    }

    /**
     * 토큰 소비 (하위 호환용)
     *
     * @return 선택된 CachedUserAgent
     * @deprecated borrow/return 패턴 사용. {@link #borrow()} 참고
     */
    @Deprecated
    public CachedUserAgent consume() {
        try {
            poolValidator.validateAvailability();
            return cacheCommandManager
                    .consumeToken()
                    .orElseThrow(NoAvailableUserAgentException::new);
        } catch (CircuitBreakerOpenException | NoAvailableUserAgentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis 장애 감지, DB 폴백: {}", e.getMessage());
            List<UserAgent> available = readManager.findAllAvailable();
            AvailableUserAgentPool dbPool = new AvailableUserAgentPool(available);
            return CachedUserAgent.forDbFallback(dbPool.selectBest());
        }
    }

    /**
     * 결과 기록 (하위 호환용)
     *
     * @deprecated borrow/return 패턴 사용. {@link #returnAgent(long, boolean, int, int)} 참고
     */
    @Deprecated
    public void recordResult(long userAgentId, boolean success, int httpStatusCode) {
        UserAgentId id = UserAgentId.of(userAgentId);

        if (success) {
            recordSuccess(id);
        } else if (httpStatusCode == HealthScore.RATE_LIMIT_STATUS_CODE) {
            handleRateLimited(id);
        } else {
            handleFailure(id, httpStatusCode);
        }
    }

    private void recordSuccess(UserAgentId id) {
        try {
            cacheStateManager.applyHealthDelta(id, HealthScore.successIncrement());
        } catch (Exception e) {
            log.warn("Redis recordSuccess 실패, 스킵: id={}", id.value());
        }
    }

    private void handleFailure(UserAgentId userAgentId, int httpStatusCode) {
        int penalty = HealthScore.penaltyFor(httpStatusCode);
        boolean suspended = false;
        try {
            suspended = cacheStateManager.applyHealthDelta(userAgentId, -penalty);
        } catch (Exception e) {
            log.warn("Redis recordFailure 실패: id={}", userAgentId.value());
        }

        if (suspended) {
            readManager
                    .findById(userAgentId)
                    .ifPresent(
                            userAgent -> {
                                userAgent.recordFailure(httpStatusCode, Instant.now());
                                commandManager.persist(userAgent);
                            });
            log.warn("UserAgent {} Health Score 부족으로 SUSPENDED 처리", userAgentId.value());
        }
    }

    private void handleRateLimited(UserAgentId userAgentId) {
        try {
            cacheCommandManager.suspendForRateLimit(userAgentId);
        } catch (Exception e) {
            log.warn("Redis suspendForRateLimit 실패: id={}", userAgentId.value());
        }

        readManager
                .findById(userAgentId)
                .ifPresent(
                        userAgent -> {
                            userAgent.recordFailure(
                                    HealthScore.RATE_LIMIT_STATUS_CODE, Instant.now());
                            commandManager.persist(userAgent);
                        });

        log.warn("UserAgent {} Rate Limited - SUSPENDED 처리, 세션 만료", userAgentId.value());
    }
}
