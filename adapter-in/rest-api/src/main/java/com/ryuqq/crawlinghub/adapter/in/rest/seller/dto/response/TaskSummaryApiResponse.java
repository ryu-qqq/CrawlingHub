package com.ryuqq.crawlinghub.adapter.in.rest.seller.dto.response;

/**
 * Task Summary API Response
 *
 * <p>셀러 상세 조회 시 포함되는 최근 태스크 요약 정보
 *
 * @param taskId 태스크 ID
 * @param status 태스크 상태 (PENDING/RUNNING/SUCCESS/FAILED 등)
 * @param taskType 태스크 유형 (FULL_SYNC/INCREMENTAL 등)
 * @param createdAt 생성 일시 (ISO-8601 형식)
 * @author development-team
 * @since 1.0.0
 */
public record TaskSummaryApiResponse(
        Long taskId, String status, String taskType, String createdAt) {}
