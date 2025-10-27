package com.ryuqq.crawlinghub.adapter.out.persistence.outbox.repository;

import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity.OperationState;
import com.ryuqq.crawlinghub.adapter.out.persistence.outbox.entity.SellerCrawlScheduleOutboxEntity.WriteAheadState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SellerCrawlScheduleOutbox JPA Repository
 * <p>
 * Orchestrator Store SPI 구현을 위한 JPA Repository입니다.
 * </p>
 * <p>
 * 주요 쿼리 메서드:
 * <ul>
 *   <li>findByOpId: OpId로 Outbox 조회</li>
 *   <li>findByIdemKey: Idempotency Key로 Outbox 조회</li>
 *   <li>findWALPending: WAL 상태가 PENDING인 Outbox 조회 (Finalizer용)</li>
 *   <li>findInProgressAndTimeout: IN_PROGRESS 상태이면서 타임아웃된 Outbox 조회 (Reaper용)</li>
 * </ul>
 * </p>
 *
 * @author Claude (claude@anthropic.com)
 * @since 1.0
 */
@Repository
public interface SellerCrawlScheduleOutboxJpaRepository
        extends JpaRepository<SellerCrawlScheduleOutboxEntity, Long> {

    /**
     * OpId로 Outbox 조회
     * <p>
     * Orchestrator의 모든 작업은 OpId로 식별되므로 핵심 조회 메서드입니다.
     * </p>
     *
     * @param opId Orchestrator OpId (UUID String)
     * @return Outbox Entity (Optional)
     */
    Optional<SellerCrawlScheduleOutboxEntity> findByOpId(String opId);

    /**
     * Idempotency Key로 Outbox 조회
     * <p>
     * 중복 실행 방지를 위한 멱등성 검증에 사용됩니다.
     * </p>
     *
     * @param idemKey Idempotency Key
     * @return Outbox Entity (Optional)
     */
    Optional<SellerCrawlScheduleOutboxEntity> findByIdemKey(String idemKey);

    /**
     * Seller ID로 Outbox 목록 조회 (최신순)
     * <p>
     * 특정 Seller의 스케줄 등록 이력을 조회할 때 사용합니다.
     * </p>
     *
     * @param sellerId Seller PK (Long FK)
     * @return Outbox Entity 목록
     */
    List<SellerCrawlScheduleOutboxEntity> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    /**
     * Seller ID와 상태로 Outbox 목록 조회
     * <p>
     * 특정 Seller의 특정 상태의 스케줄 등록 건들을 조회합니다.
     * </p>
     *
     * @param sellerId       Seller PK (Long FK)
     * @param operationState 작업 상태
     * @return Outbox Entity 목록
     */
    List<SellerCrawlScheduleOutboxEntity> findBySellerIdAndOperationState(
            Long sellerId,
            OperationState operationState
    );

    /**
     * WAL 상태가 PENDING인 Outbox 조회 (Finalizer용)
     * <p>
     * Orchestrator Finalizer가 주기적으로 호출하여
     * Write-Ahead Log 기록 후 완료 처리가 안 된 건들을 찾아 finalize() 합니다.
     * </p>
     *
     * @param walState WAL 상태 (PENDING)
     * @param pageable 페이징 정보 (조회 제한 포함)
     * @return Outbox Entity 목록
     */
    @Query("SELECT o FROM SellerCrawlScheduleOutboxEntity o "
            + "WHERE o.walState = :walState "
            + "ORDER BY o.createdAt ASC")
    List<SellerCrawlScheduleOutboxEntity> findByWalStatePending(
            @Param("walState") WriteAheadState walState,
            Pageable pageable
    );

    /**
     * IN_PROGRESS 상태이면서 타임아웃된 Outbox 조회 (Reaper용)
     * <p>
     * Orchestrator Reaper가 주기적으로 호출하여
     * IN_PROGRESS 상태로 남아있지만 타임아웃 시간이 지난 건들을 찾아
     * 재시도하거나 FAILED 처리합니다.
     * </p>
     *
     * @param operationState 작업 상태 (IN_PROGRESS)
     * @param cutoffTime     타임아웃 기준 시각 (현재 시각 - timeout)
     * @param pageable       페이징 정보 (조회 제한 포함)
     * @return Outbox Entity 목록
     */
    @Query("SELECT o FROM SellerCrawlScheduleOutboxEntity o "
            + "WHERE o.operationState = :operationState "
            + "AND o.createdAt < :cutoffTime "
            + "ORDER BY o.createdAt ASC")
    List<SellerCrawlScheduleOutboxEntity> findInProgressAndTimeout(
            @Param("operationState") OperationState operationState,
            @Param("cutoffTime") LocalDateTime cutoffTime,
            Pageable pageable
    );

    /**
     * 특정 상태의 Outbox 개수 조회 (모니터링용)
     * <p>
     * PENDING, IN_PROGRESS, FAILED 건수를 모니터링하여
     * 시스템 상태를 파악합니다.
     * </p>
     *
     * @param operationState 작업 상태
     * @return Outbox 개수
     */
    long countByOperationState(OperationState operationState);

    /**
     * 재시도 가능한 FAILED Outbox 조회
     * <p>
     * retryCount < maxRetries 인 FAILED 건들을 찾아서 재시도합니다.
     * </p>
     *
     * @param operationState 작업 상태 (FAILED)
     * @param pageable       페이징 정보 (조회 제한 포함)
     * @return Outbox Entity 목록
     */
    @Query("SELECT o FROM SellerCrawlScheduleOutboxEntity o "
            + "WHERE o.operationState = :operationState "
            + "AND o.retryCount < o.maxRetries "
            + "ORDER BY o.createdAt ASC")
    List<SellerCrawlScheduleOutboxEntity> findRetryableFailed(
            @Param("operationState") OperationState operationState,
            Pageable pageable
    );

    /**
     * 특정 기간 동안 완료된 Outbox 삭제 (정리 작업용)
     * <p>
     * 오래된 COMPLETED 건들을 주기적으로 삭제하여 테이블 크기를 관리합니다.
     * </p>
     *
     * @param operationState 작업 상태 (COMPLETED)
     * @param beforeDate     기준 날짜 (이전 건들 삭제)
     * @return 삭제된 행 수
     */
    @Query("DELETE FROM SellerCrawlScheduleOutboxEntity o "
            + "WHERE o.operationState = :operationState "
            + "AND o.completedAt < :beforeDate")
    int deleteCompletedBefore(
            @Param("operationState") OperationState operationState,
            @Param("beforeDate") LocalDateTime beforeDate
    );
}
