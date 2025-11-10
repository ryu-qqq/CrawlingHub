package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "site_api_header")
public class SiteApiHeaderEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "header_id")
    private Long headerId;

    @Column(name = "endpoint_id", nullable = false)
    private Long endpointId;

    @Column(name = "header_key", nullable = false, length = 100)
    private String headerKey;

    @Column(name = "header_value", nullable = false, length = 500)
    private String headerValue;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;

    protected SiteApiHeaderEntity() {
    }

    private SiteApiHeaderEntity(Long headerId, Long endpointId, String headerKey, String headerValue, Boolean isRequired) {
        this.headerId = headerId;
        this.endpointId = endpointId;
        this.headerKey = headerKey;
        this.headerValue = headerValue;
        this.isRequired = isRequired;
    }

    public Long getHeaderId() {
        return headerId;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void updateHeaderValue(String newValue) {
        this.headerValue = newValue;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long headerId;
        private Long endpointId;
        private String headerKey;
        private String headerValue;
        private Boolean isRequired;

        public Builder headerId(Long headerId) {
            this.headerId = headerId;
            return this;
        }

        public Builder endpointId(Long endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public Builder headerKey(String headerKey) {
            this.headerKey = headerKey;
            return this;
        }

        public Builder headerValue(String headerValue) {
            this.headerValue = headerValue;
            return this;
        }

        public Builder isRequired(Boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public SiteApiHeaderEntity build() {
            return new SiteApiHeaderEntity(headerId, endpointId, headerKey, headerValue, isRequired);
        }
    }

}
