package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import com.ryuqq.crawlinghub.domain.site.CrawlSite;
import com.ryuqq.crawlinghub.domain.site.SiteId;
import org.springframework.stereotype.Component;

/**
 * Mapper between CrawlSite domain model and CrawlSiteEntity
 * Handles bidirectional conversion
 */
@Component
public class SiteMapper {

    /**
     * Convert domain model to JPA entity
     * @param domain the domain model
     * @return JPA entity
     */
    public CrawlSiteEntity toEntity(CrawlSite domain) {
        return CrawlSiteEntity.builder()
                .siteId(domain.getSiteId() != null ? domain.getSiteId().value() : null)
                .siteName(domain.getSiteName())
                .baseUrl(domain.getBaseUrl())
                .siteType(domain.getSiteType())
                .isActive(domain.isActive())
                .build();
    }

    /**
     * Convert JPA entity to domain model
     * @param entity the JPA entity
     * @return domain model
     */
    public CrawlSite toDomain(CrawlSiteEntity entity) {
        return CrawlSite.reconstitute(
                new SiteId(entity.getSiteId()),
                entity.getSiteName(),
                entity.getBaseUrl(),
                entity.getSiteType(),
                entity.getIsActive()
        );
    }

}
