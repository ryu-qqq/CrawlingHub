package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for site registration
 * Implements CQRS Command pattern - Write operation
 * Transaction boundary is at Application layer
 *
 * Business Rules:
 * - Site name must be unique
 * - Site type must be valid enum value
 * - Uses Domain Model factory method (CrawlSite.create())
 */
@Service
public class RegisterSiteUseCase {

    private final SaveSitePort saveSitePort;
    private final LoadSitePort loadSitePort;

    public RegisterSiteUseCase(SaveSitePort saveSitePort, LoadSitePort loadSitePort) {
        this.saveSitePort = saveSitePort;
        this.loadSitePort = loadSitePort;
    }

    /**
     * Register a new crawl site
     *
     * @param command the registration command
     * @return the site ID of the created site
     * @throws DuplicateSiteException if site name already exists
     * @throws IllegalArgumentException if site type is invalid
     */
    @Transactional
    public SiteId execute(RegisterSiteCommand command) {
        // 1. Validate business rules
        if (loadSitePort.existsBySiteName(command.siteName())) {
            throw new DuplicateSiteException("Site name already exists: " + command.siteName());
        }

        // 2. Parse and validate site type
        SiteType siteType;
        try {
            siteType = SiteType.valueOf(command.siteType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid site type: " + command.siteType() +
                    ". Valid types: REST_API, GRAPHQL, WEB_SCRAPING, RSS_FEED, SOAP, CUSTOM");
        }

        // 3. Create Domain Model using factory method
        CrawlSite site = CrawlSite.create(
                command.siteName(),
                command.baseUrl(),
                siteType
        );

        // 4. Save domain model (Adapter will convert to JPA Entity)
        CrawlSite saved = saveSitePort.save(site);

        return saved.getSiteId();
    }
}
