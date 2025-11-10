package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.common.SiteType;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.dao.DataIntegrityViolationException;
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
     * @return the created site with generated ID
     * @throws DuplicateSiteException if site name already exists
     * @throws IllegalArgumentException if site type is invalid
     */
    @Transactional
    public CrawlSite execute(RegisterSiteCommand command) {
        // 1. Fast-fail validation for duplicate site names (optimization)
        if (loadSitePort.existsBySiteName(command.siteName())) {
            throw new DuplicateSiteException("Site name already exists: " + command.siteName());
        }

        // 2. Parse and validate site type (case-insensitive, dynamic enum list)
        SiteType siteType;
        try {
            siteType = SiteType.valueOf(command.siteType().toUpperCase());
        } catch (IllegalArgumentException e) {
            String validTypes = java.util.Arrays.stream(SiteType.values())
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException("Invalid site type: " + command.siteType() +
                    ". Valid types: " + validTypes);
        }

        // 3. Create Domain Model using factory method
        CrawlSite site = CrawlSite.create(
                command.siteName(),
                command.baseUrl(),
                siteType
        );

        // 4. Save domain model (Adapter will convert to JPA Entity)
        // Handle race condition: catch DataIntegrityViolationException for concurrent requests
        try {
            return saveSitePort.save(site);
        } catch (DataIntegrityViolationException e) {
            // Race condition detected: another request created the same site name
            throw new DuplicateSiteException("Site name already exists: " + command.siteName());
        }
    }
}
