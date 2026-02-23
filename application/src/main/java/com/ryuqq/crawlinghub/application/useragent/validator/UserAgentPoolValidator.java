package com.ryuqq.crawlinghub.application.useragent.validator;

import com.ryuqq.crawlinghub.application.useragent.dto.cache.PoolStats;
import com.ryuqq.crawlinghub.application.useragent.manager.UserAgentPoolCacheQueryManager;
import com.ryuqq.crawlinghub.domain.useragent.exception.CircuitBreakerOpenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * UserAgent Pool Validator
 *
 * <p><strong>책임</strong>: UserAgent Pool 가용 상태 검증 (Circuit Breaker)
 *
 * <p>Pool 가용률이 임계값 미만이면 {@link CircuitBreakerOpenException}을 던져 크롤링을 중단합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class UserAgentPoolValidator {

    private static final Logger log = LoggerFactory.getLogger(UserAgentPoolValidator.class);
    private static final double CIRCUIT_BREAKER_THRESHOLD = 20.0;

    private final UserAgentPoolCacheQueryManager queryManager;

    public UserAgentPoolValidator(UserAgentPoolCacheQueryManager queryManager) {
        this.queryManager = queryManager;
    }

    /**
     * Circuit Breaker 체크
     *
     * @throws CircuitBreakerOpenException Pool이 비어있거나 가용률 < 20%일 때
     */
    public void validateAvailability() {
        PoolStats stats = queryManager.getPoolStats();

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
}
