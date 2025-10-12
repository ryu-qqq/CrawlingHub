package com.ryuqq.crawlinghub.adapter.persistence.jpa.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA Repository for CrawlSchedule
 * Provides basic CRUD operations through Spring Data JPA
 * For complex queries, use CrawlScheduleQueryRepository
 */
public interface CrawlScheduleJpaRepository extends JpaRepository<CrawlScheduleEntity, Long> {
}
