package com.ryuqq.crawlinghub.application.product.dto.command;

/**
 * SyncOutbox 수동 재시도 커맨드
 *
 * <p>FAILED 상태의 SyncOutbox를 수동으로 재시도합니다.
 *
 * @param outboxId SyncOutbox ID
 */
public record RetrySyncOutboxCommand(Long outboxId) {

    public RetrySyncOutboxCommand {
        if (outboxId == null) {
            throw new IllegalArgumentException("Outbox ID는 필수입니다.");
        }
    }
}
