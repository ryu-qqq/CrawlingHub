package com.ryuqq.crawlinghub.adapter.in.sqs.product;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.messaging.ProductSyncPayload;
import com.ryuqq.crawlinghub.application.product.port.in.command.ProcessProductSyncFromSqsUseCase;
import io.awspring.cloud.sqs.annotation.SqsListener;
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
 * <p><strong>처리 흐름</strong>:
 *
 * <ol>
 *   <li>SQS 큐에서 메시지 수신
 *   <li>Payload → Command 변환 (ListenerMapper)
 *   <li>외부 서버 동기화 처리 (Application Layer 호출)
 * </ol>
 *
 * <p><strong>에러 처리</strong>:
 *
 * <ul>
 *   <li>Service가 내부적으로 모든 비즈니스 실패를 처리함 (Outbox FAILED 마킹 등)
 *   <li>여기까지 전파된 예외 = 인프라 오류 → throw (SQS 재시도)
 * </ul>
 *
 * <p><strong>멱등성</strong>: Application Layer에서 Outbox 상태 체크로 보장
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
@ConditionalOnProperty(
        name = "aws.sqs.listener.product-sync-listener-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ProductSyncSqsListener {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncSqsListener.class);

    private final ProductSyncListenerMapper mapper;
    private final ProcessProductSyncFromSqsUseCase processProductSyncFromSqsUseCase;

    public ProductSyncSqsListener(
            ProductSyncListenerMapper mapper,
            ProcessProductSyncFromSqsUseCase processProductSyncFromSqsUseCase) {
        this.mapper = mapper;
        this.processProductSyncFromSqsUseCase = processProductSyncFromSqsUseCase;
    }

    /**
     * ProductSync 메시지 수신 및 처리
     *
     * <p>AUTO ACK 모드: 정상 반환 시 자동 ACK, 예외 발생 시 SQS 재시도
     *
     * @param payload ProductSync 페이로드
     */
    @SqsListener("${aws.sqs.listener.product-sync-queue-url}")
    public void handleMessage(@Payload ProductSyncPayload payload) {
        Long outboxId = payload.outboxId();

        log.debug(
                "ProductSync 메시지 수신: outboxId={}, productId={}, sellerId={}, syncType={}",
                outboxId,
                payload.crawledProductId(),
                payload.sellerId(),
                payload.syncType());

        try {
            ProcessProductSyncCommand command = mapper.toCommand(payload);
            processProductSyncFromSqsUseCase.execute(command);
            log.info(
                    "ProductSync 처리 완료: outboxId={}, productId={}, syncType={}",
                    outboxId,
                    payload.crawledProductId(),
                    payload.syncType());
        } catch (Exception e) {
            // Service가 내부적으로 모든 비즈니스 실패를 처리함
            // 여기까지 전파된 예외 = 인프라 오류 → SQS 재시도
            log.warn("ProductSync 일시적 오류, SQS 재시도 위임: outboxId={}", outboxId);
            throw e;
        }
    }
}
