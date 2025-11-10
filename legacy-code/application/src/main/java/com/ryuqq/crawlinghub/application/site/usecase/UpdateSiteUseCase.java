package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for site update
 * Implements CQRS Command pattern - Write operation
 *
 * Business Rules:
 * - Site must exist
 * - Only provided fields are updated (partial update)
 * - Uses domain model reconstitution to preserve immutability
 */
@Service
public class UpdateSiteUseCase {

    private final SaveSitePort saveSitePort;
    private final LoadSitePort loadSitePort;

    public UpdateSiteUseCase(SaveSitePort saveSitePort, LoadSitePort loadSitePort) {
        this.saveSitePort = saveSitePort;
        this.loadSitePort = loadSitePort;
    }

    /**
     * Update an existing site
     * Note: Current implementation only supports activation/deactivation
     * Full update with siteName, baseUrl, siteType requires domain model enhancement
     *
     * @param command the update command
     * @throws SiteNotFoundException if site not found
     */
    @Transactional
    public void execute(UpdateSiteCommand command) {
        // 1. Load existing domain model
        CrawlSite site = loadSitePort.findById(command.siteId())
                .orElseThrow(() -> new SiteNotFoundException(command.siteId()));

        // 2. Apply business logic through domain model methods
        if (command.isActive() != null) {
            if (command.isActive()) {
                site.activate();
            } else {
                site.deactivate();
            }
        }

        // TODO: Handle siteName, baseUrl, siteType updates
        // Current domain model (CrawlSite) is immutable after creation
        // Need to enhance domain model to support field updates or use reconstitution

        // 3. Save updated domain model
        saveSitePort.save(site);
    }
}
