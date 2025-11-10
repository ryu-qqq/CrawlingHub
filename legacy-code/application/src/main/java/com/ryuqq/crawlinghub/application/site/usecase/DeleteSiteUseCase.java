package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for site deletion
 * Implements CQRS Command pattern - Write operation
 *
 * Business Rules:
 * - Site must exist
 * - Uses soft delete (deactivation) instead of hard delete
 * - Related workflows should also be deactivated (TODO)
 */
@Service
public class DeleteSiteUseCase {

    private final SaveSitePort saveSitePort;
    private final LoadSitePort loadSitePort;

    public DeleteSiteUseCase(SaveSitePort saveSitePort, LoadSitePort loadSitePort) {
        this.saveSitePort = saveSitePort;
        this.loadSitePort = loadSitePort;
    }

    /**
     * Delete (deactivate) a site
     * Implements soft delete using domain model's deactivate() method
     *
     * @param siteId the site ID to delete
     * @throws SiteNotFoundException if site not found
     */
    @Transactional
    public void execute(SiteId siteId) {
        // 1. Load existing domain model
        CrawlSite site = loadSitePort.findById(siteId)
                .orElseThrow(() -> new SiteNotFoundException(siteId));

        // 2. Soft delete using domain model method
        site.deactivate();

        // 3. Save updated domain model
        saveSitePort.save(site);

        // TODO: Deactivate related workflows
        // This should be implemented when Workflow management is added
    }
}
