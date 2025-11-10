package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.application.site.usecase.RegisterSiteCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating a new crawl site
 * Must be an immutable Java record (enforced by architecture tests)
 *
 * @param siteName the name of the site (required, not blank)
 * @param baseUrl the base URL of the site (required, valid URL format)
 * @param siteType the type of site (required, must match SiteType enum)
 */
public record CreateSiteRequest(

        @NotBlank(message = "Site name is required")
        String siteName,

        @NotBlank(message = "Base URL is required")
        @Pattern(
                regexp = "^https?://(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)",
                message = "Base URL must be a valid HTTP/HTTPS URL"
        )
        String baseUrl,

        @NotBlank(message = "Site type is required")
        String siteType

) {
    /**
     * Convert request DTO to command object
     * Encapsulates conversion logic within the DTO
     *
     * @return register site command
     */
    public RegisterSiteCommand toCommand() {
        return new RegisterSiteCommand(siteName, baseUrl, siteType);
    }
}
