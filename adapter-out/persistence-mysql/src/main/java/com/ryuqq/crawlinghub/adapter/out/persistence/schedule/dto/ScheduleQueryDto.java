package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto;

import com.ryuqq.crawlinghub.adapter.out.persistence.schedule.entity.ScheduleEntity;

import java.time.LocalDateTime;

/**
 * Schedule Query DTO (QueryDSL Projections용)
 *
 * <p><strong>역할:</strong></p>
 * <ul>
 *   <li>✅ QueryDSL Projections.constructor() 사용</li>
 *   <li>✅ DTO 직접 반환 (Domain Model 거치지 않음)</li>
 *   <li>✅ Record 패턴 (불변성 보장)</li>
 * </ul>
 *
 * <p><strong>컨벤션 준수:</strong></p>
 * <ul>
 *   <li>✅ Record 패턴 사용</li>
 *   <li>✅ QueryDSL Projections와 호환</li>
 *   <li>✅ Null 허용 필드 명시</li>
 * </ul>
 *
 * @param id 스케줄 ID
 * @param sellerId 셀러 ID (Long FK)
 * @param cronExpression Cron 표현식
 * @param status 스케줄 상태
 * @param nextExecutionTime 다음 실행 시간
 * @param lastExecutedAt 마지막 실행 시간 (Nullable)
 * @param createdAt 생성 시간
 * @param updatedAt 수정 시간
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record ScheduleQueryDto(
    Long id,
    Long sellerId,
    String cronExpression,
    ScheduleEntity.ScheduleStatus status,
    LocalDateTime nextExecutionTime,
    LocalDateTime lastExecutedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * QueryDSL Projections.constructor() 호환 생성자
     */
    public ScheduleQueryDto {
        // Record Compact Constructor - Validation
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId must not be null");
        }
        if (cronExpression == null || cronExpression.isBlank()) {
            throw new IllegalArgumentException("cronExpression must not be null or blank");
        }
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
        // nextExecutionTime, lastExecutedAt은 null 허용
    }
}
