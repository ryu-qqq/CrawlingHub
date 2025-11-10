package com.ryuqq.crawlinghub.application.product.manager;

import com.ryuqq.crawlinghub.application.product.port.out.LoadProductSyncOutboxPort;
import com.ryuqq.crawlinghub.application.product.port.out.SaveProductSyncOutboxPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.exception.ProductSyncOutboxNotFoundException;
import com.ryuqq.crawlinghub.domain.product.SyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Product Sync Outbox State Manager
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>✅ Outbox 상태 변경 (PENDING, PROCESSING, COMPLETED, FAILED)</li>
 *   <li>✅ Outbox CRUD (Port 호출)</li>
 *   <li>❌ 외부 Product API 호출 - Scheduler/Listener가 담당</li>
 *   <li>✅ 트랜잭션 경계 관리 (각 상태 변경은 별도 트랜잭션)</li>
 * </ul>
 *
 * <p><strong>AOP 이슈 해결:</strong></p>
 * <ul>
 *   <li>✅ Scheduler/EventListener에서 분리 → @Transactional 정상 작동</li>
 *   <li>✅ Scheduler/EventListener는 이 Manager를 호출 → Spring Proxy 통과</li>
 *   <li>✅ 각 상태 변경은 독립적인 트랜잭션 (실패 격리)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 준수:</strong></p>
 * <ul>
 *   <li>✅ @Transactional 내부에 외부 API 호출 없음</li>
 *   <li>✅ 순수하게 상태 관리만 담당 (Single Responsibility)</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ProductSyncOutboxStateManager {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncOutboxStateManager.class);

    private final LoadProductSyncOutboxPort loadOutboxPort;
    private final SaveProductSyncOutboxPort saveOutboxPort;

    /**
     * 생성자
     *
     * @param loadOutboxPort Outbox Query Port (읽기 작업)
     * @param saveOutboxPort Outbox Command Port (쓰기 작업)
     */
    public ProductSyncOutboxStateManager(
        LoadProductSyncOutboxPort loadOutboxPort,
        SaveProductSyncOutboxPort saveOutboxPort
    ) {
        this.loadOutboxPort = Objects.requireNonNull(loadOutboxPort, "loadOutboxPort must not be null");
        this.saveOutboxPort = Objects.requireNonNull(saveOutboxPort, "saveOutboxPort must not be null");
    }

    /**
     * Outbox 단건 조회
     *
     * @param outboxId Outbox ID
     * @return ProductSyncOutbox
     * @throws ProductSyncOutboxNotFoundException Outbox를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public ProductSyncOutbox findById(Long outboxId) {
        return loadOutboxPort.findById(outboxId)
            .orElseThrow(() -> new ProductSyncOutboxNotFoundException(outboxId));
    }

    /**
     * PENDING 상태 Outbox 조회
     *
     * @return PENDING 상태 Outbox 목록 (최대 10개)
     */
    @Transactional(readOnly = true)
    public List<ProductSyncOutbox> findPendingOutboxes() {
        return loadOutboxPort.findByStatus(SyncStatus.PENDING, 10);
    }

    /**
     * Outbox 상태를 PROCESSING으로 변경 (트랜잭션 경계)
     *
     * @param outbox Outbox
     */
    @Transactional
    public void markAsProcessing(ProductSyncOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        outbox.markAsProcessing();
        saveOutboxPort.save(outbox);
        log.info("Outbox PROCESSING 상태로 변경. outboxId={}, productId={}",
            outbox.getIdValue(), outbox.getProductId());
    }

    /**
     * Outbox 상태를 COMPLETED로 변경 (트랜잭션 경계)
     *
     * @param outbox Outbox
     */
    @Transactional
    public void markAsCompleted(ProductSyncOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        outbox.markAsCompleted();
        saveOutboxPort.save(outbox);
        log.info("Outbox COMPLETED 상태로 변경. outboxId={}, productId={}",
            outbox.getIdValue(), outbox.getProductId());
    }

    /**
     * Outbox 상태를 FAILED로 변경 (트랜잭션 경계)
     *
     * @param outbox Outbox
     */
    @Transactional
    public void markAsFailed(ProductSyncOutbox outbox) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        outbox.markAsFailed();
        saveOutboxPort.save(outbox);
        log.warn("Outbox FAILED 상태로 변경. outboxId={}, productId={}",
            outbox.getIdValue(), outbox.getProductId());
    }

    /**
     * Outbox 재시도 카운트 증가 및 에러 기록 (트랜잭션 경계)
     *
     * @param outbox       Outbox
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void incrementRetryAndRecordError(ProductSyncOutbox outbox, String errorMessage) {
        Objects.requireNonNull(outbox, "outbox must not be null");
        outbox.incrementRetryCount();
        outbox.recordError(errorMessage);
        saveOutboxPort.save(outbox);
        log.warn("Outbox 재시도 카운트 증가. outboxId={}, productId={}, retryCount={}, error={}",
            outbox.getIdValue(), outbox.getProductId(), outbox.getRetryCount(), errorMessage);
    }
}
