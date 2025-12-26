package com.ryuqq.crawlinghub.application.outbox.dto.query;

import com.ryuqq.crawlinghub.domain.task.vo.OutboxStatus;
import java.util.List;

/**
 * Outbox 목록 조회 Query DTO
 *
 * @param statuses 조회할 상태 목록 (null이면 PENDING, FAILED 조회)
 * @param limit 조회 개수 제한
 * @author development-team
 * @since 1.0.0
 */
public record GetOutboxListQuery(List<OutboxStatus> statuses, int limit) {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 500;

    public GetOutboxListQuery {
        if (limit <= 0) {
            limit = DEFAULT_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            limit = MAX_LIMIT;
        }
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(OutboxStatus.PENDING, OutboxStatus.FAILED);
        } else {
            statuses = List.copyOf(statuses);
        }
    }

    public static GetOutboxListQuery of(List<OutboxStatus> statuses, int limit) {
        return new GetOutboxListQuery(statuses, limit);
    }

    public static GetOutboxListQuery pendingOrFailed(int limit) {
        return new GetOutboxListQuery(List.of(OutboxStatus.PENDING, OutboxStatus.FAILED), limit);
    }
}
