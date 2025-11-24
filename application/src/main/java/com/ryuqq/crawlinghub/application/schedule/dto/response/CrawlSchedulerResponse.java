package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.LocalDateTime;

/**
 * 크롤 스케줄러 기본 응답 DTO.
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 */
public record CrawlSchedulerResponse(
        Long crawlSchedulerId,
        Long sellerId,
        String schedulerName,
        String cronExpression,
        SchedulerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
