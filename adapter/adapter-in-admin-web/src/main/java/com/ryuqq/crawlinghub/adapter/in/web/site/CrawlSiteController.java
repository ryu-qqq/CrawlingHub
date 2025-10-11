package com.ryuqq.crawlinghub.adapter.in.web.site;

import com.ryuqq.crawlinghub.application.site.usecase.*;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Crawl Site management
 * Thin delegation layer following Hexagonal Architecture
 *
 * Architecture Rules (enforced by tests):
 * - NO @Transactional (transaction is in application layer)
 * - Depends ONLY on UseCase interfaces
 * - Request/Response must be Java records
 * - NO Lombok allowed
 * - Controller methods should be thin (max 3 parameters recommended)
 */
@RestController
@RequestMapping("/api/v1/sites")
public class CrawlSiteController {

    private final RegisterSiteUseCase registerSiteUseCase;
    private final GetSiteUseCase getSiteUseCase;
    private final UpdateSiteUseCase updateSiteUseCase;
    private final DeleteSiteUseCase deleteSiteUseCase;

    public CrawlSiteController(RegisterSiteUseCase registerSiteUseCase,
                               GetSiteUseCase getSiteUseCase,
                               UpdateSiteUseCase updateSiteUseCase,
                               DeleteSiteUseCase deleteSiteUseCase) {
        this.registerSiteUseCase = registerSiteUseCase;
        this.getSiteUseCase = getSiteUseCase;
        this.updateSiteUseCase = updateSiteUseCase;
        this.deleteSiteUseCase = deleteSiteUseCase;
    }

    /**
     * Create a new crawl site
     *
     * @param request the site creation request
     * @return the created site response
     */
    @PostMapping
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody CreateSiteRequest request) {
        // Request → Command (encapsulated in DTO)
        RegisterSiteCommand command = request.toCommand();

        // Execute UseCase (returns created site directly - no additional DB query needed)
        CrawlSite site = registerSiteUseCase.execute(command);
        SiteResponse response = SiteResponse.from(site);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get list of active sites
     * Note: Pagination support will be added when adapter layer supports Page response
     * Current implementation returns all active sites
     *
     * @return list of site summaries
     */
    @GetMapping
    public ResponseEntity<List<SiteSummaryResponse>> getSites() {
        // Get all active sites (no pagination yet)
        List<CrawlSite> sites = getSiteUseCase.getAllActiveSites();

        // Domain → Response
        List<SiteSummaryResponse> response = sites.stream()
                .map(SiteSummaryResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Get site detail by ID
     *
     * @param siteId the site ID
     * @return the site detail
     */
    @GetMapping("/{siteId}")
    public ResponseEntity<SiteDetailResponse> getSiteDetail(@PathVariable Long siteId) {
        // Execute UseCase
        CrawlSite site = getSiteUseCase.getDetail(SiteId.of(siteId));

        // Domain → Response
        SiteDetailResponse response = SiteDetailResponse.from(site);

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing site
     *
     * @param siteId the site ID
     * @param request the update request
     * @return no content
     */
    @PutMapping("/{siteId}")
    public ResponseEntity<Void> updateSite(
            @PathVariable Long siteId,
            @Valid @RequestBody UpdateSiteRequest request) {

        // Request → Command (encapsulated in DTO)
        UpdateSiteCommand command = request.toCommand(SiteId.of(siteId));

        // Execute UseCase
        updateSiteUseCase.execute(command);

        return ResponseEntity.noContent().build();
    }

    /**
     * Delete (deactivate) a site
     *
     * @param siteId the site ID
     * @return no content
     */
    @DeleteMapping("/{siteId}")
    public ResponseEntity<Void> deleteSite(@PathVariable Long siteId) {
        // Execute UseCase
        deleteSiteUseCase.execute(SiteId.of(siteId));

        return ResponseEntity.noContent().build();
    }
}
