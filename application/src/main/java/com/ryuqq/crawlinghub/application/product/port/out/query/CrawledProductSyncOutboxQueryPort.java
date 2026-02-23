package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.query.ProductSyncOutboxCriteria;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 외부 동기화 Outbox 조회 Port (Port Out - Query)
 *
 * <p>스케줄러 및 Facade에서 사용됩니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface CrawledProductSyncOutboxQueryPort {

    /**
     * ID로 CrawledProductSyncOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return CrawledProductSyncOutbox (Optional)
     */
    Optional<CrawledProductSyncOutbox> findById(Long outboxId);

    /**
     * Idempotency Key로 CrawledProductSyncOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return CrawledProductSyncOutbox (Optional)
     */
    Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * CrawledProduct ID로 CrawledProductSyncOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findByCrawledProductId(CrawledProductId crawledProductId);

    /**
     * 상태로 CrawledProductSyncOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit);

    /**
     * PENDING 상태의 CrawledProductSyncOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findPendingOutboxes(int limit);

    /**
     * FAILED 상태이고 재시도 가능한 CrawledProductSyncOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit);

    /**
     * Criteria 기반 CrawledProductSyncOutbox 조회 (SQS 스케줄러용)
     *
     * <p>ProductSyncOutboxCriteria VO를 사용하여 유연한 조건 조회를 지원합니다.
     *
     * @param criteria 조회 조건 VO
     * @return CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findByCriteria(ProductSyncOutboxCriteria criteria);

    /**
     * Criteria 기반 CrawledProductSyncOutbox 개수 조회
     *
     * @param criteria 조회 조건 VO
     * @return 총 개수
     */
    long countByCriteria(ProductSyncOutboxCriteria criteria);

    /**
     * 조건으로 CrawledProductSyncOutbox 목록 검색 (페이징)
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> search(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo,
            long offset,
            int size);

    /**
     * 조건으로 CrawledProductSyncOutbox 개수 조회
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param itemNos 외부 상품번호 목록 (IN 조건, nullable)
     * @param statuses 상태 목록 (IN 조건, nullable)
     * @param createdFrom 생성일 시작 범위 (nullable)
     * @param createdTo 생성일 종료 범위 (nullable)
     * @return 총 개수
     */
    long count(
            Long crawledProductId,
            Long sellerId,
            List<Long> itemNos,
            List<ProductOutboxStatus> statuses,
            Instant createdFrom,
            Instant createdTo);

    /**
     * FAILED 상태에서 일정 시간 경과한 CrawledProductSyncOutbox 조회 (복구 스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @param delaySeconds FAILED 후 경과해야 할 최소 시간 (초)
     * @return 복구 대상 CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findFailedOlderThan(int limit, int delaySeconds);

    /**
     * PROCESSING 상태에서 타임아웃된 CrawledProductSyncOutbox 조회 (좀비 복구 스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @param timeoutSeconds PROCESSING 상태 타임아웃 기준 (초)
     * @return 좀비 CrawledProductSyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findStaleProcessing(int limit, long timeoutSeconds);

    /**
     * 특정 상품의 특정 SyncType에 대해 특정 상태의 Outbox 존재 여부 확인
     *
     * @param productId CrawledProduct ID value
     * @param syncType 동기화 타입
     * @param statuses 확인할 상태 목록
     * @return 존재하면 true
     */
    boolean existsByProductIdAndSyncTypeAndStatuses(
            Long productId,
            CrawledProductSyncOutbox.SyncType syncType,
            List<ProductOutboxStatus> statuses);
}
