package com.ryuqq.crawlinghub.application.task.dto.query;

import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskStatus;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskType;
import java.time.Instant;
import java.util.List;

/**
 * CrawlTask 목록 조회 Query DTO
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID (선택, null이면 전체)
 *   <li>sellerId: 셀러 ID (선택, null이면 전체)
 *   <li>statuses: 상태 필터 목록 (선택, null이면 전체, 다중 선택 가능)
 *   <li>taskTypes: 태스크 유형 필터 목록 (선택, null이면 전체, 다중 선택 가능)
 *   <li>createdFrom: 생성일시 시작 (선택)
 *   <li>createdTo: 생성일시 종료 (선택)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID (선택)
 * @param sellerId 셀러 ID (선택)
 * @param statuses 상태 필터 목록 (선택, 다중 선택 가능)
 * @param taskTypes 태스크 유형 필터 목록 (선택, 다중 선택 가능)
 * @param createdFrom 생성일시 시작 (선택)
 * @param createdTo 생성일시 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record ListCrawlTasksQuery(
        Long crawlSchedulerId,
        Long sellerId,
        List<CrawlTaskStatus> statuses,
        List<CrawlTaskType> taskTypes,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public ListCrawlTasksQuery {
        statuses = statuses != null ? List.copyOf(statuses) : null;
        taskTypes = taskTypes != null ? List.copyOf(taskTypes) : null;
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
     * 상태 필터 여부 (다중 상태)
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
    public CrawlTaskStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }

    /**
     * 태스크 유형 필터 여부 (다중 유형)
     *
     * @return 태스크 유형 필터가 있으면 true
     */
    public boolean hasTaskTypeFilter() {
        return taskTypes != null && !taskTypes.isEmpty();
    }

    /**
     * 단일 태스크 유형 반환 (하위 호환성)
     *
     * @return 첫 번째 태스크 유형 또는 null
     */
    public CrawlTaskType taskType() {
        return hasTaskTypeFilter() ? taskTypes.get(0) : null;
    }
}
