package com.ryuqq.crawlinghub.application.product.service.command;

import com.ryuqq.crawlinghub.application.product.dto.command.RetrySyncOutboxCommand;
import com.ryuqq.crawlinghub.application.product.dto.response.OutboxRetryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.command.RetrySyncOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.SyncOutboxQueryPort;
import com.ryuqq.crawlinghub.application.sync.manager.command.SyncOutboxTransactionManager;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProductSyncOutbox;
import com.ryuqq.crawlinghub.domain.product.exception.SyncOutboxNotFoundException;
import com.ryuqq.crawlinghub.domain.product.vo.ProductOutboxStatus;
import org.springframework.stereotype.Service;

/**
 * SyncOutbox 수동 재시도 서비스
 *
 * <p>FAILED 상태의 SyncOutbox를 PENDING 상태로 변경하여 재시도합니다.
 */
@Service
public class RetrySyncOutboxService implements RetrySyncOutboxUseCase {

    private final SyncOutboxQueryPort syncOutboxQueryPort;
    private final SyncOutboxTransactionManager syncOutboxTransactionManager;

    public RetrySyncOutboxService(
            SyncOutboxQueryPort syncOutboxQueryPort,
            SyncOutboxTransactionManager syncOutboxTransactionManager) {
        this.syncOutboxQueryPort = syncOutboxQueryPort;
        this.syncOutboxTransactionManager = syncOutboxTransactionManager;
    }

    @Override
    public OutboxRetryResponse execute(RetrySyncOutboxCommand command) {
        CrawledProductSyncOutbox outbox = findOutboxOrThrow(command.outboxId());

        ProductOutboxStatus previousStatus = outbox.getStatus();
        validateCanRetry(outbox);

        syncOutboxTransactionManager.resetToPending(outbox);

        return OutboxRetryResponse.success(
                command.outboxId(), previousStatus.name(), ProductOutboxStatus.PENDING.name());
    }

    private CrawledProductSyncOutbox findOutboxOrThrow(Long outboxId) {
        return syncOutboxQueryPort
                .findById(outboxId)
                .orElseThrow(() -> new SyncOutboxNotFoundException(outboxId));
    }

    private void validateCanRetry(CrawledProductSyncOutbox outbox) {
        if (outbox.isPending()) {
            throw new IllegalStateException("이미 PENDING 상태입니다.");
        }
        if (outbox.isCompleted()) {
            throw new IllegalStateException("이미 완료된 Outbox는 재시도할 수 없습니다.");
        }
    }
}
