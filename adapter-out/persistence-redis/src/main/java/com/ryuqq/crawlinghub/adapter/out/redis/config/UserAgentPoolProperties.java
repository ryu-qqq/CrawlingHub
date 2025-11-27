package com.ryuqq.crawlinghub.adapter.out.redis.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * UserAgent Pool 설정 Properties
 *
 * <p>Redis 기반 UserAgent Pool 관리에 필요한 설정값들을 정의합니다.
 *
 * <p><strong>설정 항목</strong>:
 *
 * <ul>
 *   <li>Rate Limit: 시간당 최대 요청 수, 윈도우 지속 시간
 *   <li>Session: 선제적 갱신 버퍼 시간
 *   <li>Health: SUSPENDED 전환 임계값
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "useragent-pool")
public class UserAgentPoolProperties {

    private RateLimit rateLimit = new RateLimit();
    private Session session = new Session();
    private Health health = new Health();
    private String keyPrefix = "useragent:";

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    /** Rate Limiting 설정 */
    public static class RateLimit {
        /** 시간당 최대 요청 수 (기본: 80) */
        private int maxTokens = 80;

        /** 윈도우 지속 시간 (기본: 1시간) */
        private Duration windowDuration = Duration.ofHours(1);

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Duration getWindowDuration() {
            return windowDuration;
        }

        public void setWindowDuration(Duration windowDuration) {
            this.windowDuration = windowDuration;
        }
    }

    /** 세션 관리 설정 */
    public static class Session {
        /**
         * 선제적 세션 갱신 버퍼 (분)
         *
         * <p>세션 만료 N분 전에 갱신 시작 (기본: 5분)
         */
        private int renewalBufferMinutes = 5;

        public int getRenewalBufferMinutes() {
            return renewalBufferMinutes;
        }

        public void setRenewalBufferMinutes(int renewalBufferMinutes) {
            this.renewalBufferMinutes = renewalBufferMinutes;
        }
    }

    /** Health Score 설정 */
    public static class Health {
        /** SUSPENDED로 전환되는 최소 점수 (기본: 30) */
        private int suspensionThreshold = 30;

        public int getSuspensionThreshold() {
            return suspensionThreshold;
        }

        public void setSuspensionThreshold(int suspensionThreshold) {
            this.suspensionThreshold = suspensionThreshold;
        }
    }
}
