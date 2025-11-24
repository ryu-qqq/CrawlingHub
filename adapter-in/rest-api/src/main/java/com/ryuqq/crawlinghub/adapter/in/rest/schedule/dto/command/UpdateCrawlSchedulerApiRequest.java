package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command;

import jakarta.validation.constraints.Size;

/**
 * Update CrawlScheduler API Request
 *
 * <p>크롤 스케줄러 수정 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>schedulerName: 선택적, 1-100자
 *   <li>cronExpression: 선택적, 1-100자 (AWS EventBridge 크론 형식)
 *   <li>active: 선택적 (true=ACTIVE, false=INACTIVE)
 * </ul>
 *
 * @param schedulerName 스케줄러 이름 (선택적)
 * @param cronExpression 크론 표현식 (선택적)
 * @param active 활성화 여부 (선택적)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateCrawlSchedulerApiRequest(
        @Size(min = 1, max = 100, message = "스케줄러 이름은 1-100자여야 합니다") String schedulerName,
        @Size(min = 1, max = 100, message = "크론 표현식은 1-100자여야 합니다") String cronExpression,
        Boolean active) {}
