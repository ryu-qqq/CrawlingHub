package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;

/**
 * Response DTO for site creation and update operations
 * Must be an immutable Java record (enforced by architecture tests)
 *
 * @param siteId the site ID
 * @param siteName the site name
 * @param baseUrl the base URL
 * @param siteType the site type
 * @param isActive whether the site is active
 */
public record SiteResponse(
        Long siteId,
        String siteName,
        String baseUrl,
        String siteType,
        Boolean isActive
) {

    /**
     * Create SiteResponse from domain model
     *
     * @param site the domain model
     * @return response DTO
     */
    public static SiteResponse from(CrawlSite site) {
        return new SiteResponse(
                site.getIdValue(),
                site.getSiteName(),
                site.getBaseUrl(),
                site.getSiteType().name(),
                site.isActive()
        );
    }
}
