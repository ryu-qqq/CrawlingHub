package com.ryuqq.crawlinghub.application.task.dto.command;

/**
 * CrawlTask 재실행 Command DTO
 *
 * <p>실패한 CrawlTask를 재실행하기 위한 명령 객체
 *
 * @param crawlTaskId 재실행할 CrawlTask ID
 * @author development-team
 * @since 1.0.0
 */
public record RetryCrawlTaskCommand(Long crawlTaskId) {

    public RetryCrawlTaskCommand {
        if (crawlTaskId == null || crawlTaskId <= 0) {
            throw new IllegalArgumentException("CrawlTask ID는 양수여야 합니다.");
        }
    }
}
