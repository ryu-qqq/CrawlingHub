package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.CrawlSchedulerOutBoxJpaEntity;
import com.ryuqq.crawlinghub.domain.schedule.vo.CrawlSchedulerOubBoxStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * CrawlSchedulerOutBoxJpaRepository - CrawlSchedulerOutBox JPA Repository
 *
 * <p>Spring Data JPA 기본 인터페이스입니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawlSchedulerOutBoxJpaRepository
        extends JpaRepository<CrawlSchedulerOutBoxJpaEntity, Long> {

    /**
     * 상태별 아웃박스 목록 조회
     *
     * @param status 조회할 상태
     * @param pageable 페이징 정보
     * @return 아웃박스 엔티티 목록
     */
    List<CrawlSchedulerOutBoxJpaEntity> findByStatus(
            CrawlSchedulerOubBoxStatus status, Pageable pageable);

    /**
     * PENDING 또는 FAILED 상태의 아웃박스 조회
     *
     * @param pageable 페이징 정보
     * @return 재처리 대상 아웃박스 목록
     */
    @Query(
            "SELECT o FROM CrawlSchedulerOutBoxJpaEntity o WHERE o.status IN :statuses ORDER BY"
                    + " o.createdAt ASC")
    List<CrawlSchedulerOutBoxJpaEntity> findByStatusIn(
            @Param("statuses") List<CrawlSchedulerOubBoxStatus> statuses, Pageable pageable);
}
