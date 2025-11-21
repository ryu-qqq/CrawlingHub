package com.ryuqq.crawlinghub.application.schedule.dto.query;

import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;

/**
 * Search CrawlSchedulers Query
 *
 * <p>크롤 스케줄러 목록 조회 조건
 *
 * @param sellerId 셀러 ID (선택, null이면 전체 조회)
 * @param status 스케줄러 상태 (선택, null이면 전체)
 * @param page 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @author development-team
 * @since 1.0.0
 */
public record SearchCrawlSchedulersQuery(
        Long sellerId, SchedulerStatus status, Integer page, Integer size) {}
