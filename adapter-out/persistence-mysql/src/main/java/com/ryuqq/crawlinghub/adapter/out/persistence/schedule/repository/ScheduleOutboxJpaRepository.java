package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleOutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ScheduleOutbox JPA Repository (Command 전용)
 *
 * <p><strong>CQRS 패턴 적용 - Command 작업만 수행 ⭐</strong></p>
 * <ul>
 *   <li>✅ CUD (Create, Update, Delete) 작업만 제공</li>
 *   <li>✅ Query 메서드 없음 (ScheduleOutboxQueryAdapter에서 처리)</li>
 *   <li>✅ JPA 기본 메서드만 사용 (save, delete, findById)</li>
 * </ul>
 *
 * <p><strong>주의사항:</strong></p>
 * <ul>
 *   <li>❌ Query 메서드 (findBy..., @Query) 금지</li>
 *   <li>❌ 조회 로직은 ScheduleOutboxQueryAdapter에서 JPAQueryFactory 사용</li>
 *   <li>✅ Orchestrator의 모든 조회는 ScheduleOutboxQueryAdapter를 통해 수행</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-11
 */
@Repository
public interface ScheduleOutboxJpaRepository extends JpaRepository<ScheduleOutboxEntity, Long> {
    // ✅ Query 메서드 제거 완료
    // ✅ JPA 기본 메서드만 사용: save(), delete(), findById()
    // ✅ 조회 로직은 ScheduleOutboxQueryAdapter에서 JPAQueryFactory로 처리
}
