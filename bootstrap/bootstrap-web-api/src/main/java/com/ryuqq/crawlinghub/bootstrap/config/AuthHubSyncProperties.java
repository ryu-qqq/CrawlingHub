package com.ryuqq.crawlinghub.bootstrap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AuthHub 엔드포인트 동기화 설정 Properties.
 *
 * <p>application.yml의 {@code authhub.*} 설정을 바인딩합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "authhub")
public class AuthHubSyncProperties {

    private String baseUrl;
    private String serviceToken;
    private String serviceCode;
    private EndpointSync endpointSync = new EndpointSync();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public EndpointSync getEndpointSync() {
        return endpointSync;
    }

    public void setEndpointSync(EndpointSync endpointSync) {
        this.endpointSync = endpointSync;
    }

    @SuppressWarnings("PMD.DataClass")
    public static class EndpointSync {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
