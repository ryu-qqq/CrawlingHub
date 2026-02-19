package com.ryuqq.crawlinghub.application.useragent.manager;

import com.ryuqq.crawlinghub.application.common.time.TimeProvider;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.CachedUserAgent;
import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.dto.command.RecordUserAgentResultCommand;
import com.ryuqq.crawlinghub.application.useragent.manager.query.UserAgentReadManager;
import com.ryuqq.crawlinghub.domain.useragent.aggregate.UserAgent;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import com.ryuqq.crawlinghub.domain.useragent.exception.NoAvailableUserAgentException;
import com.ryuqq.crawlinghub.domain.useragent.identifier.UserAgentId;
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
 *   <li>토큰 소비 (consume)
 *   <li>결과 기록 (recordResult)
 *   <li>Circuit Breaker 체크
 *   <li>SUSPENDED UserAgent 복구
 * </ul>
 *
 * <p><strong>리팩토링</strong>: 직접 Port 의존 대신 분리된 Manager들을 사용
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolManager {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolManager.class);
    private static final double CIRCUIT_BREAKER_THRESHOLD = 20.0;

    private final UserAgentPoolCacheManager cacheManager;
    private final UserAgentReadManager readManager;
    private final UserAgentTransactionManager transactionManager;
    private final TimeProvider timeProvider;

    public UserAgentPoolManager(
            UserAgentPoolCacheManager cacheManager,
            UserAgentReadManager readManager,
            UserAgentTransactionManager transactionManager,
            TimeProvider timeProvider) {
        this.cacheManager = cacheManager;
        this.readManager = readManager;
        this.transactionManager = transactionManager;
        this.timeProvider = timeProvider;
    }

    /**
     * 토큰 소비 (핵심 메서드)
     *
     * <ol>
     *   <li>Circuit Breaker 체크
     *   <li>Redis에서 토큰 소비 (Lua Script)
     * </ol>
     *
     * @return 선택된 CachedUserAgent
     * @throws CircuitBreakerOpenException 가용률 < 20%일 때
     * @throws NoAvailableUserAgentException 사용 가능한 UserAgent가 없을 때
     */
    public CachedUserAgent consume() {
        checkCircuitBreaker();

        return cacheManager.consumeToken().orElseThrow(NoAvailableUserAgentException::new);
    }

    /**
     * 결과 기록
     *
     * <ul>
     *   <li>성공: Health Score +5 (Redis만)
     *   <li>429: 즉시 SUSPENDED, Pool에서 제거, DB 저장
     *   <li>기타 에러: Health Score 감소, SUSPENDED 시 DB 저장
     * </ul>
     *
     * @param command 결과 기록 커맨드
     */
    public void recordResult(RecordUserAgentResultCommand command) {
        UserAgentId userAgentId = UserAgentId.of(command.userAgentId());

        if (command.success()) {
            cacheManager.recordSuccess(userAgentId);
            log.debug("UserAgent {} 성공 기록 완료", command.userAgentId());
        } else if (command.isRateLimited()) {
            handleRateLimited(userAgentId);
        } else {
            handleFailure(userAgentId, command.httpStatusCode());
        }
    }

    /**
     * 일반 실패 처리 (5xx, 기타 에러)
     *
     * <p>Redis에서 Health Score 감소 후, SUSPENDED 되었으면 DB에도 저장
     */
    private void handleFailure(UserAgentId userAgentId, int httpStatusCode) {
        boolean suspended = cacheManager.recordFailure(userAgentId, httpStatusCode);

        if (suspended) {
            readManager
                    .findById(userAgentId)
                    .ifPresent(
                            userAgent -> {
                                userAgent.recordFailure(httpStatusCode, timeProvider.now());
                                transactionManager.persist(userAgent);
                            });
            log.warn("UserAgent {} Health Score 부족으로 SUSPENDED 처리", userAgentId.value());
        } else {
            log.debug("UserAgent {} 실패 기록: HTTP {}", userAgentId.value(), httpStatusCode);
        }
    }

    /**
     * 429 Rate Limited 처리
     *
     * <ol>
     *   <li>세션 토큰 만료 처리 (SESSION_REQUIRED 상태로 전환)
     *   <li>Pool에서 제거
     *   <li>Domain 조회 → 비즈니스 로직(suspend) → DB 저장
     * </ol>
     *
     * <p>429 응답은 세션 토큰이 무효화되었음을 의미하므로 세션 재발급이 필요합니다.
     */
    private void handleRateLimited(UserAgentId userAgentId) {
        cacheManager.expireSession(userAgentId);
        cacheManager.removeFromPool(userAgentId);

        readManager
                .findById(userAgentId)
                .ifPresent(
                        userAgent -> {
                            userAgent.recordFailure(429, timeProvider.now());
                            transactionManager.persist(userAgent);
                        });

        log.warn("UserAgent {} Rate Limited (429) - SUSPENDED 처리, 세션 만료", userAgentId.value());
    }

    /**
     * Circuit Breaker 체크
     *
     * @throws CircuitBreakerOpenException 가용률 < 20%일 때
     */
    private void checkCircuitBreaker() {
        PoolStats stats = cacheManager.getPoolStats();

        if (stats.total() == 0) {
            log.error("UserAgent Pool이 비어있습니다.");
            throw new CircuitBreakerOpenException(0);
        }

        double availableRate = stats.availableRate();
        if (availableRate < CIRCUIT_BREAKER_THRESHOLD) {
            log.warn("Circuit Breaker OPEN - 가용률: {}%", String.format("%.2f", availableRate));
            throw new CircuitBreakerOpenException(availableRate);
        }
    }

    /**
     * SUSPENDED UserAgent 복구
     *
     * <p><strong>복구 조건</strong>:
     *
     * <ul>
     *   <li>SUSPENDED 상태
     *   <li>1시간 경과
     *   <li>Health Score ≥ 30
     * </ul>
     *
     * @return 복구된 UserAgent 수
     */
    public int recoverSuspendedUserAgents() {
        List<UserAgentId> recoverableIds = cacheManager.getRecoverableUserAgents();

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

    /**
     * 단일 UserAgent 복구
     *
     * <p>Domain 조회 → 비즈니스 로직(recover) → DB 저장
     *
     * <p>복구 후 SESSION_REQUIRED 상태로 Pool에 추가 (세션 발급 스케줄러에서 세션 발급)
     */
    private boolean recoverSingleUserAgent(UserAgentId userAgentId) {
        Optional<UserAgent> userAgentOptional = readManager.findById(userAgentId);

        if (userAgentOptional.isEmpty()) {
            log.warn("복구 대상 UserAgent {} 조회 실패", userAgentId.value());
            return false;
        }

        UserAgent userAgent = userAgentOptional.get();
        userAgent.recover(timeProvider.now());

        String userAgentValue = userAgent.getUserAgentString().value();
        cacheManager.restoreToPool(userAgentId, userAgentValue);
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
        return cacheManager.getPoolStats();
    }
}
