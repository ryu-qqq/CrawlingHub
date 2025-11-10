package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.application.site.port.out.LoadSitePort;
import com.ryuqq.crawlinghub.application.site.port.out.SaveSitePort;
import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence Adapter for CrawlSite
 * Implements both Command (Save) and Query (Load) ports
 * Follows CQRS pattern by delegating to appropriate repositories
 * Supports both Offset-Based and No-Offset pagination strategies
 */
@Component
public class SitePersistenceAdapter implements SaveSitePort, LoadSitePort {

    private final CrawlSiteJpaRepository jpaRepository;
    private final CrawlSiteQueryRepository queryRepository;
    private final SiteMapper mapper;

    public SitePersistenceAdapter(CrawlSiteJpaRepository jpaRepository,
                                   CrawlSiteQueryRepository queryRepository,
                                   SiteMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.queryRepository = queryRepository;
        this.mapper = mapper;
    }

    // ========================================
    // Command Port Implementation (SaveSitePort)
    // Uses JpaRepository for CUD operations
    // ========================================

    @Override
    public CrawlSite save(CrawlSite site) {
        CrawlSiteEntity entity = mapper.toEntity(site);
        CrawlSiteEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(SiteId siteId) {
        jpaRepository.deleteById(siteId.value());
    }

    // ========================================
    // Query Port Implementation (LoadSitePort)
    // Uses JpaRepository for simple find, QueryRepository for complex queries
    // ========================================

    @Override
    public Optional<CrawlSite> findById(SiteId siteId) {
        return jpaRepository.findById(siteId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlSite> findActiveSites() {
        return queryRepository.findActiveSites().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<CrawlSite> findActiveSites(Pageable pageable) {
        return queryRepository.findActiveSites(pageable)
                .map(mapper::toDomain);
    }

    @Override
    public List<CrawlSite> findActiveSites(Long lastSiteId, int pageSize) {
        return queryRepository.findActiveSites(lastSiteId, pageSize).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySiteName(String siteName) {
        return queryRepository.existsBySiteName(siteName);
    }

}
