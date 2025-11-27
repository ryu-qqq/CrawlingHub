package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.CrawledRawJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * CrawledRawJpaRepository - CrawledRaw JPA Repository
 *
 * <p>Spring Data JPA 기반의 기본 CRUD 저장소입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>CrawledRaw 엔티티 CRUD 오퍼레이션
 *   <li>Spring Data JPA 자동 구현
 * </ul>
 *
 * <p><strong>금지 사항:</strong>
 *
 * <ul>
 *   <li>❌ 비즈니스 로직 (Domain에서 처리)
 *   <li>❌ 복잡한 쿼리 (QueryDSL Repository로 분리)
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledRawJpaRepository extends JpaRepository<CrawledRawJpaEntity, Long> {}
