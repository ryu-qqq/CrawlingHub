package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawlSchedulerOutBoxJpaRepository - CrawlSchedulerOutBox JPA Repository
 *
 * <p>Spring Data JPA 기본 인터페이스입니다.
 *
 * <p>저장/삭제 전용이며, 조회는 {@link CrawlSchedulerOutBoxQueryDslRepository}를 사용합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerOutBoxJpaRepository
        extends JpaRepository<CrawlSchedulerOutBoxJpaEntity, Long> {}
