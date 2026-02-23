package com.ryuqq.crawlinghub.application.task.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.task.dto.query.GetOutboxListQuery;
import com.ryuqq.crawlinghub.application.task.dto.response.OutboxResponse;
import com.ryuqq.crawlinghub.application.task.factory.query.CrawlTaskOutboxCriteriaFactory;
import com.ryuqq.crawlinghub.application.task.manager.CrawlTaskOutboxReadManager;
import com.ryuqq.crawlinghub.application.task.port.in.query.GetOutboxListUseCase;
import com.ryuqq.crawlinghub.domain.task.aggregate.CrawlTaskOutbox;
import com.ryuqq.crawlinghub.domain.task.query.CrawlTaskOutboxCriteria;
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
 * <p><strong>의존성</strong>:
 *
 * <ul>
 *   <li>CrawlTaskOutboxReadManager: Outbox 조회 전용 Manager
 *   <li>CrawlTaskOutboxCriteriaFactory: Query → Criteria 변환 Factory
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class GetOutboxListService implements GetOutboxListUseCase {

    private final CrawlTaskOutboxReadManager readManager;
    private final CrawlTaskOutboxCriteriaFactory criteriaFactory;

    /**
     * GetOutboxListService 생성자
     *
     * @param readManager Outbox 조회 전용 Manager
     * @param criteriaFactory Query → Criteria 변환 Factory
     */
    public GetOutboxListService(
            CrawlTaskOutboxReadManager readManager,
            CrawlTaskOutboxCriteriaFactory criteriaFactory) {
        this.readManager = readManager;
        this.criteriaFactory = criteriaFactory;
    }

    @Override
    public PageResponse<OutboxResponse> execute(GetOutboxListQuery query) {
        CrawlTaskOutboxCriteria criteria = criteriaFactory.create(query);

        List<CrawlTaskOutbox> outboxList = readManager.findByCriteria(criteria);
        long totalElements = readManager.countByCriteria(criteria);

        List<OutboxResponse> content = outboxList.stream().map(this::toResponse).toList();

        int totalPages = calculateTotalPages(totalElements, query.size());
        boolean isFirst = query.page() == 0;
        boolean isLast = query.page() >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(
                content, query.page(), query.size(), totalElements, totalPages, isFirst, isLast);
    }

    private OutboxResponse toResponse(CrawlTaskOutbox outbox) {
        return new OutboxResponse(
                outbox.getCrawlTaskIdValue(),
                outbox.getIdempotencyKey(),
                outbox.getStatus(),
                outbox.getRetryCount(),
                outbox.getCreatedAt(),
                null, // updatedAt: domain 모델에 미존재, 향후 추가 예정
                outbox.getProcessedAt());
    }

    private int calculateTotalPages(long totalElements, int size) {
        if (totalElements == 0 || size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / size);
    }
}
