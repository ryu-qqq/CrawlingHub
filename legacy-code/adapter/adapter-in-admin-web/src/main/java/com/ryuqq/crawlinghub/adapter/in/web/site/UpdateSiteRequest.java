package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.application.site.usecase.UpdateSiteCommand;
import com.ryuqq.crawlinghub.domain.site.SiteId;
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

        @Pattern(
                regexp = "^https?://(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
                message = "Base URL must be a valid HTTP/HTTPS URL"
        )
        String baseUrl,

        String siteType,

        Boolean isActive

) {
    /**
     * Convert request DTO to command object
     * Encapsulates conversion logic within the DTO
     *
     * @param siteId the site ID from path parameter
     * @return update site command
     */
    public UpdateSiteCommand toCommand(SiteId siteId) {
        return new UpdateSiteCommand(siteId, siteName, baseUrl, siteType, isActive);
    }
}
