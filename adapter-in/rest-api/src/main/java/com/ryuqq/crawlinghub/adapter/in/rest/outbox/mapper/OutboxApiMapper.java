package com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * OutboxApiMapper - Outbox REST API ↔ Application Layer 변환
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class OutboxApiMapper {

    /**
     * Query Parameter → GetOutboxListQuery 변환 (페이징 + 기간 필터 포함)
     *
     * <p><strong>Strict Validation</strong>: 유효하지 않은 상태 값은 IllegalArgumentException을 발생시킵니다.
     *
     * @param statuses 상태 필터 목록 (optional)
     * @param createdFrom 생성일 시작 범위 (optional, inclusive)
     * @param createdTo 생성일 종료 범위 (optional, exclusive)
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return GetOutboxListQuery
     * @throws IllegalArgumentException 유효하지 않은 상태 값인 경우
     */
    public GetOutboxListQuery toQuery(
            List<String> statuses, Instant createdFrom, Instant createdTo, int page, int size) {
        List<OutboxStatus> parsedStatuses = parseStatuses(statuses);
        return GetOutboxListQuery.of(parsedStatuses, createdFrom, createdTo, page, size);
    }

    /**
     * 상태 문자열 목록 → OutboxStatus 목록 변환 (Strict Validation)
     *
     * @param statuses 상태 문자열 목록
     * @return OutboxStatus 목록 (null 또는 empty면 null 반환)
     * @throws IllegalArgumentException 유효하지 않은 상태 값인 경우
     */
    private List<OutboxStatus> parseStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return null;
        }
        return statuses.stream().map(this::parseStatus).toList();
    }

    /**
     * 단일 상태 문자열 → OutboxStatus 변환 (Strict Validation)
     *
     * @param status 상태 문자열
     * @return OutboxStatus
     * @throws IllegalArgumentException 유효하지 않은 상태 값인 경우
     */
    private OutboxStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or blank");
        }
        try {
            return OutboxStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            String validValues =
                    Arrays.stream(OutboxStatus.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid outbox status: '" + status + "'. Valid values: " + validValues);
        }
    }

    /**
     * OutboxResponse → OutboxApiResponse 변환
     *
     * @param appResponse Application Layer 응답
     * @return API 응답
     */
    public OutboxApiResponse toApiResponse(OutboxResponse appResponse) {
        return new OutboxApiResponse(
                appResponse.crawlTaskId(),
                appResponse.idempotencyKey(),
                appResponse.status().name(),
                appResponse.retryCount(),
                toIsoString(appResponse.createdAt()),
                toIsoString(appResponse.processedAt()));
    }

    /**
     * PageResponse<OutboxResponse> → PageApiResponse<OutboxApiResponse> 변환
     *
     * @param appPageResponse Application Layer 페이징 응답
     * @return API 페이징 응답
     */
    public PageApiResponse<OutboxApiResponse> toPageApiResponse(
            PageResponse<OutboxResponse> appPageResponse) {
        return PageApiResponse.from(appPageResponse, this::toApiResponse);
    }

    /**
     * RepublishResultResponse → RepublishResultApiResponse 변환
     *
     * @param appResponse Application Layer 응답
     * @return API 응답
     */
    public RepublishResultApiResponse toRepublishApiResponse(RepublishResultResponse appResponse) {
        return new RepublishResultApiResponse(
                appResponse.crawlTaskId(), appResponse.success(), appResponse.message());
    }

    private String toIsoString(Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
