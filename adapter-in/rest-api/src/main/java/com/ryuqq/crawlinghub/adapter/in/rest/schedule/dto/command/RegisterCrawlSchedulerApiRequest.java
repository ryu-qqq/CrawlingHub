package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Register CrawlScheduler API Request
 *
 * <p>크롤 스케줄러 등록 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>sellerId: 필수, 양수
 *   <li>schedulerName: 필수, 1-100자
 *   <li>cronExpression: 필수, 1-100자 (AWS EventBridge 크론 형식)
 * </ul>
 *
 * @param sellerId 셀러 ID
 * @param schedulerName 스케줄러 이름 (셀러별 중복 불가)
 * @param cronExpression 크론 표현식 (AWS EventBridge 형식)
 * @author development-team
 * @since 1.0.0
 */
public record RegisterCrawlSchedulerApiRequest(
        @NotNull(message = "셀러 ID는 필수입니다") @Positive(message = "셀러 ID는 양수여야 합니다") Long sellerId,
        @NotBlank(message = "스케줄러 이름은 필수입니다")
                @Size(min = 1, max = 100, message = "스케줄러 이름은 1-100자여야 합니다")
                String schedulerName,
        @NotBlank(message = "크론 표현식은 필수입니다")
                @Size(min = 1, max = 100, message = "크론 표현식은 1-100자여야 합니다")
                String cronExpression) {}
