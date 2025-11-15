package com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.crawl.task.entity.TaskMessageOutBoxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TaskMessageOutBoxEntity JPA Repository
 *
 * <p>TaskMessageOutBoxEntity에 대한 기본 CRUD 작업을 제공하는 Spring Data JPA Repository입니다.</p>
 *
 * <p><strong>제공 기능:</strong></p>
 * <ul>
 *   <li>findById(Long outboxId) - Outbox ID로 조회</li>
 *   <li>save(TaskMessageOutBoxEntity entity) - Outbox 저장/수정</li>
 *   <li>saveAll(Iterable) - 여러 Outbox 일괄 저장</li>
 *   <li>delete(TaskMessageOutBoxEntity entity) - Outbox 삭제</li>
 *   <li>findAll() - 전체 Outbox 조회</li>
 *   <li>findByStatus(String status) - 상태별 조회 (Custom Query Method)</li>
 * </ul>
 *
 * <p><strong>Pessimistic Lock:</strong></p>
 * <ul>
 *   <li>findByStatusWithLock()은 QueryDSL Repository에서 구현</li>
 *   <li>동시성 제어가 필요한 Scheduler 조회 시 사용</li>
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
public interface TaskMessageOutBoxJpaRepository extends JpaRepository<TaskMessageOutBoxEntity, Long> {

    /**
     * 상태별 Outbox 조회
     *
     * <p>특정 상태(PENDING, SENT)의 Outbox 레코드들을 조회합니다.</p>
     *
     * <p><strong>주의:</strong></p>
     * <ul>
     *   <li>Lock 없이 조회하므로 단순 조회용으로만 사용</li>
     *   <li>Scheduler에서 처리할 PENDING 레코드 조회 시에는 findByStatusWithLock() 사용</li>
     * </ul>
     *
     * @param status Outbox 상태 (String - "PENDING", "SENT")
     * @return List&lt;TaskMessageOutBoxEntity&gt; 해당 상태의 Outbox 목록
     */
    List<TaskMessageOutBoxEntity> findByStatus(String status);
}
