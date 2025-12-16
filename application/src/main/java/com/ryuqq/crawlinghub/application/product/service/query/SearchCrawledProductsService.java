package com.ryuqq.crawlinghub.application.product.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.product.assembler.CrawledProductAssembler;
import com.ryuqq.crawlinghub.application.product.dto.query.SearchCrawledProductsQuery;
import com.ryuqq.crawlinghub.application.product.dto.response.CrawledProductSummaryResponse;
import com.ryuqq.crawlinghub.application.product.port.in.query.SearchCrawledProductsUseCase;
import com.ryuqq.crawlinghub.application.product.port.out.query.CrawledProductQueryPort;
import com.ryuqq.crawlinghub.domain.product.aggregate.CrawledProduct;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * CrawledProduct 검색 Service
 *
 * <p>크롤링 상품 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용
 *   <li>Query DTO로 검색 조건 전달
 *   <li>Domain → SummaryResponse 변환 (Assembler)
 * </ul>
 *
 * <p><strong>트랜잭션</strong>: QueryService는 @Transactional 사용 금지 (읽기 전용, 불필요)
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchCrawledProductsService implements SearchCrawledProductsUseCase {

    private final CrawledProductQueryPort queryPort;
    private final CrawledProductAssembler assembler;

    public SearchCrawledProductsService(
            CrawledProductQueryPort queryPort, CrawledProductAssembler assembler) {
        this.queryPort = queryPort;
        this.assembler = assembler;
    }

    @Override
    public PageResponse<CrawledProductSummaryResponse> execute(SearchCrawledProductsQuery query) {
        // 1. 검색 및 개수 조회
        List<CrawledProduct> products = queryPort.search(query);
        long totalElements = queryPort.count(query);

        // 2. Domain → Response 변환
        List<CrawledProductSummaryResponse> content =
                products.stream().map(assembler::toSummaryResponse).toList();

        // 3. PageResponse 생성
        int page = query.page();
        int size = query.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1 || totalPages == 0;

        return PageResponse.of(content, page, size, totalElements, totalPages, first, last);
    }
}
