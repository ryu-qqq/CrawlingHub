package com.ryuqq.crawlinghub.adapter.persistence.jpa.site;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Command Repository for CrawlSite (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 * - save(entity): Create or Update
 * - deleteById(id): Delete
 * - existsById(id): Existence check
 *
 * Complex queries should use CrawlSiteQueryRepository (QueryDSL)
 */
public interface CrawlSiteJpaRepository extends JpaRepository<CrawlSiteEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
    // - findById(Long id)
    // - save(CrawlSiteEntity entity)
    // - deleteById(Long id)
    // - existsById(Long id)

    // NO custom query methods here!
    // All complex queries belong in CrawlSiteQueryRepository
}
