package com.ryuqq.crawlinghub.application.execution.dto.query;

import com.ryuqq.crawlinghub.domain.execution.vo.CrawlExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CrawlExecution 목록 조회 Query DTO
 *
 * @param crawlTaskId 태스크 ID 필터 (optional)
 * @param crawlSchedulerId 스케줄러 ID 필터 (optional)
 * @param sellerId 셀러 ID 필터 (optional)
 * @param statuses 상태 필터 목록 (optional, 다중 선택 가능)
 * @param from 조회 시작 시간 (optional)
 * @param to 조회 종료 시간 (optional)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record ListCrawlExecutionsQuery(
        Long crawlTaskId,
        Long crawlSchedulerId,
        Long sellerId,
        List<CrawlExecutionStatus> statuses,
        LocalDateTime from,
        LocalDateTime to,
        int page,
        int size) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public ListCrawlExecutionsQuery {
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

    /**
     * 상태 필터가 있는지 확인
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public CrawlExecutionStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }
}
