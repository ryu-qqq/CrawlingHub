package com.ryuqq.crawlinghub.application.schedule.dto.response;

import java.time.Instant;

/**
 * 스케줄러 실행 정보
 *
 * @param nextExecutionTime 다음 실행 예정 시각
 * @param lastExecutionTime 마지막 실행 시각
 * @param lastExecutionStatus 마지막 실행 상태
 * @author development-team
 * @since 1.0.0
 */
public record ExecutionInfo(
        Instant nextExecutionTime, Instant lastExecutionTime, String lastExecutionStatus) {}
