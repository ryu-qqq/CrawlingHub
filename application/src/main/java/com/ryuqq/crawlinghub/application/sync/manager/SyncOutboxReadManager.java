package com.ryuqq.crawlinghub.application.sync.manager;

import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.identifier.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SyncOutbox 조회 관리자
 *
 * <p>SyncOutboxQueryPort를 래핑하여 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class SyncOutboxReadManager {

    private final SyncOutboxQueryPort syncOutboxQueryPort;

    public SyncOutboxReadManager(SyncOutboxQueryPort syncOutboxQueryPort) {
        this.syncOutboxQueryPort = syncOutboxQueryPort;
    }

    /**
     * ID로 SyncOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return SyncOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductSyncOutbox> findById(Long outboxId) {
        return syncOutboxQueryPort.findById(outboxId);
    }

    /**
     * Idempotency Key로 SyncOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return SyncOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey) {
        return syncOutboxQueryPort.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * CrawledProduct ID로 SyncOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return SyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        return syncOutboxQueryPort.findByCrawledProductId(crawledProductId);
    }

    /**
     * 상태로 SyncOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return SyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        return syncOutboxQueryPort.findByStatus(status, limit);
    }

    /**
     * PENDING 상태의 SyncOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 SyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findPendingOutboxes(int limit) {
        return syncOutboxQueryPort.findPendingOutboxes(limit);
    }

    /**
     * 재시도 가능한 SyncOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 SyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        return syncOutboxQueryPort.findRetryableOutboxes(maxRetryCount, limit);
    }
}
