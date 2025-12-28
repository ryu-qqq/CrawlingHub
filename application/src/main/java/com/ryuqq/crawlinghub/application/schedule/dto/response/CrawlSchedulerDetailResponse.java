package com.ryuqq.crawlinghub.application.schedule.dto.response;

import com.ryuqq.crawlinghub.domain.schedule.vo.SchedulerStatus;
import java.time.Instant;
import java.util.List;

/**
 * 크롤 스케줄러 상세 응답 DTO
 *
 * @param crawlSchedulerId 크롤 스케줄러 ID
 * @param schedulerName 스케줄러 이름
 * @param cronExpression 크론 표현식
 * @param status 스케줄러 상태
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param seller 셀러 요약 정보
 * @param execution 실행 정보
 * @param statistics 통계 정보
 * @param recentTasks 최근 태스크 목록 (최대 10개)
 * @author development-team
 * @since 1.0.0
 */
public record CrawlSchedulerDetailResponse(
        Long crawlSchedulerId,
        String schedulerName,
        String cronExpression,
        SchedulerStatus status,
        Instant createdAt,
        Instant updatedAt,
        SellerSummaryForScheduler seller,
        ExecutionInfo execution,
        SchedulerStatistics statistics,
        List<TaskSummaryForScheduler> recentTasks) {}
