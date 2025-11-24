package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawlSchedulerHistoryJpaRepository - CrawlSchedulerHistory JPA Repository
 *
 * <p>Spring Data JPA 기본 인터페이스입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerHistoryJpaRepository
        extends JpaRepository<CrawlSchedulerHistoryJpaEntity, Long> {}
