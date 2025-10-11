package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;

/**
 * Response DTO for site detail view
 * Must be an immutable Java record (enforced by architecture tests)
 * Contains all site information including relationships
 *
 * @param siteId the site ID
 * @param siteName the site name
 * @param baseUrl the base URL
 * @param siteType the site type
 * @param isActive whether the site is active
 */
public record SiteDetailResponse(
        Long siteId,
        String siteName,
        String baseUrl,
        String siteType,
        Boolean isActive
) {

    /**
     * Create SiteDetailResponse from domain model
     * TODO: Add related entities (API endpoints, auth config, etc.) when aggregate is complete
     *
     * @param site the domain model
     * @return detail response DTO
     */
    public static SiteDetailResponse from(CrawlSite site) {
        return new SiteDetailResponse(
                site.getSiteId().value(),
                site.getSiteName(),
                site.getBaseUrl(),
                site.getSiteType().name(),
                site.isActive()
        );
    }
}
