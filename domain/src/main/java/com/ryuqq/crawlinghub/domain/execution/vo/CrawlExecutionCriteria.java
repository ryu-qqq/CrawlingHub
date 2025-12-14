package com.ryuqq.crawlinghub.domain.execution.vo;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.task.identifier.CrawlTaskId;
import java.time.Instant;

/**
 * CrawlExecution 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>crawlTaskId: 태스크 ID 필터 (optional)
 *   <li>crawlSchedulerId: 스케줄러 ID 필터 (optional)
 *   <li>status: 상태 필터 (optional)
 *   <li>from: 조회 시작 시간 (optional)
 *   <li>to: 조회 종료 시간 (optional)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * <p><strong>사용 예시</strong>:
 *
 * <pre>
 * // Task ID로 조회
 * CrawlExecutionCriteria.byTaskId(taskId, 0, 20);
 *
 * // Scheduler ID + 기간으로 조회
 * CrawlExecutionCriteria.bySchedulerIdAndPeriod(schedulerId, from, to, 0, 20);
 *
 * // Scheduler ID + 상태 + 기간으로 조회
 * CrawlExecutionCriteria.bySchedulerIdAndStatusAndPeriod(schedulerId, status, from, to, 0, 20);
 * </pre>
 *
 * @param crawlTaskId 태스크 ID (optional)
 * @param crawlSchedulerId 스케줄러 ID (optional)
 * @param status 상태 필터 (optional)
 * @param from 조회 시작 시간 (optional)
 * @param to 조회 종료 시간 (optional)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record CrawlExecutionCriteria(
        CrawlTaskId crawlTaskId,
        CrawlSchedulerId crawlSchedulerId,
        CrawlExecutionStatus status,
        Instant from,
        Instant to,
        int page,
        int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    public CrawlExecutionCriteria {
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size <= 0) {
            size = DEFAULT_SIZE;
        }
    }

    // ===== Static Factory Methods =====

    /**
     * Task ID로 조회 조건 생성
     *
     * @param crawlTaskId 태스크 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return CrawlExecutionCriteria
     */
    public static CrawlExecutionCriteria byTaskId(CrawlTaskId crawlTaskId, int page, int size) {
        return new CrawlExecutionCriteria(crawlTaskId, null, null, null, null, page, size);
    }

    /**
     * Scheduler ID + 기간으로 조회 조건 생성
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return CrawlExecutionCriteria
     */
    public static CrawlExecutionCriteria bySchedulerIdAndPeriod(
            CrawlSchedulerId crawlSchedulerId, Instant from, Instant to, int page, int size) {
        return new CrawlExecutionCriteria(null, crawlSchedulerId, null, from, to, page, size);
    }

    /**
     * Scheduler ID + 상태 + 기간으로 조회 조건 생성
     *
     * @param crawlSchedulerId 스케줄러 ID
     * @param status 상태 필터
     * @param from 조회 시작 시간
     * @param to 조회 종료 시간
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return CrawlExecutionCriteria
     */
    public static CrawlExecutionCriteria bySchedulerIdAndStatusAndPeriod(
            CrawlSchedulerId crawlSchedulerId,
            CrawlExecutionStatus status,
            Instant from,
            Instant to,
            int page,
            int size) {
        return new CrawlExecutionCriteria(null, crawlSchedulerId, status, from, to, page, size);
    }

    // ===== Helper Methods =====

    /**
     * offset 계산 (페이징용)
     *
     * @return 시작 위치
     */
    public long offset() {
        return (long) page * size;
    }

    /**
     * Task ID 필터 여부
     *
     * @return Task ID 필터가 있으면 true
     */
    public boolean hasTaskIdFilter() {
        return crawlTaskId != null;
    }

    /**
     * Scheduler ID 필터 여부
     *
     * @return Scheduler ID 필터가 있으면 true
     */
    public boolean hasSchedulerIdFilter() {
        return crawlSchedulerId != null;
    }

    /**
     * 상태 필터 여부
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return status != null;
    }

    /**
     * 기간 필터 여부
     *
     * @return 시작 또는 종료 시간이 있으면 true
     */
    public boolean hasPeriodFilter() {
        return from != null || to != null;
    }
}
