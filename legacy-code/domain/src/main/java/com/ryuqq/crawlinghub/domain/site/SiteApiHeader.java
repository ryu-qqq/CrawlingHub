package com.ryuqq.crawlinghub.domain.site;

public class SiteApiHeader {

    private final Long headerId;
    private final Long endpointId;
    private final String headerKey;
    private String headerValue;
    private final Boolean isRequired;

    private SiteApiHeader(Long headerId, Long endpointId, String headerKey, String headerValue, Boolean isRequired) {
        this.headerId = headerId;
        this.endpointId = endpointId;
        this.headerKey = headerKey;
        this.headerValue = headerValue;
        this.isRequired = isRequired;
    }

    public void updateHeaderValue(String newValue) {
        this.headerValue = newValue;
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

        public SiteApiHeader build() {
            return new SiteApiHeader(headerId, endpointId, headerKey, headerValue, isRequired);
        }
    }

}
