package com.ryuqq.crawlinghub.application.site.usecase;

/**
 * Command object for site registration
 * Immutable record for CQRS Command pattern
 * Used by RegisterSiteUseCase
 *
 * @param siteName the site name
 * @param baseUrl the base URL
 * @param siteType the site type (string representation of SiteType enum)
 */
public record RegisterSiteCommand(
        String siteName,
        String baseUrl,
        String siteType
) {
}
