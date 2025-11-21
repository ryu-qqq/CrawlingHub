package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response;

/**
 * CrawlScheduler Summary API Response
 *
 * <p>크롤 스케줄러 요약 정보 API 응답 DTO (목록 조회용)
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID
 *   <li>sellerId: 셀러 ID
 *   <li>schedulerName: 스케줄러 이름
 *   <li>cronExpression: 크론 표현식
 *   <li>status: 스케줄러 상태 (ACTIVE/INACTIVE)
 * </ul>
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerSummaryApiResponse(
        Long crawlSchedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        String status) {}
