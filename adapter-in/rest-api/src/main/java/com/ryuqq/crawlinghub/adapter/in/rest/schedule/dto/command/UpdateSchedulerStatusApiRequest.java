package com.ryuqq.crawlinghub.adapter.in.rest.schedule.dto.command;

import jakarta.validation.constraints.NotNull;

/**
 * Update Scheduler Status API Request
 *
 * <p>스케줄러 상태 변경 API 요청 DTO
 *
 * <p><strong>Validation 규칙:</strong>
 *
 * <ul>
 *   <li>active: 필수 (true=ACTIVE, false=INACTIVE)
 * </ul>
 *
 * @param active 활성화 여부 (필수, true=ACTIVE, false=INACTIVE)
 * @author development-team
 * @since 1.0.0
 */
public record UpdateSchedulerStatusApiRequest(@NotNull(message = "활성화 여부는 필수입니다") Boolean active) {}
