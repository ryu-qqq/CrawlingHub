package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.result.entity.CrawlResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawlResult JPA Repository
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
public interface CrawlResultJpaRepository extends JpaRepository<CrawlResultEntity, Long> {
}
