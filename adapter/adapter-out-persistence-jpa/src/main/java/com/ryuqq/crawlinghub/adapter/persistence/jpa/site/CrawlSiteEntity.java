package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.adapter.persistence.jpa.common.BaseTimeEntity;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import jakarta.persistence.*;

@Entity
@Table(name = "crawl_site")
public class CrawlSiteEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "site_name", nullable = false, length = 100)
    private String siteName;

    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "site_type", nullable = false, length = 50)
    private SiteType siteType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    protected CrawlSiteEntity() {
    }

    private CrawlSiteEntity(Long siteId, String siteName, String baseUrl, SiteType siteType, Boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.baseUrl = baseUrl;
        this.siteType = siteType;
        this.isActive = isActive;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long siteId;
        private String siteName;
        private String baseUrl;
        private SiteType siteType;
        private Boolean isActive;

        public Builder siteId(Long siteId) {
            this.siteId = siteId;
            return this;
        }

        public Builder siteName(String siteName) {
            this.siteName = siteName;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder siteType(SiteType siteType) {
            this.siteType = siteType;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public CrawlSiteEntity build() {
            return new CrawlSiteEntity(siteId, siteName, baseUrl, siteType, isActive);
        }
    }

}
