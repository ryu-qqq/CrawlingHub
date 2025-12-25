package com.ryuqq.crawlinghub.application.outbox.service.query;

import com.ryuqq.crawlinghub.application.outbox.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.outbox.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.outbox.port.in.query.GetOutboxListUseCase;
import com.ryuqq.crawlinghub.application.task.port.out.query.CrawlTaskOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.vo.CrawlTaskOutboxCriteria;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Outbox 목록 조회 Service
 *
 * <p>GetOutboxListUseCase 구현체
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetOutboxListService implements GetOutboxListUseCase {

    private final CrawlTaskOutboxQueryPort outboxQueryPort;

    public GetOutboxListService(CrawlTaskOutboxQueryPort outboxQueryPort) {
        this.outboxQueryPort = outboxQueryPort;
    }

    @Override
    public List<OutboxResponse> execute(GetOutboxListQuery query) {
        CrawlTaskOutboxCriteria criteria =
                CrawlTaskOutboxCriteria.byStatuses(query.statuses(), query.limit());
        List<CrawlTaskOutbox> outboxList = outboxQueryPort.findByCriteria(criteria);

        return outboxList.stream().map(this::toResponse).toList();
    }

    private OutboxResponse toResponse(CrawlTaskOutbox outbox) {
        return new OutboxResponse(
                outbox.getCrawlTaskId().value(),
                outbox.getIdempotencyKey(),
                outbox.getStatus(),
                outbox.getRetryCount(),
                outbox.getCreatedAt(),
                outbox.getProcessedAt());
    }
}
