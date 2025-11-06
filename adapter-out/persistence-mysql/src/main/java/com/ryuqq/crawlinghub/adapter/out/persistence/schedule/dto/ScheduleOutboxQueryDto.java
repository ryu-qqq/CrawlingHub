package com.ryuqq.crawlinghub.adapter.out.persistence.schedule.dto;

import com.ryuqq.crawlinghub.domain.schedule.outbox.ScheduleOutbox;

import java.time.LocalDateTime;

/**
 * ScheduleOutbox Query DTO (QueryDSL Projections용)
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
 *   <li>✅ Domain Enum 직접 사용 (Entity 내부 Enum 금지)</li>
 * </ul>
 *
 * @param id PK ID
 * @param opId Orchestrator OpId (Nullable - 초기 저장 시 null)
 * @param sellerId 셀러 ID (Long FK)
 * @param idemKey 멱등성 키
 * @param domain 도메인
 * @param eventType 이벤트 타입
 * @param bizKey 비즈니스 키
 * @param payload 페이로드 JSON
 * @param outcomeJson 결과 JSON (Nullable)
 * @param operationState 작업 상태
 * @param walState WAL 상태
 * @param errorMessage 에러 메시지 (Nullable)
 * @param retryCount 재시도 횟수
 * @param maxRetries 최대 재시도 횟수
 * @param timeoutMillis 타임아웃 (밀리초)
 * @param completedAt 완료 일시 (Nullable)
 * @param createdAt 생성 일시
 * @param updatedAt 수정 일시
 *
 * @author ryu-qqq
 * @since 2025-11-06
 */
public record ScheduleOutboxQueryDto(
    Long id,
    String opId,
    Long sellerId,
    String idemKey,
    String domain,
    String eventType,
    String bizKey,
    String payload,
    String outcomeJson,
    ScheduleOutbox.OperationState operationState,
    ScheduleOutbox.WriteAheadState walState,
    String errorMessage,
    Integer retryCount,
    Integer maxRetries,
    Long timeoutMillis,
    LocalDateTime completedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * QueryDSL Projections.constructor() 호환 생성자
     */
    public ScheduleOutboxQueryDto {
        // Record Compact Constructor - Validation
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("sellerId must not be null");
        }
        if (idemKey == null || idemKey.isBlank()) {
            throw new IllegalArgumentException("idemKey must not be null or blank");
        }
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be null or blank");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be null or blank");
        }
        if (bizKey == null || bizKey.isBlank()) {
            throw new IllegalArgumentException("bizKey must not be null or blank");
        }
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("payload must not be null or blank");
        }
        if (operationState == null) {
            throw new IllegalArgumentException("operationState must not be null");
        }
        if (walState == null) {
            throw new IllegalArgumentException("walState must not be null");
        }
        if (retryCount == null) {
            throw new IllegalArgumentException("retryCount must not be null");
        }
        if (maxRetries == null) {
            throw new IllegalArgumentException("maxRetries must not be null");
        }
        if (timeoutMillis == null) {
            throw new IllegalArgumentException("timeoutMillis must not be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }
        // opId, outcomeJson, errorMessage, completedAt은 null 허용
    }
}
