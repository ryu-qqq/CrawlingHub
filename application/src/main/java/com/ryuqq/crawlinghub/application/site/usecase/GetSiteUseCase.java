package com.ryuqq.crawlinghub.application.site.usecase;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use Case for site retrieval
 * Implements CQRS Query pattern - Read operation
 * Uses QueryDSL for complex queries (via LoadSitePort)
 */
@Service
@Transactional(readOnly = true)
public class GetSiteUseCase {

    private final LoadSitePort loadSitePort;

    public GetSiteUseCase(LoadSitePort loadSitePort) {
        this.loadSitePort = loadSitePort;
    }

    /**
     * Get all active sites
     * TODO: Add pagination support when adapter layer can handle Page response
     *
     * @return list of active sites
     */
    public List<CrawlSite> getAllActiveSites() {
        return loadSitePort.findActiveSites();
    }

    /**
     * Get site detail by ID
     *
     * @param siteId the site ID
     * @return the site detail
     * @throws SiteNotFoundException if site not found
     */
    public CrawlSite getDetail(SiteId siteId) {
        return loadSitePort.findById(siteId)
                .orElseThrow(() -> new SiteNotFoundException(siteId));
    }
}
