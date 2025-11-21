package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawlSchedulerJpaRepository - CrawlScheduler JPA Repository
 *
 * <p>Spring Data JPA 기본 인터페이스입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>기본 CRUD 연산 (save, findById, delete)
 *   <li>CommandAdapter에서 사용
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 커스텀 쿼리 메서드 정의 (QueryDslRepository 사용)
 *   <li>❌ @Query 어노테이션 사용
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerJpaRepository extends JpaRepository<CrawlSchedulerJpaEntity, Long> {}
