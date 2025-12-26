package com.ryuqq.crawlinghub.application.task.dto.query;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;

/**
 * CrawlTask 목록 조회 Query DTO
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID (NotNull)
 * @param status 상태 필터 (Optional, null이면 전체)
 * @param taskType 태스크 유형 필터 (Optional, null이면 전체)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record ListCrawlTasksQuery(
        Long crawlSchedulerId, CrawlTaskStatus status, CrawlTaskType taskType, int page, int size) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public ListCrawlTasksQuery {
        if (crawlSchedulerId == null) {
            throw new IllegalArgumentException("crawlSchedulerId는 null일 수 없습니다.");
        }
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size <= 0 || size > MAX_SIZE) {
            size = DEFAULT_SIZE;
        }
    }

    /**
     * offset 계산
     *
     * @return 시작 위치
     */
    public long offset() {
        return (long) page * size;
    }
}
