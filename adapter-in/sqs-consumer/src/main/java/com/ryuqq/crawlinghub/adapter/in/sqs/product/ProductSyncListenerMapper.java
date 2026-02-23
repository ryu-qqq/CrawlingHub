package com.ryuqq.crawlinghub.adapter.in.sqs.product;

import com.ryuqq.crawlinghub.application.product.dto.command.ProcessProductSyncCommand;
import com.ryuqq.crawlinghub.application.product.dto.messaging.ProductSyncPayload;
import org.springframework.stereotype.Component;

/**
 * ProductSync 리스너 매퍼
 *
 * <p><strong>용도</strong>: ProductSyncPayload → ProcessProductSyncCommand 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class ProductSyncListenerMapper {

    public ProcessProductSyncCommand toCommand(ProductSyncPayload payload) {
        return new ProcessProductSyncCommand(
                payload.outboxId(),
                payload.crawledProductId(),
                payload.sellerId(),
                payload.itemNo(),
                payload.syncType(),
                payload.externalProductId(),
                payload.idempotencyKey());
    }
}
