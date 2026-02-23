package com.ryuqq.crawlinghub.application.task.dto.query;

import java.time.Instant;
import java.util.List;

/**
 * CrawlTask 검색 파라미터
 *
 * <p>CrawlTask 오프셋 기반 다건 조회 파라미터
 *
 * @param crawlSchedulerIds 크롤 스케줄러 ID 목록 (선택, null이면 전체)
 * @param sellerIds 셀러 ID 목록 (선택, null이면 전체)
 * @param statuses 상태 필터 문자열 목록 (선택, null이면 전체)
 * @param taskTypes 태스크 유형 필터 문자열 목록 (선택, null이면 전체)
 * @param createdFrom 생성일시 시작 (선택)
 * @param createdTo 생성일시 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskSearchParams(
        List<Long> crawlSchedulerIds,
        List<Long> sellerIds,
        List<String> statuses,
        List<String> taskTypes,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public CrawlTaskSearchParams {
        crawlSchedulerIds = crawlSchedulerIds != null ? List.copyOf(crawlSchedulerIds) : null;
        sellerIds = sellerIds != null ? List.copyOf(sellerIds) : null;
        statuses = statuses != null ? List.copyOf(statuses) : null;
        taskTypes = taskTypes != null ? List.copyOf(taskTypes) : null;
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size <= 0 || size > MAX_SIZE) {
            size = DEFAULT_SIZE;
        }
    }
}
