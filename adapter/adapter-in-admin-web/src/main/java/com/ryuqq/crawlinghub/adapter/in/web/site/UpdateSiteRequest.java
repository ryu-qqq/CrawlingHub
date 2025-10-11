package com.ryuqq.crawlinghub.adapter.in.web.site;

import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for updating an existing crawl site
 * Must be an immutable Java record (enforced by architecture tests)
 * All fields are optional - only provided fields will be updated
 *
 * @param siteName the new name of the site (optional)
 * @param baseUrl the new base URL (optional, must be valid if provided)
 * @param siteType the new site type (optional)
 * @param isActive the new active status (optional)
 */
public record UpdateSiteRequest(

        String siteName,

        @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP/HTTPS URL")
        String baseUrl,

        String siteType,

        Boolean isActive

) {
}
