package com.ryuqq.crawlinghub.application.product.dto.command;

/**
 * 수동 동기화 트리거 커맨드
 *
 * <p>CrawledProduct에 대해 수동으로 동기화를 트리거합니다.
 *
 * @param crawledProductId CrawledProduct ID
 */
public record TriggerManualSyncCommand(Long crawledProductId) {

    public TriggerManualSyncCommand {
        if (crawledProductId == null) {
            throw new IllegalArgumentException("CrawledProduct ID는 필수입니다.");
        }
    }
}
