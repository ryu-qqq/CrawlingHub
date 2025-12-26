package com.ryuqq.crawlinghub.application.product.port.out.query;

import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
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
public interface SyncOutboxQueryPort {

    /**
     * ID로 SyncOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return SyncOutbox (Optional)
     */
    Optional<CrawledProductSyncOutbox> findById(Long outboxId);

    /**
     * Idempotency Key로 SyncOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return SyncOutbox (Optional)
     */
    Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey);

    /**
     * CrawledProduct ID로 SyncOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return SyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findByCrawledProductId(CrawledProductId crawledProductId);

    /**
     * 상태로 SyncOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return SyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit);

    /**
     * PENDING 상태의 SyncOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 SyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findPendingOutboxes(int limit);

    /**
     * FAILED 상태이고 재시도 가능한 SyncOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 SyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit);

    /**
     * 조건으로 SyncOutbox 목록 검색 (페이징)
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param status 상태 (nullable)
     * @param offset 오프셋
     * @param size 페이지 크기
     * @return SyncOutbox 목록
     */
    List<CrawledProductSyncOutbox> search(
            Long crawledProductId,
            Long sellerId,
            ProductOutboxStatus status,
            long offset,
            int size);

    /**
     * 조건으로 SyncOutbox 개수 조회
     *
     * @param crawledProductId CrawledProduct ID (nullable)
     * @param sellerId 셀러 ID (nullable)
     * @param status 상태 (nullable)
     * @return 총 개수
     */
    long count(Long crawledProductId, Long sellerId, ProductOutboxStatus status);
}
