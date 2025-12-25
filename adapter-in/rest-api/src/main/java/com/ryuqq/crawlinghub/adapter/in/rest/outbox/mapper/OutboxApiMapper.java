package com.ryuqq.crawlinghub.adapter.in.rest.outbox.mapper;

import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.OutboxApiResponse;
import com.ryuqq.crawlinghub.adapter.in.rest.outbox.dto.response.RepublishResultApiResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.dto.response.RepublishResultResponse;
import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.time.Instant;
import java.util.List;
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
     * Query Parameter → GetOutboxListQuery 변환
     *
     * @param status 상태 필터 (optional)
     * @param limit 조회 개수
     * @return GetOutboxListQuery
     */
    public GetOutboxListQuery toQuery(String status, int limit) {
        List<OutboxStatus> statuses = null;
        if (status != null && !status.isBlank()) {
            statuses = List.of(OutboxStatus.valueOf(status.toUpperCase()));
        }
        return GetOutboxListQuery.of(statuses, limit);
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
