package com.ryuqq.crawlinghub.application.task.dto.command;

import java.util.List;

/**
 * CrawlTask 배치 재실행 Command DTO
 *
 * <p>여러 실패한 CrawlTask를 일괄 재실행하기 위한 명령 객체
 *
 * @param crawlTaskIds 재실행할 CrawlTask ID 목록
 * @author development-team
 * @since 1.0.0
 */
public record BatchRetryCrawlTaskCommand(List<Long> crawlTaskIds) {

    public BatchRetryCrawlTaskCommand {
        if (crawlTaskIds == null || crawlTaskIds.isEmpty()) {
            throw new IllegalArgumentException("CrawlTask ID 목록은 비어있을 수 없습니다.");
        }
        if (crawlTaskIds.size() > 100) {
            throw new IllegalArgumentException("한 번에 최대 100개의 Task만 재처리할 수 있습니다.");
        }
        for (Long id : crawlTaskIds) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("CrawlTask ID는 양수여야 합니다.");
            }
        }
        crawlTaskIds = List.copyOf(crawlTaskIds);
    }

    public static BatchRetryCrawlTaskCommand of(List<Long> crawlTaskIds) {
        return new BatchRetryCrawlTaskCommand(crawlTaskIds);
    }
}
