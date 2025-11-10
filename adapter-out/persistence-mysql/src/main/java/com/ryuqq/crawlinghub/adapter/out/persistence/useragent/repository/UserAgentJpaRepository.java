package com.ryuqq.crawlinghub.adapter.out.persistence.useragent.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.useragent.entity.UserAgentEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserAgent JPA Repository
 * <p>
 * Spring Data JPA 인터페이스를 사용하여 기본 CRUD를 제공합니다.
 * </p>
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ Write 작업 전용 (save, delete)</li>
 *   <li>❌ Query 작업은 QueryRepository에서 처리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-05
 */
@Repository
public interface UserAgentJpaRepository extends JpaRepository<UserAgentEntity, Long> {
}



