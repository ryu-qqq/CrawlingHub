package com.ryuqq.crawlinghub.application.site.port.out;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Query Port for Site read operations
 * Follows CQRS pattern - Read operations only
 * Complex queries should be implemented using QueryDSL
 */
public interface LoadSitePort {

    /**
     * Find a site by ID
     * @param siteId the site ID
     * @return Optional containing the site if found
     */
    Optional<CrawlSite> findById(SiteId siteId);

    /**
     * Find all active sites
     * @return list of active sites
     */
    List<CrawlSite> findActiveSites();

    /**
     * Find active sites with pagination (Offset-Based)
     * Suitable for UI pagination with page numbers (< 10,000 records)
     * @param pageable pagination parameters (page, size, sort)
     * @return page of active sites
     */
    Page<CrawlSite> findActiveSites(Pageable pageable);

    /**
     * Find active sites with cursor-based pagination (No-Offset)
     * Performance-optimized for large datasets (> 10,000 records)
     * @param lastSiteId cursor - last site ID from previous page (null for first page)
     * @param pageSize number of records to fetch
     * @return list of active sites after the cursor
     */
    List<CrawlSite> findActiveSites(Long lastSiteId, int pageSize);

    /**
     * Check if a site name already exists
     * @param siteName the site name to check
     * @return true if exists, false otherwise
     */
    boolean existsBySiteName(String siteName);

}
