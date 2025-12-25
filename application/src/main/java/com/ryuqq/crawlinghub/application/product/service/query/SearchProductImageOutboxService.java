package com.ryuqq.crawlinghub.application.product.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchProductImageOutboxQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.ProductImageOutboxResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchProductImageOutboxUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.ImageOutboxQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.ProductImageOutbox;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * ProductImageOutbox 검색 Service
 *
 * <p>이미지 업로드 Outbox 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>Query DTO로 검색 조건 전달
 *   <li>Domain → Response 변환 (정적 팩토리)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 사용 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchProductImageOutboxService implements SearchProductImageOutboxUseCase {

    private final ImageOutboxQueryPort queryPort;

    public SearchProductImageOutboxService(ImageOutboxQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public PageResponse<ProductImageOutboxResponse> execute(SearchProductImageOutboxQuery query) {
        // 1. 검색 및 개수 조회
        List<ProductImageOutbox> outboxes =
                queryPort.search(
                        query.crawledProductImageId(),
                        query.status(),
                        query.getOffset(),
                        query.size());
        long totalElements = queryPort.count(query.crawledProductImageId(), query.status());

        // 2. Domain → Response 변환
        List<ProductImageOutboxResponse> content =
                outboxes.stream().map(ProductImageOutboxResponse::from).toList();

        // 3. PageResponse 생성
        int page = query.page();
        int size = query.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
