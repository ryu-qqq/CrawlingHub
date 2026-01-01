package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.common.component.lock.DistributedLockExecutor;
import com.ryuqq.crawlinghub.application.common.component.lock.LockType;
import com.ryuqq.crawlinghub.application.sync.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.sync.port.in.command.ProcessProductSyncFromSqsUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ProductSync SQS 리스너
 *
 * <p><strong>용도</strong>: ProductSync SQS 큐에서 메시지를 수신하여 외부 서버 동기화 처리
 *
 * <p><strong>메시지 페이로드</strong>:
 *
 * <pre>{@code
 * {
 *   "outboxId": 123,
 *   "crawledProductId": 456,
 *   "sellerId": 789,
 *   "itemNo": 1001,
 *   "syncType": "CREATE",
 *   "externalProductId": null,
 *   "idempotencyKey": "unique-key"
 * }
 * }</pre>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 큐에서 메시지 수신
 *   <li>분산 락 획득 시도 (outboxId 기준)
 *   <li>외부 서버 동기화 처리 (Application Layer 호출)
 *   <li>성공 시 메시지 ACK
 *   <li>실패 시 메시지 유지 → 재시도 → DLQ
 * </ol>
 *
 * <p><strong>분산 락</strong>:
 *
 * <ul>
 *   <li>락 획득 성공 → 동기화 실행 → ACK
 *   <li>락 획득 실패 → ACK (다른 워커가 처리 중이므로 skip)
 * </ul>
 *
 * <p><strong>활성화 조건</strong>: app.messaging.sqs.enabled=true
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "app.messaging.sqs.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class ProductSyncSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final ProcessProductSyncFromSqsUseCase processProductSyncFromSqsUseCase;

    public ProductSyncSqsListener(
            DistributedLockExecutor lockExecutor,
            ProcessProductSyncFromSqsUseCase processProductSyncFromSqsUseCase) {
        this.lockExecutor = lockExecutor;
        this.processProductSyncFromSqsUseCase = processProductSyncFromSqsUseCase;
    }

    /**
     * ProductSync 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     *
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload ProductSync 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(value = "${aws.sqs.product-sync-queue-url}", acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload ProductSyncPayload payload, Acknowledgement acknowledgement) {
        Long outboxId = payload.outboxId();

        log.debug(
                "ProductSync 메시지 수신: outboxId={}, productId={}, sellerId={}, syncType={}",
                outboxId,
                payload.crawledProductId(),
                payload.sellerId(),
                payload.syncType());

        // 분산 락 획득 시도 (outboxId 기준, waitTime=0 즉시 반환)
        boolean lockAcquired =
                lockExecutor.tryExecuteWithLock(
                        LockType.PRODUCT_SYNC_OUTBOX, outboxId, () -> executeProductSync(payload));

        if (!lockAcquired) {
            // 락 획득 실패 = 다른 워커가 처리 중 = 내가 할 일 없음
            acknowledgement.acknowledge();
            log.info("ProductSync 처리 skip (다른 워커 처리 중): outboxId={}", outboxId);
            return;
        }

        // 락 획득 성공 + 처리 완료
        acknowledgement.acknowledge();
        log.info(
                "ProductSync 처리 완료: outboxId={}, productId={}, syncType={}",
                outboxId,
                payload.crawledProductId(),
                payload.syncType());
    }

    /**
     * 외부 서버 동기화 실행
     *
     * <p>ProcessProductSyncFromSqsUseCase를 호출하여 동기화 수행
     *
     * @param payload 동기화 정보
     */
    private void executeProductSync(ProductSyncPayload payload) {
        processProductSyncFromSqsUseCase.execute(payload);
    }
}
