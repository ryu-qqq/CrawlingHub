package com.ryuqq.crawlinghub.adapter.persistence.jpa.task;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for CrawlTask
 * Used for simple CRUD operations
 * Complex queries should use CrawlTaskQueryRepository
 */
public interface CrawlTaskJpaRepository extends JpaRepository<CrawlTaskEntity, Long> {
}
