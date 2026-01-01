package com.ryuqq.crawlinghub.application.task.port.in.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.OutboxResponse;

/**
 * Outbox 목록 조회 UseCase
 *
 * <p>PENDING 또는 FAILED 상태의 Outbox 목록을 페이징하여 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetOutboxListUseCase {

    /**
     * Outbox 목록을 페이징하여 조회합니다.
     *
     * @param query 조회 조건 (상태, 페이지, 크기)
     * @return 페이징된 Outbox 목록
     */
    PageResponse<OutboxResponse> execute(GetOutboxListQuery query);
}
