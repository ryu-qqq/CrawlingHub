package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.AuthType;

public class SiteAuthConfig {

    private final Long authConfigId;
    private final Long siteId;
    private final AuthType authType;
    private final String tokenEndpoint;
    private String clientId;
    private String clientSecret;
    private final String additionalParams;

    private SiteAuthConfig(Long authConfigId, Long siteId, AuthType authType, String tokenEndpoint,
                          String clientId, String clientSecret, String additionalParams) {
        this.authConfigId = authConfigId;
        this.siteId = siteId;
        this.authType = authType;
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.additionalParams = additionalParams;
    }

    public void updateCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Long getAuthConfigId() {
        return authConfigId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAdditionalParams() {
        return additionalParams;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long authConfigId;
        private Long siteId;
        private AuthType authType;
        private String tokenEndpoint;
        private String clientId;
        private String clientSecret;
        private String additionalParams;

        public Builder authConfigId(Long authConfigId) {
            this.authConfigId = authConfigId;
            return this;
        }

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder authType(AuthType authType) {
            this.authType = authType;
            return this;
        }

        public Builder tokenEndpoint(String tokenEndpoint) {
            this.tokenEndpoint = tokenEndpoint;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder additionalParams(String additionalParams) {
            this.additionalParams = additionalParams;
            return this;
        }

        public SiteAuthConfig build() {
            return new SiteAuthConfig(authConfigId, siteId, authType, tokenEndpoint, clientId,
                                     clientSecret, additionalParams);
        }
    }

}
