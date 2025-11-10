package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.domain.common.AuthType;
import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "site_auth_config")
public class SiteAuthConfigEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_config_id")
    private Long authConfigId;

    @Column(name = "site_id", nullable = false, unique = true)
    private Long siteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 50)
    private AuthType authType;

    @Column(name = "token_endpoint", length = 500)
    private String tokenEndpoint;

    @Column(name = "client_id", length = 200)
    private String clientId;

    @Column(name = "client_secret", length = 500)
    private String clientSecret;

    @Column(name = "additional_params", columnDefinition = "JSON")
    private String additionalParams;

    protected SiteAuthConfigEntity() {
    }

    private SiteAuthConfigEntity(Long authConfigId, Long siteId, AuthType authType, String tokenEndpoint,
                          String clientId, String clientSecret, String additionalParams) {
        this.authConfigId = authConfigId;
        this.siteId = siteId;
        this.authType = authType;
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.additionalParams = additionalParams;
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

    public void updateCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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

        public SiteAuthConfigEntity build() {
            return new SiteAuthConfigEntity(authConfigId, siteId, authType, tokenEndpoint, clientId,
                                     clientSecret, additionalParams);
        }
    }

}
