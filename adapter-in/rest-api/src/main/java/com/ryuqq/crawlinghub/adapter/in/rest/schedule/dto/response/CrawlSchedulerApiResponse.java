package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.response;

/**
 * CrawlScheduler API Response
 *
 * <p>크롤 스케줄러 상세 정보 API 응답 DTO
 *
 * <p><strong>응답 필드:</strong>
 *
 * <ul>
 *   <li>crawlSchedulerId: 크롤 스케줄러 ID
 *   <li>sellerId: 셀러 ID
 *   <li>schedulerName: 스케줄러 이름
 *   <li>cronExpression: 크론 표현식
 *   <li>status: 스케줄러 상태 (ACTIVE/INACTIVE)
 *   <li>createdAt: 생성 일시 (ISO-8601 형식)
 *   <li>updatedAt: 수정 일시 (ISO-8601 형식)
 * </ul>
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 일시 (ISO-8601 형식)
 * @param updatedAt 수정 일시 (ISO-8601 형식)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerApiResponse(
        Long crawlSchedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        String status,
        String createdAt,
        String updatedAt) {}
