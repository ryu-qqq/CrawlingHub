package com.ryuqq.crawlinghub.application.product.listener;

import com.ryuqq.crawlinghub.application.product.component.ProductSyncOutboxStateManager;
import com.ryuqq.crawlinghub.application.product.port.out.ExternalProductApiPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.event.ProductSyncOutboxCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

/**
 * 외부 동기화 이벤트 리스너 (Fast Path)
 *
 * <p>역할: ProductSyncOutboxCreatedEvent를 구독하여 즉시 외부 동기화 시도
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>ProductSyncOutboxCreatedEvent 수신 (Outbox는 이미 PENDING 상태로 저장됨)</li>
 *   <li>Outbox 단건 조회 (findById)</li>
 *   <li>외부 API 호출 (트랜잭션 밖)</li>
 *   <li>성공 시: StateManager.markAsCompleted() (트랜잭션 내)</li>
 *   <li>실패 시: StateManager.incrementRetryAndRecordError() (트랜잭션 내)</li>
 *   <li>실패 시 Outbox는 PENDING 유지 (Scheduler가 재처리)</li>
 * </ol>
 *
 * <p>⚠️ Transaction 경계:
 * <ul>
 *   <li>이 리스너는 @Async로 별도 스레드에서 실행</li>
 *   <li>외부 API 호출은 트랜잭션 밖에서 실행</li>
 *   <li>ProductSyncOutboxStateManager의 메서드들은 각각 별도 트랜잭션</li>
 *   <li>실패 시 Outbox는 PENDING 유지 (Scheduler가 재처리)</li>
 * </ul>
 *
 * <p><strong>Zero-Tolerance 준수:</strong></p>
 * <ul>
 *   <li>✅ 외부 API 호출은 트랜잭션 밖에서 실행</li>
 *   <li>✅ 상태 변경은 별도 트랜잭션으로 관리</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 2025-11-07
 */
@Component
public class ExternalSyncEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalSyncEventListener.class);

    private final ProductSyncOutboxStateManager outboxStateManager;
    private final ExternalProductApiPort externalApiPort;

    public ExternalSyncEventListener(
        ProductSyncOutboxStateManager outboxStateManager,
        ExternalProductApiPort externalApiPort
    ) {
        this.outboxStateManager = Objects.requireNonNull(outboxStateManager, "outboxStateManager must not be null");
        this.externalApiPort = Objects.requireNonNull(externalApiPort, "externalApiPort must not be null");
    }

    /**
     * ProductSyncOutboxCreatedEvent 구독 → 즉시 외부 동기화 시도 (Fast Path)
     *
     * <p>⚠️ @Async 사용으로 별도 스레드에서 실행됨
     *
     * @param event ProductSyncOutbox 생성 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onProductSyncOutboxCreated(ProductSyncOutboxCreatedEvent event) {
        log.info("ProductSyncOutboxCreatedEvent 수신 (Fast Path). outboxId={}, productId={}",
            event.getOutboxId(), event.getProductId());

        try {
            // 1. Outbox 단건 조회 (findById 사용)
            ProductSyncOutbox outbox = outboxStateManager.findById(event.getOutboxId());

            // 2. 최대 재시도 횟수 체크
            if (outbox.isMaxRetriesExceeded()) {
                outboxStateManager.markAsFailed(outbox);
                log.warn("최대 재시도 횟수 초과 (Fast Path). outboxId={}, productId={}, retryCount={}",
                    event.getOutboxId(), event.getProductId(), outbox.getRetryCount());
                return;
            }

            // 3. 상태 전이: PENDING → PROCESSING (트랜잭션 내)
            outboxStateManager.markAsProcessing(outbox);

            // 4. 외부 Product API 호출 (트랜잭션 밖!)
            externalApiPort.updateProduct(outbox.getProductJson());

            // 5. 성공 시: COMPLETED (트랜잭션 내)
            outboxStateManager.markAsCompleted(outbox);

            log.info("외부 동기화 성공 (Fast Path). outboxId={}, productId={}",
                event.getOutboxId(), event.getProductId());

        } catch (Exception e) {
            // 6. 실패 시: 재시도 카운트 증가 (트랜잭션 내)
            try {
                ProductSyncOutbox outbox = outboxStateManager.findById(event.getOutboxId());
                outboxStateManager.incrementRetryAndRecordError(outbox, e.getMessage());
            } catch (Exception stateUpdateError) {
                log.error("상태 업데이트 실패 (Fast Path). outboxId={}, error={}",
                    event.getOutboxId(), stateUpdateError.getMessage());
            }

            log.warn("외부 동기화 실패 (Fast Path). Scheduler가 재처리 예정. outboxId={}, productId={}, error={}",
                event.getOutboxId(), event.getProductId(), e.getMessage());

            // 실패 시: Outbox는 PENDING 상태 유지 (Scheduler가 재처리)
        }
    }
}
