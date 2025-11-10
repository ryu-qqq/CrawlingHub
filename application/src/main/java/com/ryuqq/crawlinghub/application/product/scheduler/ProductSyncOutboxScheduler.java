package com.ryuqq.crawlinghub.application.product.scheduler;

import com.ryuqq.crawlinghub.application.product.manager.ProductSyncOutboxStateManager;
import com.ryuqq.crawlinghub.application.product.port.out.ExternalProductApiPort;
import com.ryuqq.crawlinghub.domain.product.ProductSyncOutbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 상품 동기화 Outbox 처리기 (Fallback Path)
 *
 * <p>역할: EventListener가 실패한 Outbox를 재처리하는 Fallback 메커니즘
 *
 * <p>처리 흐름:
 * <ol>
 *   <li>5초마다 PENDING 상태 Outbox 조회 (최대 10개)</li>
 *   <li>각 Outbox별로 외부 API 호출 (트랜잭션 밖)</li>
 *   <li>성공 시: StateManager.markAsCompleted() 호출 (트랜잭션 내)</li>
 *   <li>실패 시: StateManager.incrementRetryAndRecordError() 호출 (트랜잭션 내)</li>
 *   <li>최대 재시도 초과 시: StateManager.markAsFailed() 호출 (트랜잭션 내)</li>
 * </ol>
 *
 * <p>핵심 패턴:
 * <ul>
 *   <li>Polling Worker: @Scheduled로 주기적 실행</li>
 *   <li>Fallback Mechanism: EventListener 실패 시 재처리</li>
 *   <li>Transaction 분리: 외부 API 호출은 트랜잭션 밖, 상태 변경은 트랜잭션 내</li>
 *   <li>Spring Proxy: StateManager 메서드는 별도 트랜잭션으로 실행됨</li>
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
public class ProductSyncOutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncOutboxScheduler.class);

    private final ProductSyncOutboxStateManager outboxStateManager;
    private final ExternalProductApiPort externalApiPort;

    public ProductSyncOutboxScheduler(
        ProductSyncOutboxStateManager outboxStateManager,
        ExternalProductApiPort externalApiPort
    ) {
        this.outboxStateManager = Objects.requireNonNull(outboxStateManager, "outboxStateManager must not be null");
        this.externalApiPort = Objects.requireNonNull(externalApiPort, "externalApiPort must not be null");
    }

    /**
     * 5초마다 PENDING 상태 Outbox 처리 (Fallback Path)
     *
     * <p>⚠️ Transaction 경계:
     * <ul>
     *   <li>이 메서드 자체는 @Transactional 없음</li>
     *   <li>외부 API 호출은 트랜잭션 밖에서 실행</li>
     *   <li>ProductSyncOutboxStateManager의 메서드들은 각각 별도 트랜잭션 생성</li>
     * </ul>
     *
     * <p>⚠️ Spring AOP:
     * <ul>
     *   <li>ProductSyncOutboxStateManager는 Spring Bean이므로 Proxy를 통해 호출됨</li>
     *   <li>각 상태 변경 메서드의 @Transactional이 정상 작동함</li>
     * </ul>
     */
    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        List<ProductSyncOutbox> pendingOutboxes = outboxStateManager.findPendingOutboxes();

        if (pendingOutboxes.isEmpty()) {
            return;
        }

        log.info("PENDING Outbox 처리 시작 (Fallback). count={}", pendingOutboxes.size());

        int successCount = 0;
        int failCount = 0;

        for (ProductSyncOutbox outbox : pendingOutboxes) {
            try {
                // 1. 최대 재시도 횟수 체크
                if (outbox.isMaxRetriesExceeded()) {
                    outboxStateManager.markAsFailed(outbox);
                    log.warn("최대 재시도 횟수 초과 (Fallback). outboxId={}, productId={}, retryCount={}",
                        outbox.getIdValue(), outbox.getProductId(), outbox.getRetryCount());
                    failCount++;
                    continue;
                }

                // 2. 상태 전이: PENDING → PROCESSING (트랜잭션 내)
                outboxStateManager.markAsProcessing(outbox);

                // 3. 외부 Product API 호출 (트랜잭션 밖!)
                externalApiPort.updateProduct(outbox.getProductJson());

                // 4. 성공 시: COMPLETED (트랜잭션 내)
                outboxStateManager.markAsCompleted(outbox);
                successCount++;

                log.info("외부 동기화 성공 (Fallback). outboxId={}, productId={}",
                    outbox.getIdValue(), outbox.getProductId());

            } catch (Exception e) {
                failCount++;

                // 5. 실패 시: 재시도 카운트 증가 (트랜잭션 내)
                outboxStateManager.incrementRetryAndRecordError(outbox, e.getMessage());

                log.error("외부 동기화 실패 (Fallback). outboxId={}, productId={}, retryCount={}, error={}",
                    outbox.getIdValue(), outbox.getProductId(), outbox.getRetryCount(), e.getMessage());
            }
        }

        log.info("PENDING Outbox 처리 완료 (Fallback). total={}, success={}, fail={}",
            pendingOutboxes.size(), successCount, failCount);
    }
}
