package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.domain.site.SiteId;

/**
 * Command object for site update
 * Immutable record for CQRS Command pattern
 * Used by UpdateSiteUseCase
 *
 * @param siteId the site ID to update
 * @param siteName the new site name (nullable - no update if null)
 * @param baseUrl the new base URL (nullable - no update if null)
 * @param siteType the new site type (nullable - no update if null)
 * @param isActive the new active status (nullable - no update if null)
 */
public record UpdateSiteCommand(
        SiteId siteId,
        String siteName,
        String baseUrl,
        String siteType,
        Boolean isActive
) {
}
