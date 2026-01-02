package com.ryuqq.crawlinghub.adapter.in.sqs.listener;

import com.ryuqq.crawlinghub.application.image.dto.messaging.ProductImagePayload;
import com.ryuqq.crawlinghub.application.image.manager.command.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ProductImage DLQ 리스너
 *
 * <p><strong>용도</strong>: ProductImage 처리 실패 메시지 수신 및 Outbox FAILED 마킹
 *
 * <p><strong>처리 흐름</strong>:
 *
 * <pre>
 * product-image-queue (재시도 실패)
 *     ↓
 * product-image-dlq (maxReceiveCount 초과)
 *     ↓
 * ProductImageDlqListener (이 클래스)
 *     ↓
 * Outbox FAILED 마킹
 * </pre>
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
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.product-image-dlq-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ProductImageDlqListener {

    private static final Logger log = LoggerFactory.getLogger(ProductImageDlqListener.class);
    private static final String DLQ_ERROR_MESSAGE = "DLQ로 이동됨 (maxReceiveCount 초과)";

    private final ImageOutboxQueryPort outboxQueryPort;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;

    public ProductImageDlqListener(
            ImageOutboxQueryPort outboxQueryPort,
            ProductImageOutboxTransactionManager outboxTransactionManager) {
        this.outboxQueryPort = outboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
    }

    /**
     * DLQ 메시지 수신 및 처리
     *
     * <p>Spring Cloud AWS SQS의 자동 메시지 역직렬화 사용
     *
     * <p>MANUAL acknowledgement 모드로 명시적 ACK 처리
     *
     * @param payload ProductImage 페이로드
     * @param acknowledgement 메시지 ACK 핸들러
     */
    @SqsListener(
            value = "${aws.sqs.listener.product-image-dlq-url}",
            acknowledgementMode = "MANUAL")
    public void handleMessage(
            @Payload ProductImagePayload payload, Acknowledgement acknowledgement) {
        Long outboxId = payload.outboxId();

        log.debug("ProductImage DLQ 메시지 수신: outboxId={}", outboxId);

        try {
            // Outbox 조회 및 FAILED 마킹
            markOutboxAsFailed(outboxId);

            // 성공 시 ACK
            acknowledgement.acknowledge();

            log.info("ProductImage DLQ 처리 완료 (Outbox FAILED 마킹): outboxId={}", outboxId);

        } catch (Exception e) {
            log.error("ProductImage DLQ 처리 실패: outboxId={}, error={}", outboxId, e.getMessage(), e);
            // 처리 실패 시 ACK 하지 않음 → 다음 폴링에서 재시도
            throw e;
        }
    }

    /**
     * Outbox FAILED 마킹
     *
     * @param outboxId Outbox ID
     */
    private void markOutboxAsFailed(Long outboxId) {
        Optional<ProductImageOutbox> outboxOpt = outboxQueryPort.findById(outboxId);

        if (outboxOpt.isPresent()) {
            ProductImageOutbox outbox = outboxOpt.get();
            outboxTransactionManager.markAsFailed(outbox, DLQ_ERROR_MESSAGE);
            log.info("ProductImage Outbox FAILED 마킹 완료: outboxId={}", outboxId);
        } else {
            log.warn("ProductImage Outbox를 찾을 수 없음: outboxId={}", outboxId);
        }
    }
}
