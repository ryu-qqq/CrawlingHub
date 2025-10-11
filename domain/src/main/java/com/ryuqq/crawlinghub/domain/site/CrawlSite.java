package com.ryuqq.crawlinghub.domain.site;

import com.ryuqq.crawlinghub.domain.common.SiteType;

public class CrawlSite {

    private final SiteId siteId;
    private final String siteName;
    private final String baseUrl;
    private final SiteType siteType;
    private boolean isActive;

    private CrawlSite(SiteId siteId, String siteName, String baseUrl, SiteType siteType, boolean isActive) {
        this.siteId = siteId;
        this.siteName = siteName;
        this.baseUrl = baseUrl;
        this.siteType = siteType;
        this.isActive = isActive;
    }

    public static CrawlSite create(String siteName, String baseUrl, SiteType siteType) {
        validateCreate(siteName, baseUrl, siteType);
        return new CrawlSite(null, siteName, baseUrl, siteType, true);
    }

    public static CrawlSite reconstitute(SiteId siteId, String siteName, String baseUrl,
                                        SiteType siteType, boolean isActive) {
        return new CrawlSite(siteId, siteName, baseUrl, siteType, isActive);
    }

    private static void validateCreate(String siteName, String baseUrl, SiteType siteType) {
        if (siteName == null || siteName.isBlank()) {
            throw new IllegalArgumentException("Site name cannot be null or blank");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("Base URL cannot be null or blank");
        }
        if (siteType == null) {
            throw new IllegalArgumentException("Site type cannot be null");
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public SiteId getSiteId() {
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

    public boolean isActive() {
        return isActive;
    }

}
