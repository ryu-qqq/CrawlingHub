package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductSyncOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.id.CrawledProductId;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CrawledProductSyncOutbox 조회 관리자
 *
 * <p>CrawledProductSyncOutboxQueryPort를 래핑하여 조회 로직을 제공합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawledProductSyncOutboxReadManager {

    private final CrawledProductSyncOutboxQueryPort syncOutboxQueryPort;

    public CrawledProductSyncOutboxReadManager(
            CrawledProductSyncOutboxQueryPort syncOutboxQueryPort) {
        this.syncOutboxQueryPort = syncOutboxQueryPort;
    }

    /**
     * ID로 CrawledProductSyncOutbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return CrawledProductSyncOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductSyncOutbox> findById(Long outboxId) {
        return syncOutboxQueryPort.findById(outboxId);
    }

    /**
     * Idempotency Key로 CrawledProductSyncOutbox 조회
     *
     * @param idempotencyKey 멱등성 키
     * @return CrawledProductSyncOutbox (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<CrawledProductSyncOutbox> findByIdempotencyKey(String idempotencyKey) {
        return syncOutboxQueryPort.findByIdempotencyKey(idempotencyKey);
    }

    /**
     * CrawledProduct ID로 CrawledProductSyncOutbox 목록 조회
     *
     * @param crawledProductId CrawledProduct ID
     * @return CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findByCrawledProductId(
            CrawledProductId crawledProductId) {
        return syncOutboxQueryPort.findByCrawledProductId(crawledProductId);
    }

    /**
     * 상태로 CrawledProductSyncOutbox 목록 조회
     *
     * @param status 상태
     * @param limit 조회 개수 제한
     * @return CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findByStatus(ProductOutboxStatus status, int limit) {
        return syncOutboxQueryPort.findByStatus(status, limit);
    }

    /**
     * PENDING 상태의 CrawledProductSyncOutbox 조회 (스케줄러용)
     *
     * @param limit 조회 개수 제한
     * @return PENDING 상태의 CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findPendingOutboxes(int limit) {
        return syncOutboxQueryPort.findPendingOutboxes(limit);
    }

    /**
     * 재시도 가능한 CrawledProductSyncOutbox 조회
     *
     * @param maxRetryCount 최대 재시도 횟수
     * @param limit 조회 개수 제한
     * @return 재시도 가능한 CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findRetryableOutboxes(int maxRetryCount, int limit) {
        return syncOutboxQueryPort.findRetryableOutboxes(maxRetryCount, limit);
    }

    /**
     * FAILED 상태에서 일정 시간 경과한 CrawledProductSyncOutbox 조회 (복구용)
     *
     * @param limit 조회 개수 제한
     * @param delaySeconds FAILED 후 경과해야 할 최소 시간 (초)
     * @return 복구 대상 CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findFailedOlderThan(int limit, int delaySeconds) {
        return syncOutboxQueryPort.findFailedOlderThan(limit, delaySeconds);
    }

    /**
     * PROCESSING 상태에서 타임아웃된 CrawledProductSyncOutbox 조회 (좀비 복구용)
     *
     * @param limit 조회 개수 제한
     * @param timeoutSeconds PROCESSING 상태 타임아웃 기준 (초)
     * @return 좀비 CrawledProductSyncOutbox 목록
     */
    @Transactional(readOnly = true)
    public List<CrawledProductSyncOutbox> findStaleProcessing(int limit, long timeoutSeconds) {
        return syncOutboxQueryPort.findStaleProcessing(limit, timeoutSeconds);
    }

    /**
     * 특정 상품의 특정 SyncType에 대해 활성 Outbox 존재 여부 확인
     *
     * @param productId CrawledProduct ID
     * @param syncType 동기화 타입
     * @return 활성 Outbox가 있으면 true
     */
    @Transactional(readOnly = true)
    public boolean existsActiveOutbox(
            CrawledProductId productId, CrawledProductSyncOutbox.SyncType syncType) {
        return syncOutboxQueryPort.existsByProductIdAndSyncTypeAndStatuses(
                productId.value(),
                syncType,
                List.of(
                        ProductOutboxStatus.PENDING,
                        ProductOutboxStatus.SENT,
                        ProductOutboxStatus.PROCESSING));
    }
}
