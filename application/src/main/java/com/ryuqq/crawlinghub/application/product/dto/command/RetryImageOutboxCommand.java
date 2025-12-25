package com.ryuqq.crawlinghub.application.product.dto.command;

/**
 * ImageOutbox 수동 재시도 커맨드
 *
 * <p>FAILED 상태의 ImageOutbox를 수동으로 재시도합니다.
 *
 * @param outboxId ImageOutbox ID
 */
public record RetryImageOutboxCommand(Long outboxId) {

    public RetryImageOutboxCommand {
        if (outboxId == null) {
            throw new IllegalArgumentException("Outbox ID는 필수입니다.");
        }
    }
}
