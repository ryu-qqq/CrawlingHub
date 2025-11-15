package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * TaskEntity JPA Repository
 *
 * <p>TaskEntity에 대한 기본 CRUD 작업을 제공하는 Spring Data JPA Repository입니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>findById(Long id) - ID로 Task 조회</li>
 *   <li>save(TaskEntity entity) - Task 저장/수정</li>
 *   <li>saveAll(Iterable) - 여러 Task 일괄 저장</li>
 *   <li>delete(TaskEntity entity) - Task 삭제</li>
 *   <li>findAll() - 전체 Task 조회</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Spring Data JPA Repository 패턴</li>
 *   <li>✅ @Repository 어노테이션으로 명시적 Bean 등록</li>
 *   <li>✅ JpaRepository 확장으로 기본 CRUD 자동 제공</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-12
 */
@Repository
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * Idempotency Key로 Task 조회
     *
     * <p>중복 작업 방지를 위해 Idempotency Key로 Task를 조회합니다.</p>
     *
     * @param idempotencyKey 멱등성 키
     * @return Optional&lt;TaskEntity&gt; 조회된 Task (존재하지 않으면 empty)
     */
    Optional<TaskEntity> findByIdempotencyKey(String idempotencyKey);
}
