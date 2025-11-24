package com.ryuqq.crawlinghub.application.seller.service.query;

import com.ryuqq.crawlinghub.application.common.dto.response.PageResponse;
import com.ryuqq.crawlinghub.application.seller.assembler.SellerAssembler;
import com.ryuqq.crawlinghub.application.seller.dto.query.SearchSellersQuery;
import com.ryuqq.crawlinghub.application.seller.dto.response.SellerSummaryResponse;
import com.ryuqq.crawlinghub.application.seller.port.in.query.SearchSellersUseCase;
import com.ryuqq.crawlinghub.application.seller.port.out.query.SellerQueryPort;
import com.ryuqq.crawlinghub.domain.seller.aggregate.Seller;
import com.ryuqq.crawlinghub.domain.seller.vo.SellerQueryCriteria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Search Sellers Service
 *
 * <p>셀러 목록 조회 UseCase 구현
 *
 * <ul>
 *   <li>조회 전용 (읽기 전용 트랜잭션)
 *   <li>Query DTO → Criteria 변환
 *   <li>Domain → SummaryResponse 변환
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@Service
public class SearchSellersService implements SearchSellersUseCase {

    private final SellerQueryPort sellerQueryPort;
    private final SellerAssembler assembler;

    public SearchSellersService(SellerQueryPort sellerQueryPort, SellerAssembler assembler) {
        this.sellerQueryPort = sellerQueryPort;
        this.assembler = assembler;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerSummaryResponse> execute(SearchSellersQuery query) {
        // 1. Query → Criteria 변환 (Assembler)
        SellerQueryCriteria criteria = assembler.toCriteria(query);

        // 2. 조회
        List<Seller> sellers = sellerQueryPort.findByCriteria(criteria);
        long totalElements = sellerQueryPort.countByCriteria(criteria);

        // 3. Domain → PageResponse 변환 (Assembler)
        return assembler.toPageResponse(sellers, criteria.page(), criteria.size(), totalElements);
    }
}
