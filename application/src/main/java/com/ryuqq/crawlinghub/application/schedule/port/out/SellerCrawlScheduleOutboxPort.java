package com.ryuqq.crawlinghub.application.crawl.schedule.port.out;

import com.ryuqq.crawlinghub.domain.schedule.outbox.SellerCrawlScheduleOutbox;

import java.util.List;
import java.util.Optional;

/**
 * Seller Crawl Schedule Outbox 저장소 Port
 *
 * <p>Outbox Pattern을 위한 Write-Ahead Log (WAL) 저장소 인터페이스입니다.
 *
 * @author 개발자
 * @since 2024-01-01
 */
public interface SellerCrawlScheduleOutboxPort {

    /**
     * Outbox 저장
     *
     * @param outbox 저장할 Outbox
     * @return 저장된 Outbox
     */
    SellerCrawlScheduleOutbox save(SellerCrawlScheduleOutbox outbox);

    /**
     * Idempotency Key로 Outbox 조회
     *
     * @param idemKey Idempotency Key
     * @return Outbox (Optional)
     */
    Optional<SellerCrawlScheduleOutbox> findByIdemKey(String idemKey);

    /**
     * Idempotency Key 존재 여부 확인
     *
     * @param idemKey Idempotency Key
     * @return 존재 여부
     */
    boolean existsByIdemKey(String idemKey);

    /**
     * WAL State가 PENDING인 Outbox 목록 조회 (생성 시간 오름차순)
     *
     * <p>S2 Phase (Execute)에서 처리할 대상을 조회합니다.
     *
     * @return PENDING 상태의 Outbox 목록
     */
    List<SellerCrawlScheduleOutbox> findByWalStatePending();

    /**
     * Operation State가 FAILED인 Outbox 목록 조회
     *
     * <p>S3 Phase (Finalize)에서 재시도 대상을 조회합니다.
     *
     * @return FAILED 상태의 Outbox 목록
     */
    List<SellerCrawlScheduleOutbox> findByOperationStateFailed();

    /**
     * WAL State가 COMPLETED인 Outbox 목록 조회
     *
     * <p>S3 Phase (Finalize)에서 정리 대상을 조회합니다.
     *
     * @return COMPLETED 상태의 Outbox 목록
     */
    List<SellerCrawlScheduleOutbox> findByWalStateCompleted();

    /**
     * Outbox 삭제
     *
     * <p>S3 Phase에서 완료된 Outbox를 정리할 때 사용합니다.
     *
     * @param outbox 삭제할 Outbox
     */
    void delete(SellerCrawlScheduleOutbox outbox);
}
