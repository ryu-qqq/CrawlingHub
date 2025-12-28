package com.ryuqq.crawlinghub.application.outbox.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
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
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>페이징 조건으로 Outbox 목록 조회
 *   <li>전체 개수 조회 후 PageResponse 생성
 * </ul>
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
    public PageResponse<OutboxResponse> execute(GetOutboxListQuery query) {
        CrawlTaskOutboxCriteria criteria =
                CrawlTaskOutboxCriteria.byStatusesWithPaging(
                        query.statuses(), query.offset(), query.size());

        List<CrawlTaskOutbox> outboxList = outboxQueryPort.findByCriteria(criteria);
        long totalElements = outboxQueryPort.countByCriteria(criteria);

        List<OutboxResponse> content = outboxList.stream().map(this::toResponse).toList();

        int totalPages = calculateTotalPages(totalElements, query.size());
        boolean isFirst = query.page() == 0;
        boolean isLast = query.page() >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(
                content, query.page(), query.size(), totalElements, totalPages, isFirst, isLast);
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

    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0 || size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}
