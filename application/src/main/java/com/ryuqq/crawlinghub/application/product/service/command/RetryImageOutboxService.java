package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.image.manager.ProductImageOutboxTransactionManager;
import com.ryuqq.crawlinghub.application.product.dto.command.RetryImageOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetryImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import org.springframework.stereotype.Service;

/**
 * ImageOutbox 수동 재시도 서비스
 *
 * <p>FAILED 상태의 ImageOutbox를 PENDING 상태로 변경하여 재시도합니다.
 */
@Service
public class RetryImageOutboxService implements RetryImageOutboxUseCase {

    private final ImageOutboxQueryPort imageOutboxQueryPort;
    private final ProductImageOutboxTransactionManager outboxTransactionManager;

    public RetryImageOutboxService(
            ImageOutboxQueryPort imageOutboxQueryPort,
            ProductImageOutboxTransactionManager outboxTransactionManager) {
        this.imageOutboxQueryPort = imageOutboxQueryPort;
        this.outboxTransactionManager = outboxTransactionManager;
    }

    @Override
    public OutboxRetryResponse execute(RetryImageOutboxCommand command) {
        ProductImageOutbox outbox = findOutboxOrThrow(command.outboxId());

        ProductOutboxStatus previousStatus = outbox.getStatus();
        validateCanRetry(outbox);

        outboxTransactionManager.resetToPending(outbox);

        return OutboxRetryResponse.success(
                command.outboxId(), previousStatus.name(), ProductOutboxStatus.PENDING.name());
    }

    private ProductImageOutbox findOutboxOrThrow(Long outboxId) {
        return imageOutboxQueryPort
                .findById(outboxId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "ImageOutbox를 찾을 수 없습니다. ID: " + outboxId));
    }

    private void validateCanRetry(ProductImageOutbox outbox) {
        if (outbox.isPending()) {
            throw new IllegalStateException("이미 PENDING 상태입니다.");
        }
        if (outbox.isCompleted()) {
            throw new IllegalStateException("이미 완료된 Outbox는 재시도할 수 없습니다.");
        }
    }
}
