package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.HttpMethod;

public class SiteApiEndpoint {

    private final Long endpointId;
    private final Long siteId;
    private final String endpointKey;
    private String endpointPath;
    private final HttpMethod httpMethod;
    private String description;

    private SiteApiEndpoint(Long endpointId, Long siteId, String endpointKey, String endpointPath,
                            HttpMethod httpMethod, String description) {
        this.endpointId = endpointId;
        this.siteId = siteId;
        this.endpointKey = endpointKey;
        this.endpointPath = endpointPath;
        this.httpMethod = httpMethod;
        this.description = description;
    }

    public void updateEndpointPath(String newPath) {
        this.endpointPath = newPath;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public Long getEndpointId() {
        return endpointId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public String getEndpointPath() {
        return endpointPath;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getDescription() {
        return description;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long endpointId;
        private Long siteId;
        private String endpointKey;
        private String endpointPath;
        private HttpMethod httpMethod;
        private String description;

        public Builder endpointId(Long endpointId) {
            this.endpointId = endpointId;
            return this;
        }

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder endpointKey(String endpointKey) {
            this.endpointKey = endpointKey;
            return this;
        }

        public Builder endpointPath(String endpointPath) {
            this.endpointPath = endpointPath;
            return this;
        }

        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public SiteApiEndpoint build() {
            return new SiteApiEndpoint(endpointId, siteId, endpointKey, endpointPath, httpMethod, description);
        }
    }

}
