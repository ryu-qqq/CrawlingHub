package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.HttpMethod;
import jakarta.persistence.*;

@Entity
@Table(name = "site_api_endpoint")
public class SiteApiEndpointEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "endpoint_id")
    private Long endpointId;

    @Column(name = "site_id", nullable = false)
    private Long siteId;

    @Column(name = "endpoint_key", nullable = false, length = 100, unique = true)
    private String endpointKey;

    @Column(name = "endpoint_path", nullable = false, length = 500)
    private String endpointPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 20)
    private HttpMethod httpMethod;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected SiteApiEndpointEntity() {
    }

    private SiteApiEndpointEntity(Long endpointId, Long siteId, String endpointKey, String endpointPath,
                           HttpMethod httpMethod, String description) {
        this.endpointId = endpointId;
        this.siteId = siteId;
        this.endpointKey = endpointKey;
        this.endpointPath = endpointPath;
        this.httpMethod = httpMethod;
        this.description = description;
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

    public void updateEndpointPath(String newPath) {
        this.endpointPath = newPath;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
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

        public SiteApiEndpointEntity build() {
            return new SiteApiEndpointEntity(endpointId, siteId, endpointKey, endpointPath, httpMethod, description);
        }
    }

}
