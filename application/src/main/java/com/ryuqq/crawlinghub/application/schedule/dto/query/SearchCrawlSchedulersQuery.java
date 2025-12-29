package com.ryuqq.crawlinghub.application.schedule.dto.query;

import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;

/**
 * Search CrawlSchedulers Query
 *
 * <p>크롤 스케줄러 목록 조회 조건
 *
 * @param sellerId 셀러 ID (선택, null이면 전체 조회)
 * @param statuses 스케줄러 상태 목록 (선택, null이거나 빈 목록이면 전체)
 * @param createdFrom 생성일 시작 (선택, null이면 필터 없음)
 * @param createdTo 생성일 종료 (선택, null이면 필터 없음)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlSchedulersQuery(
        Long sellerId,
        List<SchedulerStatus> statuses,
        Instant createdFrom,
        Instant createdTo,
        Integer page,
        Integer size) {

    /**
     * 상태 필터가 있는지 확인
     *
     * @return 상태 필터가 있으면 true
     */
    public boolean hasStatusFilter() {
        return statuses != null && !statuses.isEmpty();
    }

    /**
     * 기간 필터가 있는지 확인
     *
     * @return 기간 필터가 있으면 true
     */
    public boolean hasDateFilter() {
        return createdFrom != null || createdTo != null;
    }

    /**
     * 단일 상태 반환 (하위 호환성)
     *
     * @return 첫 번째 상태 또는 null
     */
    public SchedulerStatus status() {
        return hasStatusFilter() ? statuses.get(0) : null;
    }
}
