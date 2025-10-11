package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;

/**
 * Response DTO for site list (summary view)
 * Must be an immutable Java record (enforced by architecture tests)
 * Contains only essential information for list display
 *
 * @param siteId the site ID
 * @param siteName the site name
 * @param baseUrl the base URL
 * @param siteType the site type
 * @param isActive whether the site is active
 */
public record SiteSummaryResponse(
        Long siteId,
        String siteName,
        String baseUrl,
        String siteType,
        Boolean isActive
) {

    /**
     * Create SiteSummaryResponse from domain model
     *
     * @param site the domain model
     * @return summary response DTO
     */
    public static SiteSummaryResponse from(CrawlSite site) {
        return new SiteSummaryResponse(
                site.getSiteId().value(),
                site.getSiteName(),
                site.getBaseUrl(),
                site.getSiteType().name(),
                site.isActive()
        );
    }
}
