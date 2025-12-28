package com.ryuqq.crawlinghub.domain.task.vo;

import com.ryuqq.crawlinghub.domain.schedule.identifier.CrawlSchedulerId;
import com.ryuqq.crawlinghub.domain.seller.identifier.SellerId;
import java.time.Instant;

/**
 * CrawlTask 조회 조건 Value Object
 *
 * <p><strong>조회 조건</strong>:
 *
 * <ul>
 *   <li>crawlSchedulerId: 스케줄러 ID (선택, null이면 전체)
 *   <li>sellerId: 셀러 ID (선택, null이면 전체)
 *   <li>status: 상태 필터 (선택, null이면 전체)
 *   <li>taskType: 태스크 유형 필터 (선택, null이면 전체)
 *   <li>createdFrom: 생성일시 시작 (선택)
 *   <li>createdTo: 생성일시 종료 (선택)
 *   <li>page: 페이지 번호 (0부터 시작)
 *   <li>size: 페이지 크기
 * </ul>
 *
 * @param crawlSchedulerId 스케줄러 ID (선택)
 * @param sellerId 셀러 ID (선택)
 * @param status 상태 필터 (선택)
 * @param taskType 태스크 유형 필터 (선택)
 * @param createdFrom 생성일시 시작 (선택)
 * @param createdTo 생성일시 종료 (선택)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record CrawlTaskCriteria(
        CrawlSchedulerId crawlSchedulerId,
        SellerId sellerId,
        CrawlTaskStatus status,
        CrawlTaskType taskType,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public CrawlTaskCriteria {
        if (page < 0) {
            page = DEFAULT_PAGE;
        }
        if (size <= 0 || size > MAX_SIZE) {
            size = DEFAULT_SIZE;
        }
    }

    /**
     * offset 계산 (페이징용)
     *
     * @return 시작 위치
     */
    public long offset() {
        return (long) page * size;
    }

    /**
     * 스케줄러 ID 필터 여부
     *
     * @return 스케줄러 ID 필터가 있으면 true
     */
    public boolean hasSchedulerIdFilter() {
        return crawlSchedulerId != null;
    }

    /**
     * 셀러 ID 필터 여부
     *
     * @return 셀러 ID 필터가 있으면 true
     */
    public boolean hasSellerIdFilter() {
        return sellerId != null;
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
     * 태스크 유형 필터 여부
     *
     * @return 태스크 유형 필터가 있으면 true
     */
    public boolean hasTaskTypeFilter() {
        return taskType != null;
    }

    /**
     * 생성일시 시작 필터 여부
     *
     * @return 생성일시 시작 필터가 있으면 true
     */
    public boolean hasCreatedFromFilter() {
        return createdFrom != null;
    }

    /**
     * 생성일시 종료 필터 여부
     *
     * @return 생성일시 종료 필터가 있으면 true
     */
    public boolean hasCreatedToFilter() {
        return createdTo != null;
    }
}
