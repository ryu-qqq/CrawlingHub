package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Update CrawlScheduler API Request
 *
 * <p>크롤 스케줄러 수정 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>schedulerName: 필수, 1-100자
 *   <li>cronExpression: 필수, 1-100자 (AWS EventBridge 크론 형식)
 *   <li>active: 필수 (true=ACTIVE, false=INACTIVE)
 * </ul>
 *
 * @param schedulerName 스케줄러 이름 (필수)
 * @param cronExpression 크론 표현식 (필수)
 * @param active 활성화 여부 (필수)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateCrawlSchedulerApiRequest(
        @NotBlank(message = "스케줄러 이름은 필수입니다")
                @Size(min = 1, max = 100, message = "스케줄러 이름은 1-100자여야 합니다")
                String schedulerName,
        @NotBlank(message = "크론 표현식은 필수입니다")
                @Size(min = 1, max = 100, message = "크론 표현식은 1-100자여야 합니다")
                String cronExpression,
        @NotNull(message = "활성화 여부는 필수입니다") Boolean active) {}
