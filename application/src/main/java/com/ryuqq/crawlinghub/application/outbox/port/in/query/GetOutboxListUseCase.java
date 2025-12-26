package com.ryuqq.crawlinghub.application.outbox.port.in.query;

import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import java.util.List;

/**
 * Outbox 목록 조회 UseCase
 *
 * <p>PENDING 또는 FAILED 상태의 Outbox 목록을 조회합니다.
 *
 * @author development-team
 * @since 1.0.0
 */
public interface GetOutboxListUseCase {

    /**
     * Outbox 목록을 조회합니다.
     *
     * @param query 조회 조건
     * @return Outbox 목록
     */
    List<OutboxResponse> execute(GetOutboxListQuery query);
}
