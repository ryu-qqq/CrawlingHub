package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.common.component.lock.DistributedLockExecutor;
import com.ryuqq.crawlinghub.application.common.component.lock.LockType;
import com.ryuqq.crawlinghub.application.image.dto.messaging.ProductImagePayload;
import com.ryuqq.crawlinghub.application.image.port.in.command.ProcessImageUploadFromSqsUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ProductImage SQS 리스너
 *
 * <p><strong>용도</strong>: ProductImage SQS 큐에서 메시지를 수신하여 이미지 업로드 처리
 *
 * <p><strong>메시지 페이로드</strong>:
 *
 * <pre>{@code
 * {
 *   "outboxId": 123,
 *   "crawledProductImageId": 456,
 *   "idempotencyKey": "unique-key"
 * }
 * }</pre>
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 큐에서 메시지 수신
 *   <li>분산 락 획득 시도 (outboxId 기준)
 *   <li>이미지 업로드 처리 (Application Layer 호출)
 *   <li>성공 시 메시지 ACK
 *   <li>실패 시 메시지 유지 → 재시도 → DLQ
 * </ol>
 *
 * <p><strong>분산 락</strong>:
 *
 * <ul>
 *   <li>락 획득 성공 → 이미지 업로드 실행 → ACK
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
public class ProductImageSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ProductImageSqsListener.class);

    private final DistributedLockExecutor lockExecutor;
    private final ProcessImageUploadFromSqsUseCase processImageUploadFromSqsUseCase;

    public ProductImageSqsListener(
            DistributedLockExecutor lockExecutor,
            ProcessImageUploadFromSqsUseCase processImageUploadFromSqsUseCase) {
        this.lockExecutor = lockExecutor;
        this.processImageUploadFromSqsUseCase = processImageUploadFromSqsUseCase;
    }

    /**
     * ProductImage 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     *
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload ProductImage 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(value = "${aws.sqs.product-image-queue-url}", acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload ProductImagePayload payload, Acknowledgement acknowledgement) {
        Long outboxId = payload.outboxId();

        log.debug(
                "ProductImage 메시지 수신: outboxId={}, imageId={}, idempotencyKey={}",
                outboxId,
                payload.crawledProductImageId(),
                payload.idempotencyKey());

        // 분산 락 획득 시도 (outboxId 기준, waitTime=0 즉시 반환)
        boolean lockAcquired =
                lockExecutor.tryExecuteWithLock(
                        LockType.PRODUCT_IMAGE_OUTBOX, outboxId, () -> executeImageUpload(payload));

        if (!lockAcquired) {
            // 락 획득 실패 = 다른 워커가 처리 중 = 내가 할 일 없음
            acknowledgement.acknowledge();
            log.info("ProductImage 처리 skip (다른 워커 처리 중): outboxId={}", outboxId);
            return;
        }

        // 락 획득 성공 + 처리 완료
        acknowledgement.acknowledge();
        log.info(
                "ProductImage 처리 완료: outboxId={}, imageId={}",
                outboxId,
                payload.crawledProductImageId());
    }

    /**
     * 이미지 업로드 실행
     *
     * <p>ProcessImageUploadFromSqsUseCase를 호출하여 이미지 업로드 수행
     *
     * @param payload 이미지 업로드 정보
     */
    private void executeImageUpload(ProductImagePayload payload) {
        processImageUploadFromSqsUseCase.execute(payload);
    }
}
