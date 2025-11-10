package com.ryuqq.crawlinghub.adapter.persistence.jpa.execution;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Command Repository for CrawlExecution (CQRS Pattern)
 * Handles only CUD operations using JpaRepository
 *
 * Complex queries should use CrawlExecutionQueryRepository (QueryDSL)
 */
public interface CrawlExecutionJpaRepository extends JpaRepository<CrawlExecutionEntity, Long> {
    // Inherits basic CRUD methods from JpaRepository
}
