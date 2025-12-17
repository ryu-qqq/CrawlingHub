package com.ryuqq.crawlinghub.adapter.out.persistence.product.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.product.entity.SyncOutboxJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SyncOutboxJpaRepository - SyncOutbox JPA Repository
 *
 * <p>기본 CRUD 작업을 위한 Spring Data JPA Repository입니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>기본 CRUD (save, findById, delete 등)
 *   <li>복잡한 쿼리는 QueryDslRepository에서 처리
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
public interface SyncOutboxJpaRepository extends JpaRepository<SyncOutboxJpaEntity, Long> {}
