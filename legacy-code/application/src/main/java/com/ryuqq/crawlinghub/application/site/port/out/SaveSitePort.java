package com.ryuqq.crawlinghub.application.site.port.out;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;

/**
 * Command Port for Site persistence operations (CUD)
 * Follows CQRS pattern - Write operations only
 */
public interface SaveSitePort {

    /**
     * Save a site (create or update)
     * @param site the site to save
     * @return the saved site with generated ID if new
     */
    CrawlSite save(CrawlSite site);

    /**
     * Delete a site by ID
     * @param siteId the site ID to delete
     */
    void delete(SiteId siteId);

}
