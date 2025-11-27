package com.ryuqq.crawlinghub.application.task.dto.command;

/**
 * CrawlTask 트리거 Command DTO
 *
 * <p>EventBridge에서 호출되어 CrawlTask 생성을 요청
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID (NotNull)
 * @author development-team
 * @since 1.0.0
 */
public record TriggerCrawlTaskCommand(Long crawlSchedulerId) {
    public TriggerCrawlTaskCommand {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
    }
}
