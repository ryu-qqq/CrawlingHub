package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * CrawlSchedule JPA Repository
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Spring Data JPA 사용</li>
 *   <li>✅ Query 메서드 네이밍 규칙 준수</li>
 *   <li>✅ Optional 반환 (findBy...)</li>
 *   <li>✅ List 반환 (findAllBy...)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public interface ScheduleJpaRepository extends JpaRepository<ScheduleEntity, Long> {

    /**
     * Seller ID로 활성 스케줄 조회
     *
     * @param sellerId 셀러 ID (Long FK)
     * @param status 스케줄 상태
     * @return 활성 스케줄 (Optional)
     */
    Optional<ScheduleEntity> findBySellerIdAndStatus(Long sellerId, ScheduleStatus status);

    /**
     * Seller ID로 모든 스케줄 조회
     *
     * @param sellerId 셀러 ID (Long FK)
     * @return 스케줄 목록
     */
    List<ScheduleEntity> findAllBySellerId(Long sellerId);

    /**
     * Seller ID와 상태로 스케줄 존재 여부 확인
     *
     * @param sellerId 셀러 ID
     * @param status 스케줄 상태
     * @return 존재 여부
     */
    boolean existsBySellerIdAndStatus(Long sellerId, ScheduleStatus status);

    /**
     * 활성 스케줄 목록 조회 (다음 실행 시간 순)
     *
     * @param status 스케줄 상태
     * @return 활성 스케줄 목록
     */
    @Query("SELECT s FROM ScheduleEntity s WHERE s.status = :status AND s.nextExecutionTime IS NOT NULL ORDER BY s.nextExecutionTime ASC")
    List<ScheduleEntity> findActiveSchedulesOrderByNextExecution(@Param("status") ScheduleStatus status);
}
