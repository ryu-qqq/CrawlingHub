package com.ryuqq.crawlinghub.adapter.in.rest.task.mapper;

import static com.ryuqq.crawlinghub.adapter.in.rest.common.util.DateTimeFormatUtils.format;

import com.ryuqq.crawlinghub.adapter.in.rest.common.dto.response.PageApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.query.SearchCrawlTasksOutboxApiRequest;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.CrawlTaskOutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.task.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.task.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * CrawlTaskOutboxApiMapper - Outbox REST API ↔ Application Layer 변환
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>SearchCrawlTasksOutboxApiRequest → GetOutboxListQuery 변환
 *   <li>OutboxResponse → CrawlTaskOutboxApiResponse 변환
 *   <li>PageResponse → PageApiResponse 변환
 *   <li>RepublishResultResponse → RepublishResultApiResponse 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Component
public class CrawlTaskOutboxApiMapper {

    /**
     * SearchCrawlTasksOutboxApiRequest → GetOutboxListQuery 변환
     *
     * <p><strong>Strict Validation</strong>: 유효하지 않은 상태 값은 IllegalArgumentException을 발생시킵니다.
     *
     * @param request Outbox 목록 조회 API 요청
     * @return GetOutboxListQuery
     * @throws IllegalArgumentException 유효하지 않은 상태 값인 경우
     */
    public GetOutboxListQuery toQuery(SearchCrawlTasksOutboxApiRequest request) {
        List<OutboxStatus> parsedStatuses = parseStatuses(request.statuses());
        return GetOutboxListQuery.of(
                parsedStatuses,
                request.createdFrom(),
                request.createdTo(),
                request.page(),
                request.size());
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
     * OutboxResponse → CrawlTaskOutboxApiResponse 변환
     *
     * @param appResponse Application Layer 응답
     * @return API 응답
     */
    public CrawlTaskOutboxApiResponse toApiResponse(OutboxResponse appResponse) {
        return new CrawlTaskOutboxApiResponse(
                appResponse.crawlTaskId(),
                appResponse.idempotencyKey(),
                appResponse.status() != null ? appResponse.status().name() : null,
                appResponse.retryCount(),
                format(appResponse.createdAt()),
                format(appResponse.updatedAt()),
                format(appResponse.processedAt()));
    }

    /**
     * PageResponse<OutboxResponse> → PageApiResponse<CrawlTaskOutboxApiResponse> 변환
     *
     * @param appPageResponse Application Layer 페이징 응답
     * @return API 페이징 응답
     */
    public PageApiResponse<CrawlTaskOutboxApiResponse> toPageApiResponse(
            PageResponse<OutboxResponse> appPageResponse) {
        List<CrawlTaskOutboxApiResponse> content =
                appPageResponse.content().stream().map(this::toApiResponse).toList();
        return PageApiResponse.of(
                content,
                appPageResponse.page(),
                appPageResponse.size(),
                appPageResponse.totalElements());
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
}
