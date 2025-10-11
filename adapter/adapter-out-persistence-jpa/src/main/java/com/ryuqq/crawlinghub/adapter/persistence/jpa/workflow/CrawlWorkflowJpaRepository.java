package com.ryuqq.crawlinghub.adapter.persistence.jpa.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Command Repository for CrawlWorkflow (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 * - save(entity): Create or Update
 * - deleteById(id): Delete
 * - existsById(id): Existence check
 *
 * Complex queries should use CrawlWorkflowQueryRepository (QueryDSL)
 */
public interface CrawlWorkflowJpaRepository extends JpaRepository<CrawlWorkflowEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
    // - findById(Long id)
    // - save(CrawlWorkflowEntity entity)
    // - deleteById(Long id)
    // - existsById(Long id)

    // NO custom query methods here!
    // All complex queries belong in CrawlWorkflowQueryRepository
}
