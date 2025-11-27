package com.ryuqq.crawlinghub.application.useragent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 세션 스케줄러 설정 Properties
 *
 * <p>세션 발급 및 갱신 스케줄러에 필요한 설정값들을 정의합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "useragent-pool.session")
public class SessionSchedulerProperties {

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
